package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentWalletsBinding

class WalletsFragment : Fragment() {

    private var _binding: FragmentWalletsBinding? = null
    private val binding get() = _binding!!

    private lateinit var walletAdapter: WalletAdapter
    private val walletsViewModel: WalletsViewModel by activityViewModels {
        // Pass the currentUserId from MainActivity to the ViewModelFactory
        WalletsViewModelFactory(requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("WalletsFragment", "onCreateView called")
        _binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("WalletsFragment", "onViewCreated called")

        // Setup RecyclerView
        walletAdapter = WalletAdapter(emptyList())
        binding.walletsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = walletAdapter
        }
        Log.d("WalletsFragment", "RecyclerView initialized")

        // Observe wallet list from ViewModel (already sorted)
        walletsViewModel.wallets.observe(viewLifecycleOwner) { wallets ->
            Log.i("WalletsFragment", "Wallet List updated")
            walletAdapter.updateList(wallets)
        }

        // Navigate to spending overview screen
        binding.btnSpendingOverview.setOnClickListener {
            Log.d("WalletsFragment", "Navigating to SpendingOverviewFragment")
            (requireActivity() as MainActivity).replaceFragment(SpendingOverviewFragment(), addToBackStack = false)
        }

        // Navigate to add expense screen
        binding.fabAddExpense.setOnClickListener {
            Log.d("WalletsFragment", "Navigating to AddExpensesFragment")
            (requireActivity() as MainActivity).replaceFragment(AddExpenseFragment(), addToBackStack = false)
        }

        // Navigate to create wallet screen
        binding.btnCreateWallet.setOnClickListener {
            Log.d("WalletsFragment", "Navigating to CreateWalletFragment")
            (requireActivity() as MainActivity).replaceFragment(CreateWalletFragment(), addToBackStack = false)
        }

        // Show sort options
        binding.btnSort.setOnClickListener {
            Log.d("WalletsFragment", "Sort Button Clicked")
            SortOptionsBottomSheet(
                onSortSelected = { sortType ->
                    walletsViewModel.setSort(sortType) // Update ViewModel
                },
                currentSort = walletsViewModel.getCurrentSort() // expose getter in VM
            ).show(parentFragmentManager, "SortOptionsBottomSheet")
        }

        // Search logic
        binding.searchView.apply {
            setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d("WalletsFragment", "Search submitted")
                    handleSearch(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d("WalletsFragment", "Search text changed")
                    handleSearch(newText.orEmpty())
                    return true
                }
            })
        }

        // Clear focus on background tap
        binding.root.setOnClickListener {
            Log.v("WalletsFragment", "Root clicked")
            binding.searchView.clearFocus()
        }

        @Suppress("ClickableViewAccessibility")
        binding.walletsRecyclerView.setOnTouchListener { v, _ ->
            binding.searchView.clearFocus()
            v.performClick()
            false
        }
    }

    private fun handleSearch(query: String) {
        Log.d("WalletsFragment", "Handling search for query")
        val allWallets = walletsViewModel.wallets.value.orEmpty()
        val filtered = if (query.isBlank()) {
            allWallets
        } else {
            allWallets.filter { it.name.contains(query, ignoreCase = true) }
        }

        if (query.isNotBlank() && filtered.isEmpty()) {
            Toast.makeText(context, "No wallets found", Toast.LENGTH_SHORT).show()
        }
        walletAdapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
