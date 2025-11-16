package com.iie.thethreeburnouts.mineyourmoney.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentBudgetsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BudgetsFragment : Fragment(){

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(
            requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.budgetsFragment) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            // Apply padding to push toolbar below status bar
            binding.topAppBar.setPadding(
                binding.topAppBar.paddingLeft,
                statusBar.top,
                binding.topAppBar.paddingRight,
                binding.topAppBar.paddingBottom
            )
            insets
        }
        setupToolbar()

        viewModel.loadOrInitBudget()

        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            budget?.let { updateUI(it) }
        }

        binding.btnEditBudget.setOnClickListener {
            val current = viewModel.budget.value
            val bottomSheet = EditBudgetsBottomSheet(
                currentMin = current?.minLimit ?: 0.0,
                currentMax = current?.maxLimit ?: 0.0
            ) { newMin, newMax ->
                viewModel.updateBudgetLimits(newMin, newMax)
            }
            bottomSheet.show(parentFragmentManager, "EditBudgetBottomSheet")
        }
    }

    private fun setupToolbar() {
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
        val daysLeft = getDaysLeftInMonth()
        binding.tvToolbarTitle.text = "$month Budget"
        binding.tvToolbarSubtitle.text = "$daysLeft days left"
    }

    private fun getDaysLeftInMonth(): Int {
        val cal = Calendar.getInstance()
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH)
    }

    private fun updateUI(budget: Budget) {
        val total = budget.totalSpent
        val min = budget.minLimit
        val max = budget.maxLimit

        // Display min & max
        binding.tvMonthlyBudgetLimitMin.text = "Minimum Spending Limit: R${String.format("%,.2f", min)}"
        binding.tvMonthlyBudgetLimitMax.text = "Maximum Spending Limit: R${String.format("%,.2f", max)}"

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
            total < min -> R.color.blue  // below min
            total in min..max -> R.color.green // within range
            else -> R.color.red// over max
        }

        val color = requireContext().getColor(colourRes)
        binding.budgetProgressRing.setIndicatorColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}