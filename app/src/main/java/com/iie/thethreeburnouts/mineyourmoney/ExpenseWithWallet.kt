package com.iie.thethreeburnouts.mineyourmoney

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseWithWallet(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "walletId",
        entityColumn = "id"
    )
    val wallet: Wallet
)
