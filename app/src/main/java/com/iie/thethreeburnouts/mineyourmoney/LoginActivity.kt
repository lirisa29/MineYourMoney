package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find buttons
        val btnCreateAccount = findViewById<Button>(R.id.btn_create_account)
        val btnLogin = findViewById<TextView>(R.id.btn_login)

        // Both buttons open fragment_auth_form
        btnCreateAccount.setOnClickListener { showAuthFragment() }
        btnLogin.setOnClickListener { showAuthFragment() }
    }

    private fun showAuthFragment() {
        // Replace the fragment container with AuthFormFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AuthFormFragment())
            .addToBackStack(null)
            .commit()

        // Hide the login screen and show fragment container
        findViewById<View>(R.id.login_root).visibility = View.GONE
        findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        // Check if there are fragments in the back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack() // Removes the top fragment

            // Show login screen again
            findViewById<View>(R.id.login_root).visibility = View.VISIBLE
            findViewById<View>(R.id.fragment_container).visibility = View.GONE
        } else {
            super.onBackPressed() // No fragments left, exit activity normally
        }
    }
}
