package com.iie.thethreeburnouts.mineyourmoney

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
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
    private var selectedDatePicker: Calendar = Calendar.getInstance()// stores last selected date
    private val picId = 123
    private var currentPhotoPath: String? = null  // <-- store absolute path of captured image
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

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

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Wallet selection
        binding.btnWalletDropdown.setOnClickListener {
            WalletSelectorBottomSheet { wallet ->
                selectedWallet = wallet
                binding.tvSelectedWallet.apply {
                    text = wallet.name
                    visibility = View.VISIBLE
                }
                binding.imgWalletIcon.setImageResource(wallet.iconResId)
                binding.tvWallet.error = null
            }.show(childFragmentManager, "WalletSelector")
        }

        // Recurrence selection
        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet { recurrence ->
                selectedRecurrence = recurrence
                binding.tvSelectedRecurrence.apply {
                    text = recurrence
                    visibility = View.VISIBLE
                }
                binding.tvSelectRecurrence.error = null
            }.show(childFragmentManager, "RecurrenceSelector")
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
        binding.etExpenseAmount.addTextChangedListener { editable ->
            if (!editable.isNullOrBlank()) {
                binding.expenseAmountLayout.error = null
                binding.expenseAmountLayout.isErrorEnabled = false
            }
        }

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            val amount = binding.etExpenseAmount.text.toString()
            val note = binding.etInputNote.text.toString()

            if (amount.isBlank()) {
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
                amount = amount.toDouble(),
                note = note,
                walletId = selectedWallet!!.id,          // only save ID
                recurrence = selectedRecurrence,
                date = selectedDate!!.timeInMillis,      // store millis
                photoPath = currentPhotoPath             // store path as String
            )

            // Save to Dao
            expensesViewModel.addExpense(expense)

            // Save expense (not implemented yet)
            requireActivity().onBackPressed()
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
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}