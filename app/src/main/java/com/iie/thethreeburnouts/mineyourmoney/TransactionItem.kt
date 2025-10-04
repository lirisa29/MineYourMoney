package com.iie.thethreeburnouts.mineyourmoney

sealed class TransactionItem {
    data class Header(val monthYear: String) : TransactionItem()
    data class Expense(val expense: ExpenseWithWallet) : TransactionItem()
}