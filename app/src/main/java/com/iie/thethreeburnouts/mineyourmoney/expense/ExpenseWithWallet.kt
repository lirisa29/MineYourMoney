package com.iie.thethreeburnouts.mineyourmoney.expense

import androidx.room.Embedded
import androidx.room.Relation
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet

data class ExpenseWithWallet(
    @Embedded val expense: Expense,
    @Relation(
        parentColumn = "walletId",
        entityColumn = "id"
    )
    val wallet: Wallet
)