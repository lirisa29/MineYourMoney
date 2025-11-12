package com.iie.thethreeburnouts.mineyourmoney.spendingoverview

import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseWithWallet

sealed class TransactionItem {
    data class Header(val monthYear: String) : TransactionItem()
    data class Expense(val expense: ExpenseWithWallet) : TransactionItem()
}