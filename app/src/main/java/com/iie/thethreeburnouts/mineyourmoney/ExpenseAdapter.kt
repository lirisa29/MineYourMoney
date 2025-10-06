package com.iie.thethreeburnouts.mineyourmoney

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.exp

class ExpenseAdapter (private var expenses: List<TransactionItem>,
                      private val onExpenseClick: (Int) -> Unit):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() { //(Google Developers Training team, 2024)

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EXPENSE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (expenses[position]) {
            is TransactionItem.Header -> TYPE_HEADER
            is TransactionItem.Expense -> TYPE_EXPENSE
        }
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding, private val onExpenseClick: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) { //(Google Developers Training team, 2024)
        fun bind(expenseWithWallet: ExpenseWithWallet) {
            val expense = expenseWithWallet.expense
            val wallet = expenseWithWallet.wallet

            Log.d("ExpenseAdapter", "Binding expense: id=${expense.id}, amount=${expense.amount}, wallet=${wallet.name}")


            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(expense.date))

            binding.imgWalletIcon.setImageResource(wallet.iconResId)
            binding.imgWalletIcon.imageTintList = ColorStateList.valueOf(wallet.color)
            binding.tvWalletName.text = wallet.name
            binding.tvTransactionDate.text = formattedDate
            binding.tvTransactionAmount.text = "-R${String.format("%,.2f", expense.amount)}"

            // Handle item click
            binding.root.setOnClickListener {
                Log.d("ExpenseAdapter", "Expense Clicked")
                onExpenseClick(expense.id) // pass expense ID
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder { //(Google Developers Training team, 2024)
        // Inflate the view using View Binding
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val binding = ItemExpenseBinding.inflate(inflater, parent, false)
                ExpenseViewHolder(binding, onExpenseClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) { //(Google Developers Training team, 2024)
        when (holder) {
            is HeaderViewHolder -> holder.bind(expenses[position] as TransactionItem.Header)
            is ExpenseViewHolder -> holder.bind((expenses[position] as TransactionItem.Expense).expense)
        }
    }

    // ViewHolders
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //(Google Developers Training team, 2024)
        fun bind(header: TransactionItem.Header) {
            itemView.findViewById<TextView>(R.id.tvMonthHeader).text = header.monthYear
        }
    }

    override fun getItemCount(): Int = expenses.size

    // Updates the adapter list
    fun updateList(newExpense: List<ExpenseWithWallet>) {
        val groupedList = mutableListOf<TransactionItem>()
        val formatter = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        // Group by month/year
        newExpense.groupBy { formatter.format(it.expense.date) }
            .forEach { (monthYear, list) ->
                groupedList.add(TransactionItem.Header(monthYear))
                list.forEach { groupedList.add(TransactionItem.Expense(it)) }
            }

        expenses = groupedList
        notifyDataSetChanged()
        Log.d("ExpenseAdapater", "Adapter notified of data change")
    }
}

//REFERENCE LIST
/* Google Developers Training team, 2024). Create dynamic lists with recyclerView. [Online].
Available at: https://developer.android.com/develop/ui/views/layout/recyclerview [Accessed 3 October 2025). */