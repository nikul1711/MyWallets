package com.wallet.mywallet.db.model

import androidx.room.Embedded
import com.wallet.mywallet.db.entity.WalletEntity

/** A wallet together with its current balance (initial balance + income - expenses). */
data class WalletWithBalance(
    @Embedded val wallet: WalletEntity,
    val balance: Long
)
