package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetRecurrenceSelectorBinding

class RecurrenceSelectorBottomSheet (
    private val onRecurrenceSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRecurrenceSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRecurrenceSelectorBinding.inflate(inflater, container, false)

        binding.btnRecurrenceNever.setOnClickListener {
            onRecurrenceSelected("Never")
            dismiss()
        }

        binding.btnRecurrenceDaily.setOnClickListener {
            onRecurrenceSelected("Daily")
            dismiss()
        }

        binding.btnRecurrenceWeekly.setOnClickListener {
            onRecurrenceSelected("Weekly")
            dismiss()
        }

        binding.btnRecurrenceMonthly.setOnClickListener {
            onRecurrenceSelected("Monthly")
            dismiss()
        }

        binding.btnRecurrenceYearly.setOnClickListener {
            onRecurrenceSelected("Yearly")
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}