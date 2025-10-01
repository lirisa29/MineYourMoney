package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.topAppBar.setOnClickListener {

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
            // To be implemented
        }

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
