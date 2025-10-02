package com.iie.thethreeburnouts.mineyourmoney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemWalletBinding

class WalletAdapter(private var wallets: List<Wallet>) :
    RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    inner class WalletViewHolder(private val binding: ItemWalletBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallet: Wallet) {
            binding.imgWalletIcon.setImageResource(wallet.iconResId)
            binding.tvWalletName.text = wallet.name
            binding.tvWalletAmount.text = "R${String.format("%,.2f", wallet.balance)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        // Inflate the view using View Binding
        val binding = ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = wallets[position]
        holder.bind(wallet)
    }

    override fun getItemCount(): Int = wallets.size

    // Updates the adapter list
    fun updateList(newWallets: List<Wallet>) {
        wallets = newWallets
        notifyDataSetChanged()
    }
}

