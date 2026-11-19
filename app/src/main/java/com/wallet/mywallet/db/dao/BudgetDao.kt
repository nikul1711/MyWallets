package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.BudgetEntity
import com.wallet.mywallet.db.model.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Upsert
    suspend fun upsert(budget: BudgetEntity): Long

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun count(): Int

    /**
     * Every budget with the amount spent between [from] and [to] (normally the current month).
     * A budget without a category counts all expenses; otherwise only that category.
     * Only wallets with the budget's currency are counted.
     */
    @Query(
        """
        SELECT b.*, c.name AS categoryName,
               COALESCE((
                   SELECT SUM(t.amount)
                   FROM transactions t
                   INNER JOIN wallets w ON w.id = t.walletId
                   WHERE t.type = 'EXPENSE'
                     AND w.currency = b.currency
                     AND t.date >= :from AND t.date < :to
                     AND (b.categoryId IS NULL OR t.categoryId = b.categoryId)
               ), 0) AS spent
        FROM budgets b
        LEFT JOIN categories c ON c.id = b.categoryId
        ORDER BY b.id
        """
    )
    fun observeWithSpent(from: Long, to: Long): Flow<List<BudgetWithSpent>>
}
