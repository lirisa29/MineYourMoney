package com.iie.thethreeburnouts.mineyourmoney

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.work.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar? = null
    private var selectedWalletId: Int? = null
    private var selectedDatePicker: Calendar = Calendar.getInstance()// stores last selected date
    private val picId = 123
    private var currentPhotoPath: String? = null  // <-- store absolute path of captured image
    private val expensesViewModel: ExpensesViewModel by activityViewModels {
        ExpensesViewModelFactory(
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
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        // Clear error when typing amount
        binding.etExpenseAmount.addTextChangedListener(object : android.text.TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) {
                    binding.etExpenseAmount.error = null
                    binding.expenseAmountLayout.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != current) {
                    binding.etExpenseAmount.removeTextChangedListener(this)

                    // Remove all non-digit characters
                    val cleanString = s.toString().replace("[R,.\\s]".toRegex(), "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0
                    val formatted = "R${String.format("%,.2f", parsed / 100)}"

                    current = formatted
                    binding.etExpenseAmount.setText(formatted)
                    binding.etExpenseAmount.setSelection(formatted.length)

                    binding.etExpenseAmount.addTextChangedListener(this)
                }
            }
        })

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            val amount = binding.etExpenseAmount.text.toString()
            val note = binding.etInputNote.text.toString()
            val cleanedBalance = amount.replace("[^\\d.]".toRegex(), "")
            val balance = cleanedBalance.toDouble()

            if (balance <= 0.0) {
                binding.expenseAmountLayout.error = "Please enter an amount"
                return@setOnClickListener
            }
            if (selectedWallet == null) {
                binding.tvWallet.error = "Please select a wallet"
                return@setOnClickListener
            }
            if (selectedDate == null) {
                binding.tvSelectDate.error = "Please select a date"
                return@setOnClickListener
            }
            if (selectedRecurrence == null) {
                binding.tvSelectRecurrence.error = "Please select recurrence"
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

            expensesViewModel.addExpense(expense) { success, newId ->
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Expense added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    scheduleRecurringExpense(expense)
                    (requireActivity() as MainActivity).replaceFragment(ExpenseDetailsFragment(newId.toInt(), source = "AddExpense"), addToBackStack = false)
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
        val photoFile = createImageFile()
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        startActivityForResult(cameraIntent, picId)
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath // save path for DB
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            picId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == picId && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == picId && resultCode == Activity.RESULT_OK) {
            // Mark that a photo exists
            binding.btnUploadPhoto.setImageResource(R.drawable.ic_photo_attached)
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

        val data = workDataOf(
            "amount" to expense.amount,
            "note" to expense.note,
            "walletId" to expense.walletId,
            "recurrence" to expense.recurrence,
            "userId" to expense.userId
        )

        val workRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
            interval, java.util.concurrent.TimeUnit.DAYS
        )
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
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