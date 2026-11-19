package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A label for a transaction, such as "Food & Drink" or "Salary". */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** An income category is used for income transactions, an expense category for expenses. */
    val type: TransactionType
)
