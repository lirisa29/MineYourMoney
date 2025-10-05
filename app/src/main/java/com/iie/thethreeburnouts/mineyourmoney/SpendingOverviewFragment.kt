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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentSpendingOverviewBinding

class SpendingOverviewFragment : Fragment(){

    private var _binding: FragmentSpendingOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expensesViewModel: ExpensesViewModel by activityViewModels(){
        ExpensesViewModelFactory(requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId())
    }
    // Store the last selected range
    private var lastSelectedRange: Pair<Long, Long>? = null

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

            val total = expenses.sumOf { it.expense.amount }
            binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"
        }

        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), addToBackStack = false)
        }

        binding.btnSelectRange.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()

            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .setEnd(today)
                .build()

            // Use the last selected range as initial selection if available
            val builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setTheme(R.style.CustomDateRangePicker)
                .setCalendarConstraints(constraints)

            lastSelectedRange?.let { range ->
                builder.setSelection(androidx.core.util.Pair(range.first, range.second))
            }

            val picker = builder.build()

            picker.show(childFragmentManager, "date_range_picker")

            picker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first ?: return@addOnPositiveButtonClickListener
                val endDate = selection.second ?: return@addOnPositiveButtonClickListener

                // Save this selection for next time
                lastSelectedRange = Pair(startDate, endDate)

                binding.tvSelectedRange.text = picker.headerText

                expensesViewModel.getExpensesInRange(startDate, endDate)
                    .observe(viewLifecycleOwner) { filteredExpenses ->
                        expenseAdapter.updateList(filteredExpenses)
                        val total = filteredExpenses.sumOf { it.expense.amount }
                        binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"
                    }
            }

            picker.addOnNegativeButtonClickListener {
                binding.tvSelectedRange.text = ""
                lastSelectedRange = null // clear saved selection if reset
                expensesViewModel.expense.observe(viewLifecycleOwner) { expenses ->
                    expenseAdapter.updateList(expenses)
                    val total = expenses.sumOf { it.expense.amount }
                    binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}