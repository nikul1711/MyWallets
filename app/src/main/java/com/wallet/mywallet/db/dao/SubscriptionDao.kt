package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wallet.mywallet.db.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    /** The newest row is the current subscription. */
    @Query("SELECT * FROM subscriptions ORDER BY id DESC LIMIT 1")
    fun observeCurrent(): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions ORDER BY id DESC LIMIT 1")
    suspend fun getCurrent(): SubscriptionEntity?

    @Insert
    suspend fun insert(subscription: SubscriptionEntity): Long
}
