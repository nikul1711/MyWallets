package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.GoalDao
import com.wallet.mywallet.db.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {

    fun observeGoals(): Flow<List<GoalEntity>> = goalDao.observeAll()

    suspend fun getGoal(id: Long): GoalEntity? = goalDao.getById(id)

    suspend fun count(): Int = goalDao.count()

    suspend fun save(goal: GoalEntity): Long = goalDao.upsert(goal)

    suspend fun delete(goal: GoalEntity) = goalDao.delete(goal)

    /** Puts money into a goal. Pass a negative amount to take money out. */
    suspend fun addSavings(goalId: Long, amount: Long) = goalDao.addSaved(goalId, amount)
}
