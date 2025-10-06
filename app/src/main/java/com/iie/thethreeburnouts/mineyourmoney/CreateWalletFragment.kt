package com.iie.thethreeburnouts.mineyourmoney

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
    private var selectedColor: Int? = null

    private val walletsViewModel: WalletsViewModel by activityViewModels {
        // Pass the currentUserId from MainActivity to the ViewModelFactory
        WalletsViewModelFactory(requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("CreateWalletFragment", "onViewCreated called")

        // Initialize the binding
        _binding = FragmentCreateWalletBinding.bind(view)
        binding.etInitialBalance.setText("R0.00")
        binding.etInitialBalance.setSelection(binding.etInitialBalance.text!!.length)

        var current = "R0,00"

        binding.topAppBar.setNavigationOnClickListener {
            Log.e("CreateWalletFragment", "Navigation icon clicked")
            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), addToBackStack = false)
        }

        binding.btnSelectIcon.setOnClickListener {
            Log.e("CreateWalletFragment", "Select Icon Clicked")
            val iconSheet = IconSelectorBottomSheet { selectedIconResId, selectedColour ->
                Log.e("CreateWalletFragment", "Icon Selected")
                binding.btnSelectIcon.setImageResource(selectedIconResId)
                binding.btnSelectIcon.tag = selectedIconResId
                binding.btnSelectIcon.imageTintList = ColorStateList.valueOf(selectedColour)

                selectedColor = selectedColour
            }
            iconSheet.show(parentFragmentManager, "IconSelectorBottomSheet")
        }

        binding.etWalletName.addTextChangedListener { editable ->
            if (!editable.isNullOrBlank()) {
                Log.e("CreateWalletFragment", "Wallet name entered")
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

                    Log.e("CreateWalletFragment", "Balance input changed")

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
            val iconResId = binding.btnSelectIcon.tag as? Int ?
            val colour = selectedColor ?: android.R.attr.textColorSecondary

            Log.e("CreateWalletFragment", "Confirm button clicked")

            // Clear previous errors
            if (name.isBlank()) {
                Log.e("CreateWalletFragment", "Validation failed")
                binding.walletNameLayout.error = "Please enter a wallet name."
                return@setOnClickListener
            }
            // Validate wallet name format
            val validNameRegex = "^[a-zA-Z ]{3,20}+$".toRegex()
            if (!validNameRegex.matches(name)) {
                Log.e("CreateWalletFragment", "Validation failed")
                binding.walletNameLayout.error = "Wallet name must be 3-20 characters and only contain letters."
                return@setOnClickListener
            }

            // Check if balance is empty
            if (balanceText.isBlank()) {
                Log.e("CreateWalletFragment", "Validation failed")
                binding.walletBalanceLayout.error = "Please enter an initial balance."
                return@setOnClickListener
            }
            // Check if an icon has been selected
            if(iconResId == null){
                Log.e("CreateWalletFragment", "Validation failed")
                binding.tvSelectIcon.error = "Please select an icon."
                return@setOnClickListener
            } else {
                binding.tvSelectIcon.error = null
            }

            // Clean the formatted currency string
            val cleanedBalance = balanceText.replace("[^\\d.]".toRegex(), "")
            val balance = cleanedBalance.toDouble()
             Log.e("CreateWalletFragment", "Parsed the balance")

            // Ensure balance is greater than zero
            if (balance <= 0) {
                Log.e("CreateWalletFragment", "Validation failed: balance <= 0")
                binding.walletBalanceLayout.error = "Please enter an initial balance."
                return@setOnClickListener
            }

            // Check for duplicate wallet names
            lifecycleScope.launch {
                Log.e("CreateWalletFragment", "Checking if wallet name exists in the database")
                val exists = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext()).walletDao().walletExists(
                        (requireActivity() as MainActivityProvider).getCurrentUserId(), name
                    )
                }
                Log.e("CreateWalletFragment", "Wallet name exists")

                if (exists) {
                    Log.e("CreateWalletFragment", "Validation failed: duplicate wallet name")
                    binding.walletNameLayout.error = "A wallet with this name already exists."
                    return@launch
                }

                val newWallet = Wallet(name = name, balance = balance, iconResId = iconResId, color = colour, userId = 0)
                Log.e("CreateWalletFragment", "Creating new wallet")
                walletsViewModel.addWallet(newWallet)
                Log.e("CreateWalletFragment", "Wallet added successfully!")
                (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), addToBackStack = false)
            }
        }
        // clears focus when clicking outside EditTexts
        binding.createWalletRoot.setOnClickListener {
            binding.etWalletName.clearFocus()
            binding.etInitialBalance.clearFocus()
        }
        // Hide keyboard when touching outside EditTexts
        @Suppress("ClickableViewAccessibility") //Coding Meet, 2023)
        binding.scrollView.setOnTouchListener { v, _ -> //Coding Meet, 2023)
            binding.etWalletName.clearFocus() //Coding Meet, 2023)
            binding.etInitialBalance.clearFocus() //Coding Meet, 2023)
            v.performClick() //Coding Meet, 2023)

            val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager //Coding Meet, 2023)
            imm.hideSoftInputFromWindow(v.windowToken, 0) //Coding Meet, 2023)
            // clear keyboard focus
            false //Coding Meet, 2023)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leak
    }
}

//REFERENCE LIST:
/*(Coding Meet, 2023). How to Implement Hide Soft keyboard in Android Studio Kotlin. [video online].
Available at: https://www.youtube.com/watch?v=_omdGBzLuWY  [Accessed 5 October 2025). */
