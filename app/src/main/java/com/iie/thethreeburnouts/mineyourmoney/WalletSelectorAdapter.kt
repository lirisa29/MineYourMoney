package com.iie.thethreeburnouts.mineyourmoney

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletSelectorAdapter(
    private var wallets: List<Wallet>,
    private var selectedPosition: Int = RecyclerView.NO_POSITION, //(Google Developers Training team, 2024)
    private val onWalletSelected: ((Wallet) -> Unit)? = null
) : RecyclerView.Adapter<WalletSelectorAdapter.WalletViewHolder>() { //(Google Developers Training team, 2024)

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //(Google Developers Training team, 2024)
        val icon: ImageView = itemView.findViewById(R.id.img_wallet_icon)
        val name: TextView = itemView.findViewById(R.id.tv_wallet_name)
        val amount: TextView = itemView.findViewById(R.id.tv_wallet_amount)
        val radioButton: RadioButton = itemView.findViewById(R.id.btn_radio)

        init {
            radioButton.setOnClickListener {
                Log.d("WalletSelectorAdapter", "RadioButton clicked")
                selectWallet(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        Log.d("WalletSelectorAdapter", "Creating ViewHolder for wallet list")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_wallet, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = wallets[position]

        holder.icon.setImageResource(wallet.iconResId)
        holder.icon.imageTintList = ColorStateList.valueOf(wallet.color)
        holder.name.text = wallet.name
        holder.amount.text = "R${String.format("%,.2f", wallet.balance)}"
        holder.radioButton.isChecked = (position == selectedPosition)
    }

    override fun getItemCount(): Int = wallets.size

    fun updateList(newWallets: List<Wallet>) {
        Log.i("WalletSelectorAdapter", "Updating wallet list")
        wallets = newWallets
        notifyDataSetChanged()
    }

    private fun selectWallet(position: Int) {
        if (position == RecyclerView.NO_POSITION) return //(Google Developers Training team, 2024)

        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)

        onWalletSelected?.invoke(wallets[position])
    }

    fun getSelectedWallet(): Wallet? =
        if (selectedPosition != RecyclerView.NO_POSITION) wallets[selectedPosition] else null //(Google Developers Training team, 2024)
}

//REFERENCE LIST
/* Google Developers Training team, 2024). Create dynamic lists with recyclerView. [Online].
Available at: https://developer.android.com/develop/ui/views/layout/recyclerview [Accessed 3 October 2025). */

