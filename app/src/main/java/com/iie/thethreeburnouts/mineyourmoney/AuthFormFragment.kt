package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class AuthFormFragment : Fragment(R.layout.fragment_auth_form) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find your top app bar by ID
        val btnBack = view.findViewById<View>(R.id.top_app_bar)

        // Set click listener
        btnBack.setOnClickListener {
            // Call the activity’s onBackPressed
            requireActivity().onBackPressed()
        }
    }
}
