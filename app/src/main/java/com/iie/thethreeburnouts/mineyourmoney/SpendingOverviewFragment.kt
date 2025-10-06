package com.iie.thethreeburnouts.mineyourmoney

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieDataSet
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
        Log.i("SpendingOverviewFragment", "onCreateView: Inflating SpendingOverviewFragment layout.")
        _binding = FragmentSpendingOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("SpendingOverviewFragment", "onViewCreated: Initializing UI Components")
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        expenseAdapter = ExpenseAdapter(emptyList()) { expenseId ->
            Log.i("SpendingOverviewFragment", "Expense item clicked")
            // Open ExpenseDetailsFragment
            (requireActivity() as MainActivity).replaceFragment(
                ExpenseDetailsFragment(expenseId, source = "SpendingOverview"),
                addToBackStack = false
            )
        }

        setupPieChart()

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
            Log.i("SpendingOverviewFragment", "Updated total spending")
            binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"

            updatePieChart(expenses)
        }

        binding.topAppBar.setNavigationOnClickListener {
            Log.i("SpendingOverviewFragment", "Navigation clicked")
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

                        updatePieChart(filteredExpenses)
                    }
            }

            picker.addOnNegativeButtonClickListener {
                Log.i("SpendingOverviewFragment", "Date range reset")
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

    private fun setupPieChart() {
        Log.i("SpendingOverviewFragment", "Setting up PieChart")
        binding.spendingPieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setDrawHoleEnabled(false)
        }

        // Handle clicks on segments
        binding.spendingPieChart.setOnChartValueSelectedListener(object :
            com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                if (e is com.github.mikephil.charting.data.PieEntry) {
                    val walletName = e.label
                    val amount = e.value
                    Toast.makeText(requireContext(), "$walletName: R${String.format("%,.2f", amount)}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected() {
                Log.i("SpendingOverviewFragment", "No pie chart slice selected")
            }
        })
    }

    private fun updatePieChart(expenses: List<ExpenseWithWallet>) {
        // Aggregate total per wallet
        val totalsPerWallet = expenses.groupBy { it.wallet.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        val entries = totalsPerWallet.map { (walletName, total) ->
            // Store a reference to the color in PieEntry using 'data'
            val walletColor = expenses.find { it.wallet.name == walletName }?.wallet?.color ?: Color.GRAY
            com.github.mikephil.charting.data.PieEntry(total.toFloat(), walletName, walletColor)
        }

        val dataSet = PieDataSet(entries, "")
        // Assign colors based on the wallet color stored in PieEntry.data
        val colors = entries.map { it.data as? Int ?: Color.GRAY }
        dataSet.colors = colors

        dataSet.valueFormatter = WalletValueFormatter() // <-- custom formatter

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD)
        // **Center values inside the slice**
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE)
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE)

        dataSet.colors = colors
        dataSet.sliceSpace = 2f

        val data = com.github.mikephil.charting.data.PieData(dataSet)
        data.setDrawValues(false)

        binding.spendingPieChart.data = data
        binding.spendingPieChart.invalidate()
        Log.i("SpendingOverviewFragment", "Pie chart updated successfully")
    }

    class WalletValueFormatter : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getPieLabel(value: Float, pieEntry: com.github.mikephil.charting.data.PieEntry?): String {
            pieEntry ?: return ""
            val walletName = pieEntry.label
            val amount = value
            return "$walletName: R${String.format("%,.2f", amount)}"
        }
    }
}