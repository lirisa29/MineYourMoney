package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateWalletFragment : Fragment(R.layout.fragment_create_wallet) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val btnSelectIcon = view.findViewById<View>(R.id.btn_select_icon)
        val iconImage = view.findViewById<ImageButton>(R.id.img_icon)
        val walletNameInput = view.findViewById<TextInputEditText>(R.id.et_wallet_name)
        val walletBalanceInput = view.findViewById<TextInputEditText>(R.id.et_initial_balance)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm)

        val walletDao = AppDatabase.getInstance(requireContext()).walletRepository()


        topBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        btnSelectIcon.setOnClickListener {
            val iconSheet = IconSelectorBottomSheet { selectedIconResId ->
                iconImage.setImageResource(selectedIconResId)
                iconImage.tag = selectedIconResId
            }
            iconSheet.show(parentFragmentManager, "IconSelectorBottomSheet")
        }

        btnConfirm.setOnClickListener {
            val name = walletNameInput.text.toString().ifBlank { "My Wallet" }
            val balanceText = walletBalanceInput.text.toString()
            val balance = balanceText.toDoubleOrNull() ?: 0.0
            val iconResId = iconImage.tag as? Int ?: R.drawable.ic_wallets

            // Run Room operations in a coroutine
            lifecycleScope.launch {
                val exists = withContext(Dispatchers.IO) {
                    walletDao.walletExists(name)
                }

                if (exists) {
                    walletNameInput.error = "A wallet with this name already exists."
                    return@launch
                }

                if (walletNameInput.text.isNullOrBlank()) {
                    walletNameInput.error = "Please enter a wallet name."
                    return@launch
                }

                if (balance <= 0) {
                    walletBalanceInput.error = "Please enter an initial balance."
                    return@launch
                }

                val newWallet = Wallet(name = name, balance = balance, iconResId = iconResId)
                withContext(Dispatchers.IO) {
                    walletDao.addWallet(newWallet)
                }

                requireActivity().onBackPressed()
            }
        }
    }
}
