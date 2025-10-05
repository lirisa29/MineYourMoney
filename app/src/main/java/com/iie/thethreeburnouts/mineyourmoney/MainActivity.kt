package com.iie.thethreeburnouts.mineyourmoney

import User
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.savedstate.serialization.saved
import com.iie.thethreeburnouts.mineyourmoney.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainActivityProvider {
    // Initialise ViewBinding
    private lateinit var binding: ActivityMainBinding

    // Store the logged-in user
    lateinit var loggedInUser: User
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve logged-in user from intent
        loggedInUser = intent.getParcelableExtra("USER")
            ?: throw IllegalStateException("User must be passed to MainActivity")

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, WalletsFragment())
                .commit()
        }

        // Set status and nav bar colour using theme attribute
        val backgroundColor = TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, backgroundColor, true)

        window.statusBarColor = backgroundColor.data
        window.navigationBarColor = backgroundColor.data
        Log.e("MainActivity", "Created")
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)

        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()

        // Update nav bar visibility
        updateNavBarVisibility(fragment)
    }

    private fun updateNavBarVisibility(fragment: Fragment) {
        binding.bottomNavigationView.visibility =
            if (fragment is WalletsFragment) View.VISIBLE else View.GONE
    }

    override fun getCurrentUserId(): Int = loggedInUser.id
}