package com.iie.thethreeburnouts.mineyourmoney

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

enum class SortType {
    DEFAULT,       // A-Z
    BALANCE_HIGH,  // Highest to Lowest balance
    BALANCE_LOW    // Lowest to Highest balance
}

class SortOptionsBottomSheet (private val onSortSelected: (SortType) -> Unit,
private val currentSort: SortType
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_sort_options, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultBtn = view.findViewById<RadioButton>(R.id.btn_sort_default)
        val highBalanceBtn = view.findViewById<RadioButton>(R.id.btn_sort_balance_high)
        val lowBalanceBtn = view.findViewById<RadioButton>(R.id.btn_sort_balance_low)

        when (currentSort) {
            SortType.DEFAULT -> defaultBtn.isChecked = true
            SortType.BALANCE_HIGH -> highBalanceBtn.isChecked = true
            SortType.BALANCE_LOW -> lowBalanceBtn.isChecked = true
        }

        defaultBtn.setOnClickListener {
            onSortSelected(SortType.DEFAULT)
            dismiss()
        }

        highBalanceBtn.setOnClickListener {
            onSortSelected(SortType.BALANCE_HIGH)
            dismiss()
        }

        lowBalanceBtn.setOnClickListener {
            onSortSelected(SortType.BALANCE_LOW)
            dismiss()
        }
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
        return dialog
    }
}