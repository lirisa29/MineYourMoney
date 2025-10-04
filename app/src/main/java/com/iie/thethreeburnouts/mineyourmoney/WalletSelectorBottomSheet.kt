package com.iie.thethreeburnouts.mineyourmoney

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetWalletSelectorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletSelectorBottomSheet(
    private val onWalletSelected: (Wallet) -> Unit,
    private val preselectedWalletId: Int? = null
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWalletSelectorBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWalletSelectorBinding.inflate(inflater, container, false)

        val walletDao = AppDatabase.getInstance(requireContext()).walletDao()
        val userId = (requireActivity() as MainActivityProvider).getCurrentUserId()

        // Fetch wallets in a coroutine
        lifecycleScope.launch {
            val wallets = withContext(Dispatchers.IO) {
                walletDao.getAllWallets(userId) // suspend function
            }
            val preselectedPosition = preselectedWalletId?.let { id ->
                wallets.indexOfFirst { it.id == id }
            } ?: -1

            val walletAdapter = WalletSelectorAdapter(wallets,
                selectedPosition = if (preselectedPosition >= 0) preselectedPosition else RecyclerView.NO_POSITION)
            { selectedWallet ->
                onWalletSelected(selectedWallet)
                dismiss()
            }

            binding.walletRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = walletAdapter
            }
        }

        return binding.root
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
