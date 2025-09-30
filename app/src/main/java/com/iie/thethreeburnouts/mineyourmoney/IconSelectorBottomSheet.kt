package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.GridLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IconSelectorBottomSheet(
    private val onIconSelected: (iconResId: Int) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_icon_selector, container, false)
        val iconGrid = view.findViewById<GridLayout>(R.id.icon_grid)

        // Map each button ID to its drawable resource
        val icons = listOf(
            R.id.img_icon1 to R.drawable.ic_wallets
            // Add more icons here like:
            // R.id.img_icon2 to R.drawable.ic_other_icon,
            // R.id.img_icon3 to R.drawable.ic_another_icon
        )

        for ((btnId, drawableRes) in icons) {
            val btn = view.findViewById<ImageButton>(btnId)
            btn.tag = drawableRes
            btn.setOnClickListener {
                val iconResId = it.tag as Int
                onIconSelected(iconResId)
                dismiss()
            }
        }

        return view
    }
}