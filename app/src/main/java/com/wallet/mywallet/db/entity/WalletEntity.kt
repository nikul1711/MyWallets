package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A place where money is kept: cash, bank account, card... */
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ISO 4217 currency code such as "USD". */
    val currency: String,
    /**
     * Money is ALWAYS stored as a whole number of minor units (cents), never as Double,
     * because 0.1 + 0.2 != 0.3 in floating point. 12.50 is stored as 1250.
     */
    val initialBalance: Long = 0,
    val createdAt: Long
)
