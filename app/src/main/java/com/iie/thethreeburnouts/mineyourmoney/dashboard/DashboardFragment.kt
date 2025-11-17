package com.iie.thethreeburnouts.mineyourmoney.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.budget.Budget
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModel
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetsFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentDashboardBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.AddExpenseFragment
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseWithWallet
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModel
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.spendingoverview.ExpenseAdapter
import com.iie.thethreeburnouts.mineyourmoney.wallet.CreateWalletFragment
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsViewModel
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var walletsViewModel: WalletsViewModel

    private val viewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(
            requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
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

    private val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

    private var startMillis: Long = 0L
    private var endMillis: Long = 0L

    private var maxLimit = 0f
    private var minLimit = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("WalletsFragment", "onCreateView called")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup chart immediately
        setupLineChart()

        // determine current month range
        setCurrentMonthRange()

        // observe expenses for current month
        expensesViewModel.getExpensesInRange(startMillis, endMillis)
            .observe(viewLifecycleOwner) { expenses ->
                updateLineChart(expenses, startMillis, endMillis)
            }

        viewModel.loadOrInitBudget()
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {

                // ⭐ ADDED — REQUIRED FOR LIMIT LINES TO WORK
                minLimit = budget.minLimit.toFloat()
                maxLimit = budget.maxLimit.toFloat()

                updateUI(budget)

                // ⭐ ADDED — Forces chart to update with new limit lines
                expensesViewModel.getExpensesInRange(startMillis, endMillis)
                    .observe(viewLifecycleOwner) { expenses ->
                        updateLineChart(expenses, startMillis, endMillis)
                    }
            }
        }

        binding.btnEditBudget.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                BudgetsFragment(),
                addToBackStack = false
            )
        }
        // button to go to add expense fragment
        binding.btnLogExpense.setOnClickListener{
            (requireActivity() as MainActivity).replaceFragment(
                AddExpenseFragment(),
                addToBackStack = false
            )
        }
        // button for create wallet
        binding.btnCreateWallet.setOnClickListener{
            (requireActivity() as MainActivity).replaceFragment(
                CreateWalletFragment(),
                addToBackStack = false
            )
        }
    }

    private fun setCurrentMonthRange() {
        val start = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val end = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        startMillis = start.timeInMillis
        endMillis = end.timeInMillis
    }


    private fun updateUI(budget: Budget) {
        val total = budget.totalSpent
        val min = budget.minLimit
        val max = budget.maxLimit
        // Display min & max
        binding.tvMonthlyBudgetLimitMin.text =
            "Minimum Spending Limit: R${String.format("%,.2f", min)}"
        binding.tvMonthlyBudgetLimitMax.text =
            "Maximum Spending Limit: R${String.format("%,.2f", max)}"
        val percentUsed = if (max > 0)
            ((total / max) * 100).toInt().coerceAtMost(100)
        else 0
        val remaining = (max - total).coerceAtLeast(0.0)
        // Display spending / max
        binding.tvBudgetSpent.text = "R${String.format("%,.2f", remaining)} /"

        binding.tvBudgetTotal.text = "R${String.format("%,.2f", max)}"
        // Update percentage text
        binding.tvBudgetUsage.text = "You've used $percentUsed% of your budget"
        // The progress ring starts full and decreases with spending
        binding.budgetProgressRing.progress = percentUsed
        // Change ring colour based on spending
        val colourRes = when {
            total < min -> R.color.blue // below min
            total in min..max -> R.color.green // within range
            else -> R.color.red // over max
        }
        val color = requireContext().getColor(colourRes)
        binding.budgetProgressRing.setIndicatorColor(color)
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

    private fun updateLineChart(expenses: List<ExpenseWithWallet>, startMillis: Long, endMillis: Long) {
        val chart: LineChart = binding.spendingLineChart
        chart.clear()

        // Build date labels for each day in the selected range (inclusive)
        val labels = mutableListOf<String>()
        val cal =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = startMillis }
        val endCal =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = endMillis }
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