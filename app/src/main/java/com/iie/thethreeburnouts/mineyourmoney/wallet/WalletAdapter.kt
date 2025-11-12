package com.iie.thethreeburnouts.mineyourmoney.wallet

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iie.thethreeburnouts.mineyourmoney.databinding.ItemWalletBinding

class WalletAdapter(private var wallets: List<Wallet>,
                    private val onDeleteClicked: (Wallet) -> Unit,
                    private val onEditClicked: (Wallet) -> Unit):
    RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() { //(Google Developers Training team, 2024)

    var swipedPosition: Int? = null
    var swipeThreshold: Float = 300f // pass from fragment if needed

    inner class WalletViewHolder(val binding: ItemWalletBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallet: Wallet, position: Int) {
            binding.imgWalletIcon.setImageResource(wallet.iconResId)
            binding.imgWalletIcon.imageTintList = ColorStateList.valueOf(wallet.color)
            binding.tvWalletName.text = wallet.name
            binding.tvWalletAmount.text = "R${String.format("%,.2f", wallet.balance)}"

            // Set translation based on swipe state
            val isSwiped = swipedPosition == position
            binding.cardForeground.translationX = if (isSwiped) swipeThreshold else 0f

            // Only allow button clicks if swiped
            binding.btnDeleteWallet.isEnabled = isSwiped
            binding.btnEditWallet.isEnabled = isSwiped

            binding.btnDeleteWallet.visibility = if (isSwiped) View.VISIBLE else View.INVISIBLE
            binding.btnEditWallet.visibility = if (isSwiped) View.VISIBLE else View.INVISIBLE

            binding.btnDeleteWallet.setOnClickListener {
                if (isSwiped) {
                    onDeleteClicked(wallet)
                    swipedPosition = null
                }
            }

            binding.btnEditWallet.setOnClickListener {
                if (isSwiped) {
                    onEditClicked(wallet)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val binding = ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position], position)
    }

    override fun getItemCount(): Int = wallets.size

    fun updateList(newWallets: List<Wallet>) {
        wallets = newWallets
        swipedPosition = null // reset swipe whenever list changes
        notifyDataSetChanged()
    }
}