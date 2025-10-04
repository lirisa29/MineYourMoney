package com.iie.thethreeburnouts.mineyourmoney

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
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
        binding.etInitialBalance.setText("R0.00")
        binding.etInitialBalance.setSelection(binding.etInitialBalance.text!!.length)


        var current = "R0,00"

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

        binding.etWalletName.addTextChangedListener { editable ->
            if (!editable.isNullOrBlank()) {
                binding.etWalletName.error = null
                binding.walletNameLayout.isErrorEnabled = false
            }
        }

        binding.etInitialBalance.addTextChangedListener(object : android.text.TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    binding.etInitialBalance.error = null
                    binding.walletBalanceLayout.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != current) {
                    binding.etInitialBalance.removeTextChangedListener(this)

                    // Remove all non-digit characters
                    val cleanString = s.toString().replace("[R,.\\s]".toRegex(), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = "R${String.format("%,.2f", parsed / 100)}"

                    current = formatted
                    binding.etInitialBalance.setText(formatted)
                    binding.etInitialBalance.setSelection(formatted.length)

                    binding.etInitialBalance.addTextChangedListener(this)
                }
            }
        })

        binding.btnConfirm.setOnClickListener {
            val name = binding.etWalletName.text.toString()
            val balanceText = binding.etInitialBalance.text.toString()
            val iconResId = binding.btnSelectIcon.tag as? Int ?: R.drawable.ic_wallets

            if (name.isBlank()) {
                binding.walletNameLayout.error = "Please enter a wallet name."
                return@setOnClickListener
            }

            val validNameRegex = "^[a-zA-Z ]{3,20}+$".toRegex()
            if (!validNameRegex.matches(name)) {
                binding.walletNameLayout.error = "Wallet name must be 3-20 characters and only contain letters."
                return@setOnClickListener
            }

            if (balanceText.isBlank()) {
                binding.walletBalanceLayout.error = "Please enter an initial balance."
                return@setOnClickListener
            }

            // Clean the formatted currency string
            val cleanedBalance = balanceText.replace("[^\\d.]".toRegex(), "")
            val balance = cleanedBalance.toDouble()

            if (balance <= 0) {
                binding.walletBalanceLayout.error = "Please enter an initial balance."
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val exists = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext()).walletDao().walletExists(name)
                }

                if (exists) {
                    binding.walletNameLayout.error = "A wallet with this name already exists."
                    return@launch
                }

                val newWallet = Wallet(name = name, balance = balance, iconResId = iconResId)
                walletsViewModel.addWallet(newWallet)
                requireActivity().onBackPressed()
            }
        }
        // clears focus when clicking outside EditTexts
        binding.createWalletRoot.setOnClickListener {
            binding.etWalletName.clearFocus()
            binding.etInitialBalance.clearFocus()
        }
        // Hide keyboard when touching outside EditTexts
        @Suppress("ClickableViewAccessibility")
        binding.scrollView.setOnTouchListener { v, _ ->
            binding.etWalletName.clearFocus()
            binding.etInitialBalance.clearFocus()
            v.performClick()

            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            // clear keyboard focus
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leak
    }
}
