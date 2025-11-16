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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.crystals.StreakManager
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment
import kotlinx.coroutines.launch
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
    private var selectedDatePicker = Calendar.getInstance()

    private var currentPhotoPath: String? = null
    private val picId = 123

    private val expensesViewModel: ExpensesViewModel by activityViewModels {
        ExpensesViewModelFactory(
            requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        setupAppBar()

        binding.etExpenseAmount.setText("R0.00")
        binding.etExpenseAmount.setSelection(binding.etExpenseAmount.text!!.length)

        setupAmountFormatter()
        setupWalletSelector()
        setupRecurrenceSelector()
        setupDatePicker()
        setupPhotoPicker()

        binding.btnConfirm.setOnClickListener { handleConfirm() }
    }


    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity)
                .replaceFragment(WalletsFragment(), addToBackStack = false)
        }
    }

    private fun setupWalletSelector() {
        binding.btnWalletDropdown.setOnClickListener {
            WalletSelectorBottomSheet(
                onWalletSelected = { wallet ->
                    selectedWallet = wallet
                    binding.tvSelectedWallet.text = wallet.name
                    binding.tvSelectedWallet.visibility = View.VISIBLE

                    binding.imgWalletIcon.setImageResource(wallet.iconResId)
                    binding.imgWalletIcon.imageTintList = ColorStateList.valueOf(wallet.color)

                    binding.tvWallet.error = null
                }
            ).show(childFragmentManager, "WalletSelector")
        }
    }

    private fun setupRecurrenceSelector() {
        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet(
                currentRecurrence = selectedRecurrence,
                onRecurrenceSelected = { r ->
                    selectedRecurrence = r
                    binding.tvSelectedRecurrence.text = r
                    binding.tvSelectedRecurrence.visibility = View.VISIBLE
                    binding.tvSelectRecurrence.error = null
                }
            ).show(childFragmentManager, "RecurrenceSelector")
        }
    }

    private fun setupDatePicker() {
        binding.btnDateDropdown.setOnClickListener {
            DatePickerBottomSheet(selectedDatePicker) { y, m, d ->
                selectedDatePicker.set(y, m, d)
                selectedDate = selectedDatePicker

                binding.tvSelectedDate.visibility = View.VISIBLE
                binding.tvSelectedDate.text = "$d/${m + 1}/$y"
                binding.tvSelectDate.error = null
            }.show(parentFragmentManager, "datePicker")
        }
    }

    private fun setupPhotoPicker() {
        binding.btnUploadPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), picId)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        startActivityForResult(intent, picId)
    }

    private fun createImageFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_${stamp}_", ".jpg", dir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)

        if (req == picId && res == Activity.RESULT_OK) {

            // ✔️ Update existing icon only — NO preview image
            binding.imgUploadPhoto.setImageResource(R.drawable.ic_photo_attached)

            Toast.makeText(requireContext(), "Photo attached!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAmountFormatter() {
        var current = ""

        binding.etExpenseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == current) return

                binding.etExpenseAmount.removeTextChangedListener(this)

                val clean = s.toString().replace("[R,.\\s]".toRegex(), "")
                val parsed = clean.toDoubleOrNull() ?: 0.0
                val formatted = "R${String.format("%,.2f", parsed / 100)}"

                current = formatted
                binding.etExpenseAmount.setText(formatted)
                binding.etExpenseAmount.setSelection(formatted.length)

                binding.etExpenseAmount.addTextChangedListener(this)
            }
        })
    }

    private fun handleConfirm() {
        val amount = binding.etExpenseAmount.text.toString()
            .replace("[^\\d.]".toRegex(), "")
            .toDoubleOrNull() ?: 0.0

        if (amount <= 0.0) {
            binding.expenseAmountLayout.error = "Enter amount"
            return
        }
        if (selectedWallet == null) {
            binding.tvWallet.error = "Select wallet"
            return
        }
        if (selectedDate == null) {
            binding.tvSelectDate.error = "Select date"
            return
        }
        if (selectedRecurrence == null) {
            binding.tvSelectRecurrence.error = "Select recurrence"
            return
        }

        val expense = Expense(
            amount = amount,
            note = binding.etInputNote.text.toString(),
            walletId = selectedWallet!!.id,
            recurrence = selectedRecurrence,
            date = selectedDate!!.timeInMillis,
            photoPath = currentPhotoPath,
            userId = 0
        )

        expensesViewModel.addExpense(expense) { success, newId ->
            if (success) {

                StreakManager.updateStreak(requireContext())

                Toast.makeText(requireContext(), "+1 Swing earned!", Toast.LENGTH_SHORT).show()
                Toast.makeText(requireContext(), "Expense added!", Toast.LENGTH_SHORT).show()

                scheduleRecurringExpense(expense)

                (requireActivity() as MainActivity).replaceFragment(
                    ExpenseDetailsFragment(newId.toInt(), "AddExpense"),
                    addToBackStack = false
                )

            } else {
                binding.tvWallet.error = "Insufficient funds"
                Toast.makeText(requireContext(), "Insufficient funds", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleRecurringExpense(expense: Expense) {
        val interval = when (expense.recurrence) {
            "Daily" -> 1L
            "Weekly" -> 7L
            "Monthly" -> 30L
            "Yearly" -> 365L
            else -> return
        }

        val data = workDataOf(
            "amount" to expense.amount,
            "note" to expense.note,
            "walletId" to expense.walletId,
            "recurrence" to expense.recurrence,
            "userId" to expense.userId
        )

        val req = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
            interval, TimeUnit.DAYS
        )
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "recurring_expense_${expense.id}_${expense.recurrence}",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
