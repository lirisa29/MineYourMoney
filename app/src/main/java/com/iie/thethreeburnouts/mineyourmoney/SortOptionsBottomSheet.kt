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
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetSortOptionsBinding

enum class SortType {
    DEFAULT,       // A-Z
    BALANCE_HIGH,  // Highest to Lowest balance
    BALANCE_LOW    // Lowest to Highest balance
}

class SortOptionsBottomSheet (private val onSortSelected: (SortType) -> Unit,
private val currentSort: SortType
) : BottomSheetDialogFragment() { //(Geeks for Geeks, 2025)

    private var _binding: BottomSheetSortOptionsBinding? = null //(Geeks for Geeks, 2025)
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSortOptionsBinding.inflate(inflater, container, false) //(Geeks for Geeks, 2025)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the correct radio button based on currentSort
        when (currentSort) {
            SortType.DEFAULT -> binding.btnSortDefault.isChecked = true
            SortType.BALANCE_HIGH -> binding.btnSortBalanceHigh.isChecked = true
            SortType.BALANCE_LOW -> binding.btnSortBalanceLow.isChecked = true
        }

        // Set click listeners for each radio button
        binding.btnSortDefault.setOnClickListener {
            onSortSelected(SortType.DEFAULT)
            dismiss()
        }

        binding.btnSortBalanceHigh.setOnClickListener {
            onSortSelected(SortType.BALANCE_HIGH)
            dismiss()
        }

        binding.btnSortBalanceLow.setOnClickListener {
            onSortSelected(SortType.BALANCE_LOW)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) //(Geeks for Geeks, 2025)
            val typedValue = TypedValue()
            val theme = requireContext().theme
            val resolved = theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val backgroundColor = if (resolved) typedValue.data else Color.WHITE // fallback
            bottomSheet?.setBackgroundColor(backgroundColor) //(Geeks for Geeks, 2025)
            dialog.window?.navigationBarColor = backgroundColor
        }

        // Add slide up/down animations
        dialog.window?.attributes?.windowAnimations = R.style.BottomSheetAnimation

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//REFERENCE LIST:
/* (Geeks for Geeks, 2025). Modal Bottom Sheet in Android with Examples. [Online].
Available at: https://www.geeksforgeeks.org/android/modal-bottom-sheet-in-android-with-examples/  [Accessed 5 October 2025). */