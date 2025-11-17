package com.iie.thethreeburnouts.mineyourmoney.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), AuthFormFragment.AuthListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(this@LoginActivity).clearAll()
        }

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
                R.anim.slide_out_left,   // exit (if navigating to another fragment)
                R.anim.slide_in_right,    // popEnter (when returning to this fragment)
                R.anim.slide_out_left   // popExit (when popping this fragment)
            )
            .replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onAuthSuccess(user: User) {
        val intent = Intent(this, MainActivity::class.java).apply {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USER", user)
        }
        startActivity(intent)
        Log.e("LoginActivity","Authentication succeeded")
    }
}