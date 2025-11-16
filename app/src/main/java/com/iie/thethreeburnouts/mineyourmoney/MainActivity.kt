package com.iie.thethreeburnouts.mineyourmoney

import com.iie.thethreeburnouts.mineyourmoney.login.User
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationBarView
import com.iie.thethreeburnouts.mineyourmoney.budget.Budget
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetRepository
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetsFragment
import com.iie.thethreeburnouts.mineyourmoney.crystals.CrystalsFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.ActivityMainBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseRepository
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletRepository
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MainActivityProvider {
    private lateinit var binding: ActivityMainBinding

    lateinit var loggedInUser: User
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loggedInUser = intent.getParcelableExtra("USER")
            ?: throw IllegalStateException("com.iie.thethreeburnouts.mineyourmoney.login.User must be passed to MainActivity")

        // ---- Room/Firestore Sync on Login ----
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@MainActivity)
            val repo = BudgetRepository(db.budgetDao())
            val walletRepo = WalletRepository(db.walletDao())
            val expensesRepo = ExpenseRepository(db.expensesDao())

            // 1. Try to pull latest Firestore version
            repo.downloadFromFirestore(loggedInUser.id)
            walletRepo.downloadFromFirestore(loggedInUser.id)
            expensesRepo.downloadExpenses(loggedInUser.id)
        }

        if (savedInstanceState == null){
            replaceFragment(BudgetsFragment(), addToBackStack = false)
            binding.bottomNavigationView.selectedItemId = R.id.nav_budgets
        }

        // Set status and nav bar colour using theme attribute
        val backgroundColor = TypedValue()
        theme.resolveAttribute(android.R.attr.colorBackground, backgroundColor, true)
        window.statusBarColor = backgroundColor.data
        window.navigationBarColor = backgroundColor.data
    }

    /**
     * Replace fragment in the main container.
     * This no longer touches navigation listeners; it only commits fragment.
     */
    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)

        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()

        // only update visibility (do not reassign listeners)
        updateNavBarVisibility(fragment)
    }

    /**
     * Setup the bottom navigation listener ONCE.
     * When a nav item is selected we call replaceFragment.
     */
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(
            NavigationBarView.OnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_budgets -> {
                        replaceFragment(BudgetsFragment(), addToBackStack = false)
                        true
                    }
                    R.id.nav_wallets -> {
                        replaceFragment(WalletsFragment(), addToBackStack = false)
                        true
                    }
                    R.id.nav_crystals -> {
                        replaceFragment(CrystalsFragment(), addToBackStack = false)
                        true
                    }
                    else -> false
                }
            }
        )
    }

    /**
     * Only control visibility here — do NOT change listeners.
     */
    public fun updateNavBarVisibility(fragment: Fragment) {
        binding.bottomNavigationView.visibility =
            if (fragment is WalletsFragment || fragment is BudgetsFragment || fragment is CrystalsFragment) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun getCurrentUserId(): Int = loggedInUser.id
}
