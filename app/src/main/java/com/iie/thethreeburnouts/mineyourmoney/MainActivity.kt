package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.iie.thethreeburnouts.mineyourmoney.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Initialise ViewBinding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    override fun onBackPressed() {
        // Handle back press for fragments in back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}