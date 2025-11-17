package com.iie.thethreeburnouts.mineyourmoney.spendingoverview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker

import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModel
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentSpendingOverviewBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseDetailsFragment
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseWithWallet
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModel
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SpendingOverviewFragment : Fragment(){

    private var _binding: FragmentSpendingOverviewBinding? = null
    private val binding get() = _binding!!

    // trying to not hardcode
    private val budgetViewModel: BudgetViewModel by lazy {
        val activityProvider = requireActivity() as MainActivityProvider
        val userId = activityProvider.getCurrentUserId()

        val factory = BudgetViewModelFactory(
            requireActivity().application,
            userId
        )
        ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
    }
    private val expensesViewModel: ExpensesViewModel by lazy {
        val activityProvider = requireActivity() as MainActivityProvider
        val userId = activityProvider.getCurrentUserId()

        val factory = ExpensesViewModelFactory(
            requireActivity().application,
            userId
        )
        ViewModelProvider(requireActivity(), factory)[ExpensesViewModel::class.java]
    }

    private lateinit var expenseAdapter: ExpenseAdapter
    // Store the last selected range
    private var lastSelectedRange: Pair<Long, Long>? = null

    // Replace with users limit from settings
    private var maxLimit = 0f
    private var minLimit = 0f

    private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

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

        budgetViewModel.budget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                minLimit = budget.minLimit.toFloat()
                maxLimit = budget.maxLimit.toFloat()

                // Refresh chart for current range
                if (lastSelectedRange != null) {
                    val (start, end) = lastSelectedRange!!
                    expensesViewModel.getExpensesInRange(start, end)
                        .observe(viewLifecycleOwner) { expenses ->
                            updateLineChart(expenses, start, end)
                        }
                } else {
                    loadDefault30Days()
                }
            }
        }

        // Setup RecyclerView
        expenseAdapter = ExpenseAdapter(emptyList()) { expenseId ->
            Log.i("SpendingOverviewFragment", "Expense item clicked")
            // Open ExpenseDetailsFragment
            (requireActivity() as MainActivity).replaceFragment(
                ExpenseDetailsFragment(expenseId, source = "SpendingOverview"),
                addToBackStack = false
            )
        }

        setupLineChart() //(danielgindi,2025)
        loadDefault30Days()

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

            if (lastSelectedRange == null) {
                // default range displayed by loadDefault30Days will observe data directly
            } else {
                // refresh chart for currently selected range
                val (startUtc, endUtc) = lastSelectedRange!!
                val startOfDay = startOfDay(startUtc)
                val endOfDay = endOfDay(endUtc)
                expensesViewModel.getExpensesInRange(startOfDay, endOfDay)
                    .observe(viewLifecycleOwner) { filteredExpenses ->
                        updateLineChart(filteredExpenses, startOfDay, endOfDay)
                    }
            }

            //updatePieChart(expenses) //(danielgindi,2025)
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
                        updateLineChart(filteredExpenses, startOfDay, endOfDay)
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

                    loadDefault30Days()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLineChart() {
        val chart: LineChart = binding.spendingLineChart
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.axisRight.isEnabled = false

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = -30f
        }
        chart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
        }

        // Scaling Functionality
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setScaleXEnabled(true)
        chart.setScaleYEnabled(true)

        // Highlight per drag
        chart.isHighlightPerDragEnabled = true

        // Show 7 data points at a time on X-axis
        chart.setVisibleXRangeMaximum(7f) // shows around 7 days at a time, adjust if needed

        // Drag deceleration
        chart.setDragDecelerationFrictionCoef(0.9f)
    }

    private fun loadDefault30Days() {
        val end = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val start = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.DAY_OF_YEAR, -30) }

        val startMillis = start.timeInMillis
        val endMillis = end.timeInMillis
        lastSelectedRange = Pair(startMillis, endMillis)

        expensesViewModel.getExpensesInRange(startMillis, endMillis)
            .observe(viewLifecycleOwner) { expenses ->
                expenseAdapter.updateList(expenses)
                updateLineChart(expenses, startMillis, endMillis)
            }
    }

    private fun startOfDay(time: Long): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun endOfDay(time: Long): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

    private fun updateLineChart(expenses: List<ExpenseWithWallet>, startMillis: Long, endMillis: Long) {
        val chart: LineChart = binding.spendingLineChart
        chart.clear()

        // Build date labels for each day in the selected range (inclusive)
        val labels = mutableListOf<String>()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endMillis }
        while (!cal.after(endCal)) {
            labels.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (labels.isEmpty()) {
            chart.data = null
            chart.invalidate()
            return
        }

        // Group expenses by wallet name -> then by formatted day label -> sum amounts
        // note: use the same sdf.format(...) for extracting day label from expense date
        val byWalletThenDay: Map<String, Map<String, Double>> = expenses
            .groupBy { it.wallet.name ?: "Unknown Wallet" }
            .mapValues { (_, list) ->
                list.groupBy { sdf.format(it.expense.date) } // previous code used this successfully
                    .mapValues { dayEntry -> dayEntry.value.sumOf { it.expense.amount } }
            }

        // Create a LineDataSet per wallet
        val dataSets = mutableListOf<LineDataSet>()
        var globalMax = 0f

        // Ensure consistent order of wallets (e.g., alphabetic or by creation). We'll use the keys order.
        val walletNames = byWalletThenDay.keys.toList()

        walletNames.forEachIndexed { walletIndex, walletName ->
            val dayMap = byWalletThenDay[walletName] ?: emptyMap()

            // Build entries – one entry per label (day in range), missing days -> 0
            val entries = labels.mapIndexed { dayIndex, label ->
                val amount = (dayMap[label] ?: 0.0).toFloat()
                if (amount > globalMax) globalMax = amount
                Entry(dayIndex.toFloat(), amount)
            }

            // Create dataset and style it
            val colorInt = expenses.find { it.wallet.name == walletName }?.wallet?.color
                ?: 0xFF3F51B5.toInt() // fallback color

            val set = LineDataSet(entries, walletName).apply {
                mode = LineDataSet.Mode.LINEAR
                lineWidth = 2f
                circleRadius = 3f
                setCircleColor(colorInt)
                color = colorInt
                setDrawValues(false)
                setDrawFilled(false)
            }

            dataSets.add(set)
        }

        // If no wallets (no expenses) just clear
        if (dataSets.isEmpty()) {
            chart.data = null
            chart.invalidate()
            return
        }

        chart.data = LineData(dataSets as List<ILineDataSet>)

        // X-axis labels (use index formatter)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = labels.size.coerceAtMost(10) // reduce clutter; chart will show subset
        chart.xAxis.granularity = 1f

        // Add min/max limit lines and ensure axis maximum includes maxLimit
        val leftAxis = chart.axisLeft
        leftAxis.removeAllLimitLines()

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        val color = typedValue.data // this is the actual color int


        val maxLine = LimitLine(maxLimit, "Max Limit").apply {
            lineWidth = 2f
            lineColor = Color.RED
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textColor = color
            textSize = 11f
        }

        val minLine = LimitLine(minLimit, "Min Limit").apply {
            lineWidth = 2f
            lineColor = Color.GREEN
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textColor = color
            textSize = 11f
        }

        leftAxis.addLimitLine(maxLine)
        leftAxis.addLimitLine(minLine)
        leftAxis.axisMinimum = 0f

        // Make axisMaximum either slightly above the highest daily value OR slightly above maxLimit (whichever is higher)
        val highestValue = (globalMax.coerceAtLeast(maxLimit))
        leftAxis.axisMaximum = (highestValue * 1.1f).coerceAtLeast(maxLimit * 1.05f)

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isWordWrapEnabled = true

        // Scaling functionality
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setScaleXEnabled(true)
        chart.setScaleYEnabled(true)
        chart.setPinchZoom(true)
        chart.setDragDecelerationFrictionCoef(0.9f)
        chart.isHighlightPerDragEnabled = true

        // Show 7 data points at a time on X-axis
        chart.setVisibleXRangeMaximum(7f)

        // Apply zoom to show more data points if needed
        val xZoom = (labels.size / 16f).coerceAtLeast(1f)
        val yZoom = 1.5f // adjust this number to control vertical zoom intensity
        chart.zoom(xZoom, yZoom, 0f, 0f)

        // Scroll to the end to show the latest data
        chart.moveViewToX((labels.size - 1).toFloat())

        val xAxis = chart.xAxis

        val leftAxis2 = chart.axisLeft

        val legend = chart.legend

        val description = chart.description

        xAxis.textColor = color
        leftAxis2.textColor = color
        legend.textColor = color
        description.textColor = color

        chart.invalidate()
    }
}