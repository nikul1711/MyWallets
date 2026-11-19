package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Upsert
    suspend fun upsert(goal: GoalEntity): Long

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY id")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun count(): Int

    /** Adds money to a goal. A negative [amount] takes money out. The result never goes below zero. */
    @Query("UPDATE goals SET savedAmount = MAX(0, savedAmount + :amount) WHERE id = :id")
    suspend fun addSaved(id: Long, amount: Long)
}
