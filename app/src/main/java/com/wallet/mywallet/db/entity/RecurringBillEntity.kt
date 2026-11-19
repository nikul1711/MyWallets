package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A bill that repeats, such as rent or a streaming subscription. */
@Entity(
    tableName = "recurring_bills",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("walletId"), Index("categoryId"), Index("nextDueDate")]
)
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Amount in minor units (cents), in the currency of the wallet. */
    val amount: Long,
    /** The wallet the bill is paid from. */
    val walletId: Long,
    val categoryId: Long? = null,
    val interval: RecurrenceInterval,
    /** Next due day, as the start of a local day in epoch milliseconds. */
    val nextDueDate: Long,
    /** How many days before the due date the reminder notification starts. */
    val remindDaysBefore: Int = 1,
    /** A paused bill shows no reminders. */
    val isActive: Boolean = true,
    val createdAt: Long
)
