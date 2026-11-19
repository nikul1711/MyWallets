package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A monthly spending limit. It restarts every calendar month.
 * The amount spent is NOT stored: it is calculated from transactions, so it can never go out of date.
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Null means "all expenses". */
    val categoryId: Long? = null,
    /** Monthly limit in minor units (cents). */
    val limitAmount: Long,
    /** Only expenses from wallets with this currency count towards the budget. */
    val currency: String,
    val createdAt: Long
)
