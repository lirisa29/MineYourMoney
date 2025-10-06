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
    private var remainingBudget: Double = 0.0
    private var walletToEdit: Wallet? = null

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

        walletToEdit = arguments?.getParcelable("wallet_to_edit")

        if (walletToEdit != null) {
            setupEditMode(walletToEdit!!)
        } else {
            setupCreateMode()
        }

        setupListeners()

        lifecycleScope.launch {
            val userId = (requireActivity() as MainActivityProvider).getCurrentUserId()
            remainingBudget = AppDatabase.getInstance(requireContext())
                .budgetDao()
                .getRemainingBudget(userId)
            // Show remaining budget in TextView
            updateMoneyLeftText()}
    }

    private fun setupEditMode(wallet: Wallet) {
        binding.topAppBar.title = "Edit Wallet"
        binding.etWalletName.setText(wallet.name)
        binding.etInitialBalance.setText("R${String.format("%,.2f", wallet.balance)}")
        binding.btnSelectIcon.setImageResource(wallet.iconResId)
        binding.btnSelectIcon.tag = wallet.iconResId
        binding.btnSelectIcon.imageTintList = ColorStateList.valueOf(wallet.color)
        selectedColor = wallet.color
    }

    private fun setupCreateMode() {
        binding.topAppBar.title = "Create Wallet"
        binding.etInitialBalance.setText("R0.00")
        binding.etInitialBalance.setSelection(binding.etInitialBalance.text!!.length)
    }

    private fun setupListeners() {
        var current = "R0,00"

        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), false)
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
                binding.etWalletName.error = null //(GeeksforGeeks, 2025)
                binding.walletNameLayout.isErrorEnabled = false //(GeeksforGeeks, 2025)
            }
        }

        binding.etInitialBalance.addTextChangedListener(object : android.text.TextWatcher { //(GeeksforGeeks, 2025)

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} //(GeeksforGeeks, 2025)

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { //(GeeksforGeeks, 2025)
                if (!s.isNullOrBlank()) {
                    binding.etInitialBalance.error = null //(GeeksforGeeks, 2025)
                    binding.walletBalanceLayout.isErrorEnabled = false //(GeeksforGeeks, 2025)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) { //(GeeksforGeeks, 2025)
                if (s.toString() != current) {
                    binding.etInitialBalance.removeTextChangedListener(this) //(GeeksforGeeks, 2025)

                    // Remove all non-digit characters
                    val cleanString = s.toString().replace("[R,.\\s]".toRegex(), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = "R${String.format("%,.2f", parsed / 100)}"

                    Log.e("CreateWalletFragment", "Balance input changed")

                    current = formatted
                    binding.etInitialBalance.setText(formatted)
                    binding.etInitialBalance.setSelection(formatted.length)

                    // **Validate against remaining budget**
                    val amount = parsed / 100
                    if (amount > remainingBudget) {
                        binding.walletBalanceLayout.error =
                            "Cannot exceed remaining budget of R${String.format("%,.2f", remainingBudget)}"
                    }

                    binding.etInitialBalance.addTextChangedListener(this) //(GeeksforGeeks, 2025)
                }

                // Update tvMoneyLeft dynamically
                updateMoneyLeftText()
            }
        })

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

        binding.btnConfirm.setOnClickListener {
            handleConfirmClick()
        }
    }

    private fun handleConfirmClick() {
        val name = binding.etWalletName.text.toString()
        val balanceText = binding.etInitialBalance.text.toString()
        val iconResId = binding.btnSelectIcon.tag as? Int
        val color = selectedColor ?: android.R.attr.textColorSecondary

        // Check if an icon has been selected
        if(iconResId == null){
            Log.e("CreateWalletFragment", "Validation failed")
            binding.tvSelectIcon.error = "Please select an icon." //(GeeksforGeeks, 2025)
            return
        } else {
            binding.tvSelectIcon.error = null //(GeeksforGeeks, 2025)
        }
        // Clear previous errors
        if (name.isBlank()) {
            Log.e("CreateWalletFragment", "Validation failed")
            binding.walletNameLayout.error = "Please enter a wallet name." //(GeeksforGeeks, 2025)
            return
        }
        // Validate wallet name format
        val validNameRegex = "^[a-zA-Z ]{3,20}+$".toRegex() //(GeeksforGeeks, 2025)
        if (!validNameRegex.matches(name)) {
            Log.e("CreateWalletFragment", "Validation failed")
            binding.walletNameLayout.error = "Wallet name must be 3-20 characters and only contain letters." //(GeeksforGeeks, 2025)
            return
        }

        // Check if balance is empty
        if (balanceText.isBlank()) {
            Log.e("CreateWalletFragment", "Validation failed")
            binding.walletBalanceLayout.error = "Please enter an initial balance." //(GeeksforGeeks, 2025)
            return
        }

        // Clean the formatted currency string
        val cleanedBalance = balanceText.replace("[^\\d.]".toRegex(), "") //(GeeksforGeeks, 2025)
        val balance = cleanedBalance.toDouble()
        Log.e("CreateWalletFragment", "Parsed the balance")

        // Ensure balance is greater than zero
        if (balance <= 0) {
            Log.e("CreateWalletFragment", "Validation failed: balance <= 0")
            binding.walletBalanceLayout.error = "Please enter an initial balance." //(GeeksforGeeks, 2025)
            return
        }

        if (balance > remainingBudget) {
            binding.walletBalanceLayout.error = //(GeeksforGeeks, 2025)
                "Initial balance cannot exceed remaining budget of R${String.format("%,.2f", remainingBudget)}"
            return
        }

        // Check for duplicate wallet names
        lifecycleScope.launch {
            Log.e("CreateWalletFragment", "Checking if wallet name exists in the database")
            val walletNameChanged = walletToEdit?.name != name
            val userId = (requireActivity() as MainActivityProvider).getCurrentUserId()

            if (walletNameChanged) {
                val exists = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext()).walletDao().walletExists(
                        userId, name
                    )
                }

                if (exists) {
                    binding.walletNameLayout.error = "A wallet with this name already exists."
                    return@launch
                }
            }

        lifecycleScope.launch {
            val userId = (requireActivity() as MainActivityProvider).getCurrentUserId()

            if (walletToEdit != null) {
                // EDIT MODE
                val updatedWallet = walletToEdit!!.copy(
                    name = name,
                    balance = balance,
                    iconResId = iconResId,
                    color = color
                )

                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(requireContext())
                    val walletDao = db.walletDao()
                    val budgetDao = db.budgetDao()
                    val expenseDao = db.expensesDao()

                    val totalExpenses = expenseDao.getTotalSpentInWallet(walletToEdit!!.id) ?: 0.0
                    val originalAllocation = walletToEdit!!.balance
                    val difference = balance - originalAllocation

                    if (difference > 0) {
                        budgetDao.addSpending(userId, difference)
                    } else if (difference < 0) {
                        budgetDao.refundSpending(userId, -difference)
                    }

                    // Update wallet in DB
                    walletDao.addWallet(updatedWallet)
                }

            } else {
                // CREATE MODE
                val newWallet = Wallet(name = name, balance = balance, iconResId = iconResId, color = color, userId = userId)
                walletsViewModel.addWallet(newWallet)
            }

            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), false)
        }
    }}

    // Helper function to update tvMoneyLeft dynamically
    private fun updateMoneyLeftText() {
        val balanceText = binding.etInitialBalance.text.toString().replace("[R,\\s]".toRegex(), "")
        val balance = balanceText.toDoubleOrNull() ?: 0.0 //(GeeksforGeeks, 2025)
        val remaining = remainingBudget - balance
        binding.tvMoneyLeft.text = "You have R${String.format("%,.2f", remaining)} remaining from your monthly limit"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leak
    }
}

//Reference List:
/* Coding Meet. 2023. How to Implement Hide Soft keyboard in Android Studio Kotlin. [video online].
Available at: https://www.youtube.com/watch?v=_omdGBzLuWY  [Accessed 5 October 2025). */
/* Geeks for Geeks. 2025. Working With the EditText in Android [Online].
Available at: https://www.geeksforgeeks.org/android/working-with-the-edittext-in-android/  [Accessed 5 October 2025). */
/* Geeks for Geeks. 2025. Implement Form Validation (Error to EditText) in Android [Online].
Available at: https://www.geeksforgeeks.org/android/implement-form-validation-error-to-edittext-in-android/ [Accessed 5 October 2025). */
