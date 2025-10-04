package com.iie.thethreeburnouts.mineyourmoney

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetIconSelectorBinding


class IconSelectorBottomSheet(
    private val onIconSelected: (iconResId: Int, color: Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetIconSelectorBinding? = null
    private val binding get() = _binding!!
    // Store references to created buttons so we can update tint later
    private val iconButtons = mutableListOf<ImageButton>()
    val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private var defaultColour: Int = android.R.attr.textColorSecondary
    private var selectedColour: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetIconSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val icons = listOf(
            R.drawable.ic_wallets,
            R.drawable.car,
            R.drawable.cook,
            R.drawable.electricity,
            R.drawable.fitness,
            R.drawable.fuel,
            R.drawable.home,
            R.drawable.medical,
            R.drawable.person__2_,
            R.drawable.pet,
            R.drawable.plane,
            R.drawable.shopping
        )

        val iconGrid = binding.iconGrid // use binding directly if ID matches

        // Dynamically add icons
        for (iconRes in icons) {
            val btn = ImageButton(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(64.dp, 64.dp).apply {
                    setMargins(12, 12, 12, 12)
                }
                setBackgroundResource(R.drawable.bg_icon_button)
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
        val colourContainer = binding.root.findViewById<ViewGroup>(R.id.colour_list)
        for (i in 0 until colourContainer.childCount) {
            val colorView = colourContainer.getChildAt(i)
            colorView.setOnClickListener {
                selectedColour = colorView.backgroundTintList?.defaultColor ?: android.R.attr.textColorSecondary

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