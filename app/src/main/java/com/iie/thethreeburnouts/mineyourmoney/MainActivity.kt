package com.iie.thethreeburnouts.mineyourmoney

import User
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
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

        // Set up Navigation with BottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        // Use ViewBinding for BottomNavigationView
        binding.bottomNavigationView.setupWithNavController(navController)

        // Set status and nav bar colour using theme attribute
        val backgroundColor = TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, backgroundColor, true)

        window.statusBarColor = backgroundColor.data
        window.navigationBarColor = backgroundColor.data
    }

    override fun getCurrentUserId(): Int = loggedInUser.id
}