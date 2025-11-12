package com.iie.thethreeburnouts.mineyourmoney.spendingoverview

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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentSpendingOverviewBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseDetailsFragment
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseWithWallet
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModel
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment
import java.util.Calendar
import java.util.TimeZone

class SpendingOverviewFragment : Fragment(){

    private var _binding: FragmentSpendingOverviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expensesViewModel: ExpensesViewModel by activityViewModels(){ //(Google Developers Training team, 2025)
        ExpensesViewModelFactory(
            requireActivity().application, //(Google Developers Training team, 2025)
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
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

        setupPieChart() //(danielgindi,2025)

        binding.transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_divider)!!)
            binding.transactionRecyclerView.addItemDecoration(divider)
        }

        // Observe expense list from ViewModel
        expensesViewModel.expense.observe(viewLifecycleOwner) { expenses -> //(Google Developers Training team, 2025)
            expenseAdapter.updateList(expenses)

            val total = expenses.sumOf { it.expense.amount }
            Log.i("SpendingOverviewFragment", "Updated total spending")
            binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"

            updatePieChart(expenses) //(danielgindi,2025)
        }

        binding.topAppBar.setNavigationOnClickListener {
            Log.i("SpendingOverviewFragment", "Navigation clicked")
            (requireActivity() as MainActivity).replaceFragment(WalletsFragment(), addToBackStack = false)
        }

        binding.btnSelectRange.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()

            val constraints = CalendarConstraints.Builder() //(Google Developers Training team, 2025)
                .setValidator(DateValidatorPointBackward.now())
                .setEnd(today)
                .build()

            // Use the last selected range as initial selection if available
            val builder = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setTheme(R.style.CustomDateRangePicker)
                .setCalendarConstraints(constraints) //(Google Developers Training team, 2025)

            lastSelectedRange?.let { range ->
                builder.setSelection(androidx.core.util.Pair(range.first, range.second))
            }

            val picker = builder.build()

            picker.show(childFragmentManager, "date_range_picker")

            picker.addOnPositiveButtonClickListener { selection ->
                val startDateUtc = selection.first ?: return@addOnPositiveButtonClickListener
                val endDateUtc = selection.second ?: return@addOnPositiveButtonClickListener

                // Convert to full-day range
                val startOfDay = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = startDateUtc
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfDay = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = endDateUtc
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                // Save this selection for next time
                lastSelectedRange = Pair(startDateUtc, endDateUtc)

                binding.tvSelectedRange.text = picker.headerText

                // Use the adjusted range when filtering expenses
                expensesViewModel.getExpensesInRange(startOfDay, endOfDay)
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
                expensesViewModel.expense.observe(viewLifecycleOwner) { expenses -> //(Google Developers Training team, 2025)
                    expenseAdapter.updateList(expenses)
                    val total = expenses.sumOf { it.expense.amount }
                    binding.tvTotalSpendingAmount.text = "R${String.format("%,.2f", total)}"

                    updatePieChart(expenses)
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
        binding.spendingPieChart.apply { //(danielgindi,2025)
            description.isEnabled = false
            isRotationEnabled = true
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setDrawHoleEnabled(false)
        }

        // Handle clicks on segments
        binding.spendingPieChart.setOnChartValueSelectedListener(object : //(danielgindi,2025)
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is PieEntry) {
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

    private fun updatePieChart(expenses: List<ExpenseWithWallet>) { //(danielgindi,2025)
        // Aggregate total per wallet
        val totalsPerWallet = expenses.groupBy { it.wallet.name }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        val entries = totalsPerWallet.map { (walletName, total) ->
            // Store a reference to the color in PieEntry using 'data'
            val walletColor = expenses.find { it.wallet.name == walletName }?.wallet?.color ?: Color.GRAY
            PieEntry(total.toFloat(), walletName, walletColor)
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

        val data = PieData(dataSet)
        data.setDrawValues(false)

        binding.spendingPieChart.data = data //(danielgindi,2025)
        binding.spendingPieChart.invalidate() //(danielgindi,2025)
        Log.i("SpendingOverviewFragment", "Pie chart updated successfully")
    }

    class WalletValueFormatter : ValueFormatter() {
        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
            pieEntry ?: return ""
            val walletName = pieEntry.label //(danielgindi,2025)
            val amount = value
            return "$walletName: R${String.format("%,.2f", amount)}"
        }
    }
}