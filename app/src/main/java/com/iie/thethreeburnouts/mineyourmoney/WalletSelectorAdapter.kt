package com.iie.thethreeburnouts.mineyourmoney

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletSelectorAdapter(
    private var wallets: List<Wallet>,
    private var selectedPosition: Int = RecyclerView.NO_POSITION,
    private val onWalletSelected: ((Wallet) -> Unit)? = null
) : RecyclerView.Adapter<WalletSelectorAdapter.WalletViewHolder>() {

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.img_wallet_icon)
        val name: TextView = itemView.findViewById(R.id.tv_wallet_name)
        val amount: TextView = itemView.findViewById(R.id.tv_wallet_amount)
        val radioButton: RadioButton = itemView.findViewById(R.id.btn_radio)

        init {
            radioButton.setOnClickListener {
                selectWallet(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
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
        wallets = newWallets
        notifyDataSetChanged()
    }

    private fun selectWallet(position: Int) {
        if (position == RecyclerView.NO_POSITION) return

        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)

        onWalletSelected?.invoke(wallets[position])
    }

    fun getSelectedWallet(): Wallet? =
        if (selectedPosition != RecyclerView.NO_POSITION) wallets[selectedPosition] else null
}