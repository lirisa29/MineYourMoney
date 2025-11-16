package com.iie.thethreeburnouts.mineyourmoney.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemTopWalletsBinding
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet

class TopWalletsAdapter (private var wallets: List<Wallet>) :
    RecyclerView.Adapter<TopWalletsAdapter.WalletViewHolder>() {

    inner class WalletViewHolder(val binding: ItemTopWalletsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallet: Wallet, position: Int) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopWalletsAdapter.WalletViewHolder {
        val binding = ItemTopWalletsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopWalletsAdapter.WalletViewHolder, position: Int) {
        holder.bind(wallets[position], position)
    }

    override fun getItemCount(): Int = wallets.size
}