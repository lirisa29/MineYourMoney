package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentCreateWalletBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateWalletFragment : Fragment(R.layout.fragment_create_wallet) {

    private var _binding: FragmentCreateWalletBinding? = null
    private val binding get() = _binding!!

    private val walletsViewModel: WalletsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the binding
        _binding = FragmentCreateWalletBinding.bind(view)

        val walletDao = AppDatabase.getInstance(requireContext()).walletDao()

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnSelectIcon.setOnClickListener {
            val iconSheet = IconSelectorBottomSheet { selectedIconResId ->
                binding.btnSelectIcon.setImageResource(selectedIconResId)
                binding.btnSelectIcon.tag = selectedIconResId
            }
            iconSheet.show(parentFragmentManager, "IconSelectorBottomSheet")
        }

        binding.btnConfirm.setOnClickListener {
            val name = binding.etWalletName.text.toString().ifBlank { "My Wallet" }
            val balanceText = binding.etInitialBalance.text.toString()
            val balance = balanceText.toDoubleOrNull() ?: 0.0
            val iconResId = binding.btnSelectIcon.tag as? Int ?: R.drawable.ic_wallets

            // Run Room operations in a coroutine
            lifecycleScope.launch {
                val exists = withContext(Dispatchers.IO) {
                    walletDao.walletExists(name)
                }

                if (exists) {
                    binding.walletNameLayout.error = "A wallet with this name already exists."
                    return@launch
                }

                if (binding.etWalletName.text.isNullOrBlank()) {
                    binding.walletNameLayout.error = "Please enter a wallet name."
                    return@launch
                }

                if (balance <= 0) {
                    binding.walletBalanceLayout.error = "Please enter an initial balance."
                    return@launch
                }

                val newWallet = Wallet(name = name, balance = balance, iconResId = iconResId)

                walletsViewModel.addWallet(newWallet)
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leak
    }
}
