package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import java.util.Calendar
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar? = null
    //private var selectedDatePicker = Calendar.getInstance()
    private val picId = 123

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

        binding.btnWalletDropdown.setOnClickListener {
            WalletSelectorBottomSheet { wallet ->
                selectedWallet = wallet
                binding.tvSelectedWallet.apply {
                    text = wallet.name
                    visibility = View.VISIBLE
                }
                binding.tvWallet.error = null
            }.show(childFragmentManager, "WalletSelector")
        }

        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet { recurrence ->
                selectedRecurrence = recurrence
                binding.tvSelectedRecurrence.apply {
                    text = recurrence
                    visibility = View.VISIBLE
                }
                binding.tvSelectRecurrence.error = null  // clear error once picked
            }.show(childFragmentManager, "RecurrenceSelector")
        }

        binding.btnDateDropdown.setOnClickListener {
            DatePickerBottomSheet(Calendar.getInstance()) { year, month, day -> //issue is here
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                binding.tvSelectedDate.apply {
                    text = "$day/${month + 1}/$year"
                    visibility = View.VISIBLE
                }
                binding.tvSelectDate.error = null // clear error once picked
            }.show(childFragmentManager, "DatePicker")
        }

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

        // clears error once user types amount
        binding.etExpenseAmount.addTextChangedListener { editable ->
            if (!editable.isNullOrBlank()) {
                binding.expenseAmountLayout.error = null
                binding.expenseAmountLayout.isErrorEnabled = false
            }
        }

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
            // Save expense (not implemented yet)
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, picId)
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
            val photo = data?.extras?.get("data") as Bitmap?
            photo?.let {
                //binding.imgSavedPhoto.setImageBitmap(it)
            }
        }
    }
}
