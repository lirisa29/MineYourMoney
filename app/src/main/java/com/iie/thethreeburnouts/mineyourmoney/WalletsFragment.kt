package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletsFragment : Fragment(R.layout.fragment_wallets) {

    private var currentSort: SortType = SortType.DEFAULT
    private lateinit var walletAdapter: WalletAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.walletsRecyclerView)
        searchView = view.findViewById(R.id.search_view)
        val btnCreateWallet = view.findViewById<View>(R.id.btn_create_wallet)
        val btnSort = view.findViewById<View>(R.id.btn_sort)
        val btnAddExpense = view.findViewById<View>(R.id.fab_add_expense)

        val walletDao = AppDatabase.getInstance(requireContext()).walletRepository()

        btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_walletsFragment_to_addExpenseFragment)
        }

        walletAdapter = WalletAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = walletAdapter

        // Navigate to create wallet fragment
        btnCreateWallet.setOnClickListener {
            findNavController().navigate(R.id.action_walletsFragment_to_createWalletFragment)
        }

        // Show sort options bottom sheet
        btnSort.setOnClickListener {
            SortOptionsBottomSheet(
                onSortSelected = { sortType ->
                    currentSort = sortType
                    loadWallets(walletDao) // reload wallets with new sort
                },
                currentSort = currentSort
            ).show(parentFragmentManager, "SortOptionsBottomSheet")
        }

        // Setup search view listener
        searchView.apply {
            setIconified(true)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    handleSearch(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    handleSearch(newText.orEmpty())
                    return true
                }
            })
        }

        view.setOnClickListener { searchView.clearFocus() }

        @Suppress("ClickableViewAccessibility")
        recyclerView.setOnTouchListener { v, _ ->
            searchView.clearFocus()
            v.performClick()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        val walletDao = AppDatabase.getInstance(requireContext()).walletRepository()
        loadWallets(walletDao)
    }

    private fun loadWallets(walletDao: WalletRepository) {
        lifecycleScope.launch {
            val wallets = withContext(Dispatchers.IO) {
                walletDao.getSortedWallets(currentSort)
            }
            updateWallets(wallets)
        }
    }

    private fun handleSearch(query: String) {
        val walletDao = AppDatabase.getInstance(requireContext()).walletRepository()
        lifecycleScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                if (query.isBlank()) {
                    walletDao.getSortedWallets(currentSort)
                } else {
                    walletDao.getAllWallets().filter { it.name.contains(query, ignoreCase = true) }
                }
            }

            if (query.isNotBlank() && filtered.isEmpty()) {
                Toast.makeText(context, "No wallets found", Toast.LENGTH_SHORT).show()
            }
            updateWallets(filtered)
        }
    }

    private fun updateWallets(list: List<Wallet>) {
        walletAdapter.updateList(list)
    }
}
