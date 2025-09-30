package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.Fragment

class CreateWalletFragment : Fragment(R.layout.fragment_create_wallet){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topBar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val btnSelectIcon = view.findViewById<View>(R.id.btn_select_icon)
        val iconImage = view.findViewById<ImageButton>(R.id.img_icon)
        val walletNameInput = view.findViewById<TextInputEditText>(R.id.et_wallet_name)
        val walletBalanceInput = view.findViewById<TextInputEditText>(R.id.et_initial_balance)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm)
    }
}