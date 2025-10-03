package com.iie.thethreeburnouts.mineyourmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter (private var expenses: List<ExpenseWithWallet>):
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expenseWithWallet: ExpenseWithWallet) {
            val expense = expenseWithWallet.expense
            val wallet = expenseWithWallet.wallet

            // Format date (Long -> String)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(expense.date))

            binding.imgWalletIcon.setImageResource(wallet.iconResId)
            binding.tvWalletName.text = wallet.name
            binding.tvTransactionDate.text = formattedDate
            binding.tvTransactionAmount.text = "R${String.format("%,.2f", expense.amount)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        // Inflate the view using View Binding
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    // Updates the adapter list
    fun updateList(newExpense: List<ExpenseWithWallet>) {
        expenses = newExpense
        notifyDataSetChanged()
    }
}