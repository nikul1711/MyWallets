package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The person using the app. The app is fully local, so this is a profile, not a login account.
 * There is only ever one row, with id = [LOCAL_USER_ID].
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long = LOCAL_USER_ID,
    val displayName: String,
    val email: String? = null,
    val avatarUri: String? = null,
    /** ISO 4217 currency code such as "USD". Used as the default for new wallets, budgets and goals. */
    val defaultCurrency: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val LOCAL_USER_ID = 1L
    }
}
