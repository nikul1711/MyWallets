package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO = "Data Access Object": the only place that contains SQL for this table.
 * Functions returning Flow emit again every time the table changes.
 * `suspend` functions run on a background thread, so they never block the screen.
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = 1")
    fun observe(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun get(): UserEntity?

    /** Insert, or replace if the id already exists. */
    @Upsert
    suspend fun upsert(user: UserEntity)
}
