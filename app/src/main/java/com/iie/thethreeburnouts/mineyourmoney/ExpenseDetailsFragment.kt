package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.net.Uri
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentExpenseDetailsBinding

class ExpenseDetailsFragment(private val expenseId: Int, private val source: String) : Fragment() {

    private var _binding: FragmentExpenseDetailsBinding? = null
    private val binding get() = _binding!!
    private val expensesViewModel: ExpensesViewModel by activityViewModels()
    private var hasBeenDeleted = false
    private val TAG = "ExpenseDetailsFragment"

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
            Log.d(TAG, "Delete clicked for expenseId=$expenseId")
            hasBeenDeleted = true
            expensesViewModel.deleteExpense(expenseId)
            Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show()

            when (source) {
                "SpendingOverview" -> (requireActivity() as MainActivity)
                    .replaceFragment(SpendingOverviewFragment(), addToBackStack = false)
                "AddExpense" -> (requireActivity() as MainActivity)
                    .replaceFragment(WalletsFragment(), addToBackStack = false)
            }
        }
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            Log.d(TAG, "Back arrow pressed")

            when (source) {
                "SpendingOverview" -> (requireActivity() as MainActivity)
                    .replaceFragment(SpendingOverviewFragment(), addToBackStack = false)
                "AddExpense" -> (requireActivity() as MainActivity)
                    .replaceFragment(WalletsFragment(), addToBackStack = false)
            }
        }
    }

    private fun loadExpenseDetails(id: Int) {
        Log.d(TAG, "Observing expense details for id=$id")

        expensesViewModel.getExpenseById(id).observe(viewLifecycleOwner) { expenseWithWallet ->
            Log.d(TAG, "Expense LiveData update for id=$id: $expenseWithWallet")

            if (hasBeenDeleted) {
                Log.w(TAG, "Expense already deleted, ignoring LiveData update")
                return@observe
            }

            if (expenseWithWallet == null) {
                Log.w(TAG, "Expense not found in database (possibly deleted)")
                requireActivity().onBackPressed()
                return@observe
            }

            val exp = expenseWithWallet.expense
            if (exp == null) {
                Log.e(TAG, "Expense object is null inside ExpenseWithWallet!")
                return@observe
            }

            Log.d(TAG, "Displaying expense details for expenseId=${exp.id}")

            binding.tvExpenseAmount.text = "R${String.format("%,.2f", exp.amount)}"
            binding.tvWallet.text = expenseWithWallet.wallet?.name ?: "Unknown"
            binding.tvDate.text =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(exp.date))
            binding.tvRecurrence.text = exp.recurrence ?: "Never"

            // Note
            if (!exp.note.isNullOrEmpty()) {
                binding.tvNote.text = exp.note
            } else {
                binding.tvNote.text = "No note provided"
            }

            // Photo
            if (!exp.photoPath.isNullOrEmpty()) {
                binding.tvPhotoEmpty.visibility = View.GONE
                binding.imgPhoto.visibility = View.VISIBLE
                binding.imgPhoto.setImageURI(Uri.parse(exp.photoPath))
            } else {
                binding.tvPhotoEmpty.visibility = View.VISIBLE
                binding.imgPhoto.visibility = View.GONE
                binding.tvPhotoEmpty.text = "No photo attached"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called for ExpenseDetailsFragment")
        _binding = null
    }
}