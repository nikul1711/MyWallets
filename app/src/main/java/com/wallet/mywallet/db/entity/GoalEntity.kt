package com.wallet.mywallet.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A savings goal, for example "New laptop". */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Amount to reach, in minor units (cents). */
    val targetAmount: Long,
    /** Amount saved so far, in minor units (cents). */
    val savedAmount: Long = 0,
    val currency: String,
    /** Optional target date, as the start of a local day in epoch milliseconds. */
    val deadline: Long? = null,
    val createdAt: Long
)
