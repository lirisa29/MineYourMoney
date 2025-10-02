package com.iie.thethreeburnouts.mineyourmoney

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iie.thethreeburnouts.mineyourmoney.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), AuthFormFragment.AuthListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCreateAccount.setOnClickListener { showAuthFragment(false) }
        binding.btnLogin.setOnClickListener { showAuthFragment(true) }
    }

    private fun showAuthFragment(isLogin: Boolean) {
        val fragment = AuthFormFragment().apply {
            arguments = Bundle().apply { putBoolean("IS_LOGIN", isLogin) }
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,   // enter
                R.anim.slide_out_right,   // exit (if navigating to another fragment)
                R.anim.slide_in_right,    // popEnter (when returning to this fragment)
                R.anim.slide_out_right   // popExit (when popping this fragment)
            )
            .replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onAuthSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
