package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.TransactionDao
import com.wallet.mywallet.db.entity.TransactionEntity
import com.wallet.mywallet.db.model.CategoryTotal
import com.wallet.mywallet.db.model.CurrencyTotal
import com.wallet.mywallet.db.model.MonthlyTotal
import com.wallet.mywallet.db.model.TransactionDetail
import kotlinx.coroutines.flow.Flow

/** Transactions and everything calculated from them (monthly summary and reports). */
class TransactionRepository(private val transactionDao: TransactionDao) {

    fun observeDetails(): Flow<List<TransactionDetail>> = transactionDao.observeDetails()

    fun observeTotalsByCurrency(from: Long, to: Long): Flow<List<CurrencyTotal>> =
        transactionDao.observeTotalsByCurrency(from, to)

    fun observeExpenseByCategory(currency: String, from: Long, to: Long): Flow<List<CategoryTotal>> =
        transactionDao.observeExpenseByCategory(currency, from, to)

    fun observeMonthlyTotals(currency: String, from: Long, to: Long): Flow<List<MonthlyTotal>> =
        transactionDao.observeMonthlyTotals(currency, from, to)

    suspend fun getTransaction(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun save(transaction: TransactionEntity): Long = transactionDao.upsert(transaction)

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)
}
