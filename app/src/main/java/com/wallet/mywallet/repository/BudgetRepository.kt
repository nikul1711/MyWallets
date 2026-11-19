package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.BudgetDao
import com.wallet.mywallet.db.entity.BudgetEntity
import com.wallet.mywallet.db.model.BudgetWithSpent
import com.wallet.mywallet.util.DateUtils
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    /** Budgets with the amount spent so far in the current calendar month. */
    fun observeCurrentMonth(): Flow<List<BudgetWithSpent>> =
        budgetDao.observeWithSpent(
            from = DateUtils.startOfMonth(0),
            to = DateUtils.startOfMonth(1)
        )

    suspend fun getBudget(id: Long): BudgetEntity? = budgetDao.getById(id)

    suspend fun count(): Int = budgetDao.count()

    suspend fun save(budget: BudgetEntity): Long = budgetDao.upsert(budget)

    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)
}
