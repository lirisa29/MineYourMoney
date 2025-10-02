package com.iie.thethreeburnouts.mineyourmoney
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetDatePickerBinding
import java.util.Calendar

class DatePickerBottomSheet (private val initialDate: Calendar = Calendar.getInstance(),
private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDatePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDatePickerBinding.inflate(inflater, container, false)

        binding.datePicker.init(
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, day ->
            onDateSelected(year, month, day)
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