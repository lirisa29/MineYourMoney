package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import android.widget.SearchView
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

        recyclerView = view.findViewById(R.id.walletsRecyclerView)
        searchView = view.findViewById(R.id.search_view)
        val btnCreateWallet = view.findViewById<View>(R.id.btn_create_wallet)
        val btnSort = view.findViewById<View>(R.id.btn_sort)

        // Initialize adapter with full wallet list
        walletAdapter = WalletAdapter(WalletRepository.getWallets())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = walletAdapter

        // Navigate to create wallet fragment
        btnCreateWallet.setOnClickListener {
            findNavController().navigate(R.id.action_walletsFragment_to_createWalletFragment)
        }

        // Show sort options bottom sheet
        btnSort.setOnClickListener {
            val sortSheet = SortOptionsBottomSheet(
                onSortSelected = { sortType ->
                    currentSort = sortType // update the current sort
                    val sortedWallets = WalletRepository.getSortedWallets(sortType)
                    walletAdapter.updateList(sortedWallets)
                },
                currentSort = currentSort // pass the current one
            )
            sortSheet.show(parentFragmentManager, "SortOptionsBottomSheet")
        }

        // Setup search view to filter wallets by name
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                walletAdapter.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                walletAdapter.filter(newText ?: "")
                return true
            }
        })
    }
}