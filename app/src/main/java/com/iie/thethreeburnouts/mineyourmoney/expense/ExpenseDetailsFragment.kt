package com.iie.thethreeburnouts.mineyourmoney.expense

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.MainActivityProvider
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentExpenseDetailsBinding
import com.iie.thethreeburnouts.mineyourmoney.spendingoverview.SpendingOverviewFragment
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletsFragment

class ExpenseDetailsFragment(private val expenseId: Int, private val source: String)
    : Fragment() {

    private var _binding: FragmentExpenseDetailsBinding? = null
    private val binding get() = _binding!!

    // 🔥 FIX: Use correct factory so the ViewModel contains getExpenseById()
    private val expensesViewModel: ExpensesViewModel by activityViewModels {
        ExpensesViewModelFactory(
            requireActivity().application,
            (requireActivity() as MainActivityProvider).getCurrentUserId()
        )
    }

    private var hasBeenDeleted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopAppBar()
        loadExpenseDetails(expenseId)

        binding.btnDelete.setOnClickListener {
            hasBeenDeleted = true
            expensesViewModel.deleteExpense(expenseId)

            Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show()

            when (source) {
                "SpendingOverview" ->
                    (requireActivity() as MainActivity).replaceFragment(
                        SpendingOverviewFragment(), addToBackStack = false
                    )

                "AddExpense" ->
                    (requireActivity() as MainActivity).replaceFragment(
                        WalletsFragment(), addToBackStack = false
                    )
            }
        }
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            when (source) {
                "SpendingOverview" ->
                    (requireActivity() as MainActivity).replaceFragment(
                        SpendingOverviewFragment(), addToBackStack = false
                    )
                "AddExpense" ->
                    (requireActivity() as MainActivity).replaceFragment(
                        WalletsFragment(), addToBackStack = false
                    )
            }
        }
    }

    private fun loadExpenseDetails(id: Int) {

        expensesViewModel.getExpenseById(id).observe(viewLifecycleOwner) { expenseWithWallet ->

            if (hasBeenDeleted) return@observe
            if (expenseWithWallet == null) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return@observe
            }

            val exp = expenseWithWallet.expense ?: return@observe

            binding.tvExpenseAmount.text = "R${String.format("%,.2f", exp.amount)}"
            binding.tvWallet.text = expenseWithWallet.wallet?.name ?: "Unknown"
            binding.tvDate.text = java.text.SimpleDateFormat(
                "dd/MM/yyyy", java.util.Locale.getDefault()
            ).format(java.util.Date(exp.date))

            binding.tvRecurrence.text = exp.recurrence ?: "Never"

            if (!exp.note.isNullOrEmpty()) {
                binding.tvNote.text = exp.note
            } else {
                binding.tvNote.text = "No note provided"
            }

            if (!exp.photoPath.isNullOrEmpty()) {
                binding.imgPhoto.visibility = View.VISIBLE
                binding.tvPhotoEmpty.visibility = View.GONE
                binding.imgPhoto.setImageURI(Uri.parse(exp.photoPath))
            } else {
                binding.imgPhoto.visibility = View.GONE
                binding.tvPhotoEmpty.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
