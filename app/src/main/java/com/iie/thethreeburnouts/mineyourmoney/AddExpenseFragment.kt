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

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()
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

        // ref the module manual for this
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
            }.show(childFragmentManager, "WalletSelector")
        }

        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet { recurrence ->
                selectedRecurrence = recurrence
                binding.tvSelectedRecurrence.apply {
                    text = recurrence
                    visibility = View.VISIBLE
                }
            }.show(childFragmentManager, "RecurrenceSelector")
        }

        binding.btnDateDropdown.setOnClickListener {
            DatePickerBottomSheet(selectedDate) { year, month, day ->
                selectedDate.set(year, month, day)
                binding.tvSelectedDate.apply {
                    text = "$day/${month + 1}/$year"
                    visibility = View.VISIBLE
                }
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

        // ref the module manual for this

        binding.btnConfirm.setOnClickListener {
            // Handle saving the expense
            val amount = binding.etExpenseAmount.text.toString()
            val note = binding.etInputNote.text.toString()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, picId)
    }

    // Request CAMERA permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            picId
        )
    }

    // Handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == picId && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // Permission denied - Inform the user
            //binding.imgSavedPhoto.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    // Handle the captured image
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
