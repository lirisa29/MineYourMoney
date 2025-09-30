package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment

class CreateWalletFragment : Fragment(R.layout.fragment_create_wallet){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val btnSelectIcon = view.findViewById<View>(R.id.btn_select_icon)
        val iconImage = view.findViewById<ImageButton>(R.id.img_icon)
        val walletNameInput = view.findViewById<TextInputEditText>(R.id.et_wallet_name)
        val walletBalanceInput = view.findViewById<TextInputEditText>(R.id.et_initial_balance)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm)

        topBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        btnSelectIcon.setOnClickListener {
            val iconSheet = IconSelectorBottomSheet{ selectedIconResId ->
                iconImage.setImageResource(selectedIconResId)
                iconImage.tag = selectedIconResId // Store the selected icon resource ID in the tag
            }
            iconSheet.show(parentFragmentManager, "IconSelectorBottomSheet")
        }

        btnConfirm.setOnClickListener {
            val name = walletNameInput.text.toString().ifBlank { "My Wallet" }
            val balanceText = walletBalanceInput.text.toString()
            val balance = balanceText.toDoubleOrNull() ?: 0.0
            val iconResId = iconImage.tag as? Int ?: R.drawable.ic_wallets

            val walletExists = WalletRepository.walletExists(name)
            if (walletExists) {
                walletNameInput.error = "A wallet with this name already exists."
                return@setOnClickListener
            }
            if (walletNameInput.text.isNullOrBlank()) {
                walletNameInput.error = "Please enter a wallet name."
                return@setOnClickListener
            }
            if (balance <= 0) {
                walletBalanceInput.error = "Please enter an initial balance."
                return@setOnClickListener
            }
            val newWallet = Wallet(name, balance, iconResId)
            WalletRepository.addWallet(newWallet)
            requireActivity().onBackPressed()

        }

    }

}