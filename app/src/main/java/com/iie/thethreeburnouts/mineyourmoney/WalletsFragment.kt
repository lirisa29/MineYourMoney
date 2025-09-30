package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

class WalletsFragment : Fragment(R.layout.fragment_wallets) {
    private var currentSort: SortType = SortType.DEFAULT
    private lateinit var walletAdapter: WalletAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val recyclerView = view.findViewById<RecyclerView>(R.id.walletsRecyclerView)
        val searchView = view.findViewById<SearchView>(R.id.search_view)
        val btnCreateWallet = view.findViewById<View>(R.id.btn_create_wallet)
        val btnSort = view.findViewById<View>(R.id.btn_sort)
        val defaultSortType = SortType.DEFAULT

        walletAdapter = WalletAdapter(WalletRepository.getWallets())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = walletAdapter

        btnCreateWallet.setOnClickListener{
            findNavController().navigate(R.id.action_walletsFragment_to_createWalletFragment)
        }

        btnSort.setOnClickListener {
            val sortSheet = SortOptionsBottomSheet(
                onSortSelected = { sortType ->
                    currentSort = sortType // update the current sort
                    val sortedWallets = WalletRepository.getSortedWallets(sortType)
                    walletAdapter.updateList(sortedWallets)
                },
                currentSort = currentSort // pass the current one, not always DEFAULT
            )
            sortSheet.show(parentFragmentManager, "SortOptionsBottomSheet")

        }
    }
}