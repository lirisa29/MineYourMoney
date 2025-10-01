package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.BottomSheetWalletSelectorBinding

class WalletSelectorBottomSheet (private val onWalletSelected: (Wallet) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWalletSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWalletSelectorBinding.inflate(inflater, container, false)

        val walletAdapter = WalletSelectorAdapter(WalletRepository.getWallets()) { selectedWallet ->
            onWalletSelected(selectedWallet)
            dismiss()
        }

        binding.walletRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = walletAdapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}