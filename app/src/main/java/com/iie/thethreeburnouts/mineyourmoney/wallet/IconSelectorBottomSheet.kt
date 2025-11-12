package com.iie.thethreeburnouts.mineyourmoney.wallet

import android.R
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetIconSelectorBinding

class IconSelectorBottomSheet(
    private val onIconSelected: (iconResId: Int, color: Int) -> Unit
) : BottomSheetDialogFragment() {//(Geeks for Geeks, 2025)

    private var _binding: BottomSheetIconSelectorBinding? = null//(Geeks for Geeks, 2025)
    private val binding get() = _binding!!
    // Store references to created buttons so we can update tint later
    private val iconButtons = mutableListOf<ImageButton>()
    val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private var defaultColour: Int = R.attr.textColorSecondary
    private var selectedColour: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetIconSelectorBinding.inflate(inflater, container, false) //(Geeks for Geeks, 2025)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val icons = listOf(
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_wallets,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_car,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_cook,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_electricity,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_fitness,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_fuel,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_home,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_medical,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_person_2,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_pet,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_plane,
            com.iie.thethreeburnouts.mineyourmoney.R.drawable.ic_shopping
        )

        val iconGrid = binding.iconGrid // use binding directly if ID matches

        // Dynamically add icons
        for (iconRes in icons) {
            val btn = ImageButton(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 64.dp
                    height = 64.dp
                    setMargins(12, 12, 12, 12)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setGravity(Gravity.CENTER)
                }
                setBackgroundResource(com.iie.thethreeburnouts.mineyourmoney.R.drawable.bg_icon_button)
                setImageResource(iconRes)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                setPadding(12, 12, 12, 12)
                tag = iconRes
                setColorFilter(defaultColour)
                contentDescription = "Wallet Icon"
            }

            // click listener
            btn.setOnClickListener {
                if (selectedColour == null) {
                    Toast.makeText(requireContext(), "Please select a colour first", Toast.LENGTH_SHORT).show()
                } else {
                    onIconSelected(iconRes, selectedColour!!)
                    dismiss()
                }
            }

            iconGrid.addView(btn)
            iconButtons.add(btn)
        }

        // Click listeners for colours
        val colourContainer = binding.root.findViewById<ViewGroup>(com.iie.thethreeburnouts.mineyourmoney.R.id.colour_list)
        for (i in 0 until colourContainer.childCount) {
            val colorView = colourContainer.getChildAt(i)
            colorView.setOnClickListener {
                selectedColour = colorView.backgroundTintList?.defaultColor ?: R.attr.textColorSecondary

                // Update all icons with selected colour
                iconButtons.forEach { icon ->
                    icon.setColorFilter(selectedColour!!)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            val bottomSheet = (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) //(Geeks for Geeks, 2025)
            val typedValue = TypedValue()
            val theme = requireContext().theme
            val resolved = theme.resolveAttribute(R.attr.colorBackground, typedValue, true)
            val backgroundColor = if (resolved) typedValue.data else Color.WHITE // fallback
            bottomSheet?.setBackgroundColor(backgroundColor) //(Geeks for Geeks, 2025)
            dialog.window?.navigationBarColor = backgroundColor

            // Allow scrollable BottomSheet
            val behavior = BottomSheetBehavior.from(bottomSheet!!) //(Geeks for Geeks, 2025)
            behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO //(Geeks for Geeks, 2025)
            behavior.isFitToContents = true
        }

        // Apply slide-up and slide-down animations
        dialog.window?.attributes?.windowAnimations = com.iie.thethreeburnouts.mineyourmoney.R.style.BottomSheetAnimation

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}