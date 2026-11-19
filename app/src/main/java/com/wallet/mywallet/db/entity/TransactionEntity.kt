package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One income or expense. */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        // Deleting a wallet deletes its transactions.
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        // Deleting a category keeps the transactions but clears their category.
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("walletId"), Index("categoryId"), Index("date")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletId: Long,
    val categoryId: Long? = null,
    /** Always positive, in minor units (cents). Whether it adds or subtracts comes from [type]. */
    val amount: Long,
    val type: TransactionType,
    /** The chosen day, stored as the start of that local day in epoch milliseconds. */
    val date: Long,
    val note: String? = null
)
