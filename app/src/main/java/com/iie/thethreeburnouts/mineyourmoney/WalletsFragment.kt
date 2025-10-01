package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WalletsFragment : Fragment(R.layout.fragment_wallets) {

    private var currentSort: SortType = SortType.DEFAULT
    private lateinit var walletAdapter: WalletAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.walletsRecyclerView)
        searchView = view.findViewById(R.id.search_view)
        val btnCreateWallet = view.findViewById<View>(R.id.btn_create_wallet)
        val btnSort = view.findViewById<View>(R.id.btn_sort)

        // Initialize adapter with full wallet list
        walletAdapter = WalletAdapter(WalletRepository.getWallets()).also {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = it
        }

        // Navigate to create wallet fragment
        btnCreateWallet.setOnClickListener {
            findNavController().navigate(R.id.action_walletsFragment_to_createWalletFragment)
        }

        // Show sort options bottom sheet
        btnSort.setOnClickListener {
            SortOptionsBottomSheet(
                onSortSelected = { sortType ->
                    currentSort = sortType
                    updateWallets(WalletRepository.getSortedWallets(sortType))
                },
                currentSort = currentSort
            ).show(parentFragmentManager, "SortOptionsBottomSheet")
        }

        // Setup search view listener
        searchView.apply {
            setIconified(true) // collapse on start
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

        // Clear focus when tapping outside
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
        updateWallets(WalletRepository.getSortedWallets(currentSort))
    }

    private fun handleSearch(query: String) {
        walletAdapter.filter(query)?.let { filtered ->
            if (filtered.isEmpty()) {
                Toast.makeText(context, "No wallets found", Toast.LENGTH_SHORT).show()
            }
            updateWallets(filtered)
        } ?: run {
            // Query empty → restore current sort
            updateWallets(WalletRepository.getSortedWallets(currentSort))
        }
    }

    private fun updateWallets(list: List<Wallet>) {
        walletAdapter.updateList(list)
    }
}