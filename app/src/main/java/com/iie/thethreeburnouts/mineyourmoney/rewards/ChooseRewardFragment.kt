package com.iie.thethreeburnouts.mineyourmoney.rewards

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.crystals.BuffManager
import com.iie.thethreeburnouts.mineyourmoney.crystals.CrystalsFragment
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentChooserewardBinding

class ChooseRewardFragment : Fragment(R.layout.fragment_choosereward) {

    private var _binding: FragmentChooserewardBinding? = null
    private val binding get() = _binding!!

    private var selectedReward: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChooserewardBinding.bind(view)

        // Back
        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val rewardIcons = listOf(binding.reward1, binding.reward2, binding.reward3)

        rewardIcons.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedReward = index
                highlightSelection(rewardIcons, index)
            }
        }

        binding.buttonConfirmReward.setOnClickListener {
            if (selectedReward == -1) {
                Toast.makeText(requireContext(), "Please choose a reward first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            applyReward(selectedReward)
            Toast.makeText(requireContext(), "Reward applied!", Toast.LENGTH_SHORT).show()

            (requireActivity() as MainActivity).replaceFragment(
                CrystalsFragment(),
                addToBackStack = false
            )
        }
    }

    private fun highlightSelection(views: List<ImageView>, selectedId: Int) {
        views.forEachIndexed { index, view ->
            if (index == selectedId) {
                view.alpha = 1f
                view.setBackgroundResource(R.drawable.reward_selected_outline)
            } else {
                view.alpha = 0.5f
                view.setBackgroundResource(0)
            }
        }
    }

    private fun applyReward(id: Int) {
        when (id) {
            0 -> BuffManager.setStreakProtector(requireContext(), true)
            1 -> BuffManager.addDamageBuff(requireContext(), 1)
            2 -> BuffManager.addExtraSwingDay(requireContext(), 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
