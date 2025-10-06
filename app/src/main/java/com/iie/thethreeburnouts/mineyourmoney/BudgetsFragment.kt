package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import java.text.SimpleDateFormat
import java.util.*
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentBudgetsBinding

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
        setupToolbar()

        viewModel.loadOrInitBudget()

        viewModel.budget.observe(viewLifecycleOwner) { budget ->
            budget?.let { updateUI(it) }
        }

        binding.btnEditBudget.setOnClickListener {
            val currentLimit = viewModel.budget.value?.monthlyLimit ?: 0.0
            val bottomSheet = EditBudgetsBottomSheet(currentLimit) { newLimit ->
                viewModel.updateMonthlyLimit(newLimit)
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
        val percentUsed = if (budget.monthlyLimit > 0)
            ((budget.totalSpent / budget.monthlyLimit) * 100).toInt().coerceAtMost(100)
        else 0

        val remaining = (budget.monthlyLimit - budget.totalSpent).coerceAtLeast(0.0)

        // Update center text to show how much budget is left
        binding.tvBudgetSpent.text = "R${String.format("%,.2f", remaining)} / "
        binding.tvBudgetTotal.text = "R${String.format("%,.2f", budget.monthlyLimit)}"

        // Update percentage text
        binding.tvBudgetUsage.text = "You've used $percentUsed% of your budget"

        // The progress ring starts full and decreases with spending
        binding.budgetProgressRing.progress = 100 - percentUsed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}