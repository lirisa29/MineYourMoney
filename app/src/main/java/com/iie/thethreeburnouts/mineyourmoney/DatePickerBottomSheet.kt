package com.iie.thethreeburnouts.mineyourmoney
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetDatePickerBinding
import java.util.Calendar

class DatePickerBottomSheet(
    private val initialDate: Calendar = Calendar.getInstance(),
    private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDatePickerBinding? = null //(GeeksforGeeks, 2025)
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DatePickerBottomSheet", "onCreateView called")
        _binding = BottomSheetDatePickerBinding.inflate(inflater, container, false) //(GeeksforGeeks, 2025)

        Log.d("DatePickerBottomSheet","Initializing DatePicker")

        // Initialize the DatePicker with the initial/current date
        binding.datePicker.init(
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH),
            null // no listener needed here; selection handled onDismiss
        )

        // Restrict max date to today
        binding.datePicker.maxDate = Calendar.getInstance().timeInMillis

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Call the callback with whatever date is currently selected
        val year = binding.datePicker.year
        val month = binding.datePicker.month
        val day = binding.datePicker.dayOfMonth
        Log.d("DatePickerBottomSheet", "Bottom sheet dismissed")
        onDateSelected(year, month, day)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("DatePickerBottomSheet", "onCreateDialog called")
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            Log.d("DatePickerBottomSheet", "Dialog shown")
            val bottomSheet =
                (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) //(GeeksforGeeks, 2025)
            val typedValue = TypedValue()
            val theme = requireContext().theme
            val resolved = theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val backgroundColor = if (resolved) typedValue.data else Color.WHITE
            bottomSheet?.setBackgroundColor(backgroundColor) //(GeeksforGeeks, 2025)
            dialog.window?.navigationBarColor = backgroundColor
        }

        // Apply slide-up and slide-down animations
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DatePickerBottomSheet", "onDestroyView called")
        _binding = null
    }
}
//REFERENCE LIST:
/* (Geeks for Geeks, 2025). Modal Bottom Sheet in Android with Examples. [Online].
Available at: https://www.geeksforgeeks.org/android/modal-bottom-sheet-in-android-with-examples/  [Accessed 5 October 2025). */
