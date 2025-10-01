package com.iie.thethreeburnouts.mineyourmoney
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}