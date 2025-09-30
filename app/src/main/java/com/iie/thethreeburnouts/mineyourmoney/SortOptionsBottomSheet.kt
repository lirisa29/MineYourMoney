package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
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
}