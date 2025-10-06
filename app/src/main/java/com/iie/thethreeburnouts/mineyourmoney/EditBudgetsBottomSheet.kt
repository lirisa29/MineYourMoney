package com.iie.thethreeburnouts.mineyourmoney

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetEditBudgetBinding

class EditBudgetsBottomSheet(
    private val currentLimit: Double,
    private val onLimitChanged: (Double) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditBudgetBinding? = null
    private val binding get() = _binding!!
    private var current = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetEditBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial formatted text
        val formattedStart = "R" + String.format("%,.2f", currentLimit)
        binding.etBudgetAmount.setText(formattedStart) //(GeeksforGeeks, 2025)
        current = formattedStart

        // Format text as currency while typing
        binding.etBudgetAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString() //(GeeksforGeeks, 2025)
                if (newText != current) { //(GeeksforGeeks, 2025)
                    binding.etBudgetAmount.removeTextChangedListener(this)

                    // Remove everything except digits
                    val cleanString = newText.replace("[R,.\\s]".toRegex(), "") //(GeeksforGeeks, 2025)
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0

                    // Format as currency (R#,###.##)
                    val formatted = "R" + String.format("%,.2f", parsed / 100)

                    current = formatted
                    binding.etBudgetAmount.setText(formatted) //(GeeksforGeeks, 2025)
                    binding.etBudgetAmount.setSelection(formatted.length)

                    binding.etBudgetAmount.addTextChangedListener(this)
                }
            }
        })

        // Handle save button click
        binding.btnSave.setOnClickListener {
            hideKeyboard()
            saveAndDismiss()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etBudgetAmount.windowToken, 0)
    }

    private fun saveAndDismiss() {
        val text = binding.etBudgetAmount.text.toString().replace("[R,\\s]".toRegex(), "") //(GeeksforGeeks, 2025)
        val newLimit = text.toDoubleOrNull() ?: currentLimit
        onLimitChanged(newLimit)
        dismiss()
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

            // Allow scrollable BottomSheet
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
            behavior.peekHeight = com.google.android.material.bottomsheet.BottomSheetBehavior.PEEK_HEIGHT_AUTO
            behavior.isFitToContents = true
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
//Reference List:
/* Geeks for Geeks. 2025. Modal Bottom Sheet in Android with Examples. [Online].
Available at: https://www.geeksforgeeks.org/android/modal-bottom-sheet-in-android-with-examples/  [Accessed 5 October 2025). */