package com.iie.thethreeburnouts.mineyourmoney.expense

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModel
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar? = null
    private var selectedWalletId: Int? = null
    private var selectedDatePicker: Calendar = Calendar.getInstance()// stores last selected date
    private val picId = 123
    private var currentPhotoPath: String? = null  // stores absolute path of captured image
    private val expensesViewModel: ExpensesViewModel by activityViewModels { // (Google Developers Training team, 2025)
        ExpensesViewModelFactory( // (Google Developers Training team, 2025)
            requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etExpenseAmount.setText("R0.00")
        binding.etExpenseAmount.setSelection(binding.etExpenseAmount.text!!.length)

        var current = "R0,00"

        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), addToBackStack = false)
        }

        // Wallet selection
        binding.btnWalletDropdown.setOnClickListener {
            WalletSelectorBottomSheet(
                onWalletSelected = { wallet ->
                    selectedWallet = wallet
                    selectedWalletId = wallet.id
                    binding.tvSelectedWallet.apply {
                        text = wallet.name
                        visibility = View.VISIBLE
                    }
                    binding.imgWalletIcon.setImageResource(wallet.iconResId)
                    binding.imgWalletIcon.imageTintList = ColorStateList.valueOf(wallet.color)
                    binding.tvWallet.error = null
                },
                preselectedWalletId = selectedWalletId
            ).show(childFragmentManager, "WalletSelector")
        }

        // Recurrence selection
        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet(
                currentRecurrence = selectedRecurrence,
                onRecurrenceSelected = { recurrence ->
                    selectedRecurrence = recurrence
                    binding.tvSelectedRecurrence.apply {
                        text = recurrence
                        visibility = View.VISIBLE
                    }
                    binding.tvSelectRecurrence.error = null
                }).show(childFragmentManager, "RecurrenceSelector")

        }

        // Date selection
        binding.btnDateDropdown.setOnClickListener {
            openDatePicker()
        }

        // Camera photo upload
        binding.btnUploadPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission( //(IIE-Vega, 2025)
                    requireContext(), //(IIE-Vega, 2025)
                    Manifest.permission.CAMERA //(IIE-Vega, 2025)
                ) == PackageManager.PERMISSION_GRANTED //(IIE-Vega, 2025)
            ) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Clear error when typing amount
        binding.etExpenseAmount.addTextChangedListener(object : TextWatcher { //(GeeksforGeeks, 2025)

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} //(GeeksforGeeks, 2025)

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { //(GeeksforGeeks, 2025)
                if (!s.isNullOrBlank()) {
                    binding.etExpenseAmount.error = null //(GeeksforGeeks, 2025)
                    binding.expenseAmountLayout.isErrorEnabled = false //(GeeksforGeeks, 2025)
                }
            }

            override fun afterTextChanged(s: Editable?) { //(GeeksforGeeks, 2025)
                if (s.toString() != current) {
                    binding.etExpenseAmount.removeTextChangedListener(this) //(GeeksforGeeks, 2025)

                    // Remove all non-digit characters
                    val cleanString = s.toString().replace("[R,.\\s]".toRegex(), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = "R${String.format("%,.2f", parsed / 100)}"

                    current = formatted
                    binding.etExpenseAmount.setText(formatted)
                    binding.etExpenseAmount.setSelection(formatted.length)

                    binding.etExpenseAmount.addTextChangedListener(this) //(GeeksforGeeks, 2025)
                }
            }
        })

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            val amount = binding.etExpenseAmount.text.toString() //(GeeksforGeeks, 2025)
            val note = binding.etInputNote.text.toString() //(GeeksforGeeks, 2025)
            val cleanedBalance = amount.replace("[^\\d.]".toRegex(), "")
            val balance = cleanedBalance.toDouble()

            if (balance <= 0.0) {
                binding.expenseAmountLayout.error = "Please enter an amount" //(GeeksforGeeks, 2025)
                return@setOnClickListener
            }
            if (selectedWallet == null) {
                binding.tvWallet.error = "Please select a wallet" //(GeeksforGeeks, 2025)
                return@setOnClickListener
            }
            if (selectedDate == null) {
                binding.tvSelectDate.error = "Please select a date" //(GeeksforGeeks, 2025)
                return@setOnClickListener
            }
            if (selectedRecurrence == null) {
                binding.tvSelectRecurrence.error = "Please select recurrence" //(GeeksforGeeks, 2025)
                return@setOnClickListener
            }

            val expense = Expense(
                amount = balance,
                note = note,
                walletId = selectedWallet!!.id,          // only save ID
                recurrence = selectedRecurrence,
                date = selectedDate!!.timeInMillis,      // store millis
                photoPath = currentPhotoPath,
                userId = 0// store path as String
            )

            expensesViewModel.addExpense(expense) { success, newId -> //(Google Developers Training team, 2025)
                if (success) {
                    // Subtract expense from user's available budget (add to totalSpent)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(requireContext())
                        val budgetDao = db.budgetDao()

                        // Increase totalSpent by expense amount
                        budgetDao.addSpending(
                            (requireActivity() as MainActivityProvider).getCurrentUserId(),
                            expense.amount
                        )
                    }

                    Toast.makeText(
                        requireContext(),
                        "Expense added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    scheduleRecurringExpense(expense)
                    (requireActivity() as MainActivity).replaceFragment(
                        ExpenseDetailsFragment(
                            newId.toInt(),
                            source = "AddExpense"
                        ), addToBackStack = false)
                } else {
                    binding.tvWallet.error = "Insufficient funds in selected wallet"
                    Toast.makeText(
                        requireContext(),
                        "Insufficient funds in selected wallet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Clear focus when clicking outside the EditText
        binding.rootLayout.setOnClickListener {
            binding.etExpenseAmount.clearFocus()
            binding.etInputNote.clearFocus()
        }

        @Suppress("ClickableViewAccessibility")
        binding.scrollView.setOnTouchListener { v, _ ->
            binding.etExpenseAmount.clearFocus()
            binding.etInputNote.clearFocus()
            v.performClick()

            val imm =
                requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            // clear keyboard focus
            false
        }
    }

    // Open DatePickerBottomSheet and store last selected date
    private fun openDatePicker() {
        DatePickerBottomSheet(initialDate = selectedDatePicker) { year, month, day ->
            selectedDatePicker.set(year, month, day)
            binding.tvSelectedDate.apply {
                text = "$day/${month + 1}/$year"
                visibility = View.VISIBLE
            }
            selectedDate = selectedDatePicker
            binding.tvSelectDate.error = null
        }.show(parentFragmentManager, "datePicker")
    }

    // Camera helper functions
    private fun openCamera() {
        val photoFile = createImageFile() //(IIE-Vega, 2025)
        val photoURI: Uri = FileProvider.getUriForFile(  //(IIE-Vega, 2025)
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { //(IIE-Vega, 2025)
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        startActivityForResult(cameraIntent, picId) //(IIE-Vega, 2025)
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) //(IIE-Vega, 2025)
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) //(IIE-Vega, 2025)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath // save path for DB //(IIE-Vega, 2025)
        }
    }

    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            picId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) //(IIE-Vega, 2025)
        if (requestCode == picId && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == picId && resultCode == Activity.RESULT_OK) { //(IIE-Vega, 2025)
            // Mark that a photo exists
            binding.btnUploadPhoto.setImageResource(R.drawable.ic_photo_attached) //(IIE-Vega, 2025)
        }
    }

    private fun scheduleRecurringExpense(expense: Expense) {
        val TAG = "AddExpenseFragment"
        Log.d(
            TAG,
            "Scheduling recurring expense for: ${expense.walletId} | recurrence=${expense.recurrence}"
        )

        val interval = when (expense.recurrence) {
            "Daily" -> 1L
            "Weekly" -> 7L
            "Monthly" -> 30L
            "Yearly" -> 365L
            else -> {
                Log.d(TAG, "No recurrence selected. Skipping scheduling") // Never
                return
            }
        }

        val data = workDataOf( //(App Dev Insights, 2024)
            "amount" to expense.amount,
            "note" to expense.note,
            "walletId" to expense.walletId,
            "recurrence" to expense.recurrence,
            "userId" to expense.userId
        )

        val workRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>( //(App Dev Insights, 2024)
            interval, TimeUnit.DAYS
        )
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork( //(App Dev Insights, 2024)
            "recurring_expense_${expense.id}_${expense.recurrence}",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.d(TAG, "WorkManager job scheduled successfully with interval=$interval days")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}