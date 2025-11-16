package com.iie.thethreeburnouts.mineyourmoney.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.budget.Budget
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModel
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetViewModelFactory
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetsFragment
import com.iie.thethreeburnouts.mineyourmoney.budget.EditBudgetsBottomSheet
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentDashboardBinding
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentWalletsBinding
import com.iie.thethreeburnouts.mineyourmoney.expense.AddExpenseFragment

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
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
        Log.d("WalletsFragment", "onCreateView called")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadOrInitBudget()
        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            budget?.let { updateUI(it) }
        }
        binding.btnEditBudget.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                BudgetsFragment(),
                addToBackStack = false
            )
        }
        // nav to budget screen on budget card click
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
        binding.budgetProgressRing.progress = 100 - percentUsed
        // Change ring colour based on spending
        val colourRes = when {
            total < min -> R.color.blue // below min
            total in min..max -> R.color.purple // within range
            else -> R.color.red // over max
        }
        val color = requireContext().getColor(colourRes)
        binding.budgetProgressRing.setIndicatorColor(color)
    }
}