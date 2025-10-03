package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentSpendingOverviewBinding

class SpendingOverviewFragment : Fragment(){

    private var _binding: FragmentSpendingOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expensesViewModel: ExpensesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpendingOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
            binding.transactionRecyclerView.addItemDecoration(divider)
        }

        // Observe expense list from ViewModel
        expensesViewModel.expense.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.updateList(expenses)
        }

        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}