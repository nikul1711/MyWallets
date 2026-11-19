package com.wallet.mywallet.repository

import androidx.room.withTransaction
import com.wallet.mywallet.db.AppDatabase
import com.wallet.mywallet.db.entity.RecurringBillEntity
import com.wallet.mywallet.db.entity.TransactionEntity
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.db.model.BillDetail
import com.wallet.mywallet.util.DateUtils
import kotlinx.coroutines.flow.Flow

class BillRepository(private val db: AppDatabase) {

    private val billDao = db.recurringBillDao()
    private val transactionDao = db.transactionDao()

    fun observeBills(): Flow<List<BillDetail>> = billDao.observeDetails()

    suspend fun getBill(id: Long): RecurringBillEntity? = billDao.getById(id)

    suspend fun save(bill: RecurringBillEntity): Long = billDao.upsert(bill)

    suspend fun delete(bill: RecurringBillEntity) = billDao.delete(bill)

    /** Bills that need a reminder notification today. Used by the daily worker. */
    suspend fun getDueForReminder(): List<BillDetail> =
        billDao.getDueForReminder(System.currentTimeMillis())

    /**
     * "Mark as paid": records an expense for today and moves the bill to its next due date.
     * Both steps happen in one database transaction: either both succeed or neither does.
     */
    suspend fun markPaid(bill: RecurringBillEntity) {
        db.withTransaction {
            transactionDao.upsert(
                TransactionEntity(
                    walletId = bill.walletId,
                    categoryId = bill.categoryId,
                    amount = bill.amount,
                    type = TransactionType.EXPENSE,
                    date = DateUtils.today(),
                    note = bill.name
                )
            )
            billDao.upsert(
                bill.copy(nextDueDate = DateUtils.nextDueDate(bill.nextDueDate, bill.interval))
            )
        }
    }
}
