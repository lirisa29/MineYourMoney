package com.iie.thethreeburnouts.mineyourmoney

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetRecurrenceSelectorBinding

class RecurrenceSelectorBottomSheet (
    private val onRecurrenceSelected: (String) -> Unit,
    private val currentRecurrence: String? = null
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRecurrenceSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRecurrenceSelectorBinding.inflate(inflater, container, false)

        when (currentRecurrence) {
            "Never" -> binding.btnRecurrenceNever.isChecked = true
            "Daily" -> binding.btnRecurrenceDaily.isChecked = true
            "Weekly" -> binding.btnRecurrenceWeekly.isChecked = true
            "Monthly" -> binding.btnRecurrenceMonthly.isChecked = true
            "Yearly" -> binding.btnRecurrenceYearly.isChecked = true
        }
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val typedValue = TypedValue()
            val theme = requireContext().theme
            val resolved = theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val backgroundColor = if (resolved) typedValue.data else Color.WHITE // fallback
            bottomSheet?.setBackgroundColor(backgroundColor)
            dialog.window?.navigationBarColor = backgroundColor
        }

        // Apply slide-up and slide-down animations
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}