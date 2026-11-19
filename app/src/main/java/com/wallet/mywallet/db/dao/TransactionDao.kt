package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.TransactionEntity
import com.wallet.mywallet.db.model.CategoryTotal
import com.wallet.mywallet.db.model.CurrencyTotal
import com.wallet.mywallet.db.model.MonthlyTotal
import com.wallet.mywallet.db.model.TransactionDetail
import kotlinx.coroutines.flow.Flow

/**
 * Every date range in this file is "from (inclusive) to (exclusive)" in epoch milliseconds,
 * so a month is [first day of month, first day of next month).
 */
@Dao
interface TransactionDao {

    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    /** All transactions, newest first, with the wallet and category names for display. */
    @Query(
        """
        SELECT t.*, c.name AS categoryName, w.name AS walletName, w.currency AS walletCurrency
        FROM transactions t
        INNER JOIN wallets w ON w.id = t.walletId
        LEFT JOIN categories c ON c.id = t.categoryId
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun observeDetails(): Flow<List<TransactionDetail>>

    /** Income and expense per currency (wallets can use different currencies). */
    @Query(
        """
        SELECT w.currency AS currency,
               SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income,
               SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense
        FROM transactions t
        INNER JOIN wallets w ON w.id = t.walletId
        WHERE t.date >= :from AND t.date < :to
        GROUP BY w.currency
        ORDER BY w.currency
        """
    )
    fun observeTotalsByCurrency(from: Long, to: Long): Flow<List<CurrencyTotal>>

    /** Report: how much was spent in each category, biggest first. */
    @Query(
        """
        SELECT t.categoryId AS categoryId, c.name AS categoryName, SUM(t.amount) AS total
        FROM transactions t
        INNER JOIN wallets w ON w.id = t.walletId
        LEFT JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND w.currency = :currency AND t.date >= :from AND t.date < :to
        GROUP BY t.categoryId
        ORDER BY total DESC
        """
    )
    fun observeExpenseByCategory(currency: String, from: Long, to: Long): Flow<List<CategoryTotal>>

    /** Report: income and expense for every month in the range (months without data are missing). */
    @Query(
        """
        SELECT strftime('%Y-%m', t.date / 1000, 'unixepoch', 'localtime') AS month,
               SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income,
               SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense
        FROM transactions t
        INNER JOIN wallets w ON w.id = t.walletId
        WHERE w.currency = :currency AND t.date >= :from AND t.date < :to
        GROUP BY month
        ORDER BY month
        """
    )
    fun observeMonthlyTotals(currency: String, from: Long, to: Long): Flow<List<MonthlyTotal>>
}
