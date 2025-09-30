package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.GridLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IconSelectorBottomSheet (private val onIconSelected: (iconResId: Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_icon_selector, container, false)
        val iconGrid = view.findViewById<GridLayout>(R.id.icon_grid)

        for (i in 0 until iconGrid.childCount) {
            val child = iconGrid.getChildAt(i)
            if (child is ImageButton) {
                child.setOnClickListener {
                    val iconResId = it.tag as? Int
                    if (iconResId != null) {
                        onIconSelected(iconResId)
                        dismiss()
                    }
                }
            }
        }

        return view
    }
}