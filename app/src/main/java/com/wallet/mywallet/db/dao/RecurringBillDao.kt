package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.RecurringBillEntity
import com.wallet.mywallet.db.model.BillDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {

    @Upsert
    suspend fun upsert(bill: RecurringBillEntity): Long

    @Delete
    suspend fun delete(bill: RecurringBillEntity)

    @Query("SELECT * FROM recurring_bills WHERE id = :id")
    suspend fun getById(id: Long): RecurringBillEntity?

    /** All bills, the ones due soonest first. */
    @Query(
        """
        SELECT b.*, w.name AS walletName, w.currency AS walletCurrency, c.name AS categoryName
        FROM recurring_bills b
        INNER JOIN wallets w ON w.id = b.walletId
        LEFT JOIN categories c ON c.id = b.categoryId
        ORDER BY b.isActive DESC, b.nextDueDate
        """
    )
    fun observeDetails(): Flow<List<BillDetail>>

    /**
     * Active bills whose reminder period has started: due date minus the reminder days is today or earlier.
     * Used by the daily reminder worker. 86400000 = milliseconds in one day.
     */
    @Query(
        """
        SELECT b.*, w.name AS walletName, w.currency AS walletCurrency, c.name AS categoryName
        FROM recurring_bills b
        INNER JOIN wallets w ON w.id = b.walletId
        LEFT JOIN categories c ON c.id = b.categoryId
        WHERE b.isActive = 1 AND b.nextDueDate - b.remindDaysBefore * 86400000 <= :now
        ORDER BY b.nextDueDate
        """
    )
    suspend fun getDueForReminder(now: Long): List<BillDetail>
}
