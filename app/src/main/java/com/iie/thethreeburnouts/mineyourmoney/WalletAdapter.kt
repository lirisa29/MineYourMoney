package com.iie.thethreeburnouts.mineyourmoney

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemWalletBinding

class WalletAdapter(private var wallets: List<Wallet>,
                    private val onDeleteClicked: (Wallet) -> Unit,
                    private val onEditClicked: (Wallet) -> Unit):
    RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() { //(Google Developers Training team, 2024)

    inner class WalletViewHolder(val binding: ItemWalletBinding) : RecyclerView.ViewHolder(binding.root) { //(Google Developers Training team, 2024)
        fun bind(wallet: Wallet) {
            binding.imgWalletIcon.setImageResource(wallet.iconResId)
            binding.imgWalletIcon.imageTintList = ColorStateList.valueOf(wallet.color)
            binding.tvWalletName.text = wallet.name
            binding.tvWalletAmount.text = "R${String.format("%,.2f", wallet.balance)}"

            // Button click listeners
            binding.btnDeleteWallet.setOnClickListener {
                onDeleteClicked(wallet)
            }

            binding.btnEditWallet.setOnClickListener {
                onEditClicked(wallet)
            }
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
//Reference List:
/* Google Developers Training team. 2024. Create dynamic lists with recyclerView. [Online].
Available at: https://developer.android.com/develop/ui/views/layout/recyclerview [Accessed 3 October 2025). */

