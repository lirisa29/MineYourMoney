package com.iie.thethreeburnouts.mineyourmoney

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import java.util.Calendar

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()



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

        val cameraProviderResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                var bitmap = it.data!!.extras?.get("data") as Bitmap
                //binding.img.setImageBitmap(bitmap)
            }
        }
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
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraProviderResult.launch(intent)
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
}
