package com.iie.thethreeburnouts.mineyourmoney

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity(), AuthFormFragment.AuthListener {

    private lateinit var fragmentContainer: View
    private lateinit var loginRoot: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize views safely
        loginRoot = findViewById(R.id.login_root)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(loginRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_create_account)?.setOnClickListener { showAuthFragment(false) }
        findViewById<TextView>(R.id.btn_login)?.setOnClickListener { showAuthFragment(true) }
    }

    private fun showAuthFragment(isLogin: Boolean) {
        val fragment = AuthFormFragment().apply {
            arguments = Bundle().apply { putBoolean("IS_LOGIN", isLogin) }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()

        loginRoot.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            loginRoot.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    override fun onAuthSuccess() {
        // Launch MainActivity safely with cleared back stack
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // finish() not needed because flags already clear stack
    }
}
