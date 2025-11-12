package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentMiningBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.RecurrenceSelectorBottomSheet
import com.iie.thethreeburnouts.mineyourmoney.expense.WalletSelectorBottomSheet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment

class CrystalsFragment : Fragment() {

    private var _binding: FragmentMiningBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        }
}