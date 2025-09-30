package com.iie.thethreeburnouts.mineyourmoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletAdapter (private var wallets: List<Wallet>) :
    RecyclerView.Adapter<WalletAdapter.WalletViewHolder>(){

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

    fun updateList(newWallets: List<Wallet>) {
        wallets = newWallets
        notifyDataSetChanged()
    }
}