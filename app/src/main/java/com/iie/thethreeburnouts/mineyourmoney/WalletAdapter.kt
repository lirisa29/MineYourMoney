package com.iie.thethreeburnouts.mineyourmoney



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletAdapter(private var wallets: List<Wallet>) :
    RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    // Keep a copy of the full list for filtering
    private val fullList: List<Wallet> = ArrayList(wallets)

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.img_wallet_icon)
        val name: TextView = itemView.findViewById(R.id.tv_wallet_name)
        val amount: TextView = itemView.findViewById(R.id.tv_wallet_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = wallets[position]
        holder.icon.setImageResource(wallet.iconResId)
        holder.name.text = wallet.name
        holder.amount.text = "R${String.format("%,.2f", wallet.balance)}"
    }

    override fun getItemCount(): Int = wallets.size

    // Updates the adapter list
    fun updateList(newWallets: List<Wallet>) {
        wallets = newWallets
        notifyDataSetChanged()
    }

    // Filter wallets by name and sort alphabetically
    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullList.sortedBy { it.name.lowercase() } // Sort alphabetically
        } else {
            fullList.filter { it.name.contains(query, ignoreCase = true) }
                .sortedBy { it.name.lowercase() } // Sort filtered results alphabetically
        }
        updateList(filteredList)
    }
}
