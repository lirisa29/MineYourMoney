package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.rewards.ChooseRewardFragment
import kotlinx.coroutines.launch

class CrystalsFragment : Fragment(R.layout.fragment_mining) {

    private lateinit var streakIcon: ImageView
    private lateinit var streakNumber: TextView
    private lateinit var repo: CrystalRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = CrystalRepository(requireContext())

        val progress = view.findViewById<LinearProgressIndicator>(R.id.mining_progress)
        val swingLabel = view.findViewById<TextView>(R.id.available_swings)
        val useSwingBtn = view.findViewById<Button>(R.id.button_use_swing)
        val vaultBtn = view.findViewById<Button>(R.id.button_crystal_vault)

        streakIcon = view.findViewById(R.id.streak_icon)
        streakNumber = view.findViewById(R.id.streak_number)

        MiningManager.loadData(requireContext())
        updateUI(progress, swingLabel)
        loadStreak()

        vaultBtn.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                CrystalVaultFragment(),
                addToBackStack = true
            )
        }

        vaultBtn.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                CrystalVaultFragment(),
                addToBackStack = true
            )
        }

        val helpFab = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.help_fab)
        helpFab.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                RulesFragment(),
                addToBackStack = true
            )
        }

        useSwingBtn.setOnClickListener {
            val broke = MiningManager.useSwing(requireContext())

            updateUI(progress, swingLabel)  // UPDATED EARLY UI UPDATE

            if (broke) {
                lifecycleScope.launch {
                    val result = MiningManager.finalizeBreak(requireContext(), repo)

                    Toast.makeText(requireContext(), "${result.rarity} crystal unlocked!", Toast.LENGTH_SHORT).show()

                    result.grantedBuff?.let { buff ->
                        when (buff) {
                            BuffType.STREAK_PROTECTOR ->
                                Toast.makeText(requireContext(), "Streak Protector earned!", Toast.LENGTH_LONG).show()

                            BuffType.DAMAGE_BUFF ->
                                Toast.makeText(requireContext(), "Damage buff +${result.amount}!", Toast.LENGTH_LONG).show()

                            BuffType.EXTRA_SWING_DAYS ->
                                Toast.makeText(requireContext(), "Extra swings for ${result.amount} day(s)!", Toast.LENGTH_LONG).show()
                        }
                    }

                    // Navigate to reward screen
                    (requireActivity() as MainActivity).replaceFragment(
                        ChooseRewardFragment(),
                        addToBackStack = true
                    )

                    // Refresh when back
                    loadStreak()
                    updateUI(progress, swingLabel)
                }
            }
        }
    }

    private fun loadStreak() {
        val streak = StreakManager.getStreak(requireContext())
        if (streak <= 0) {
            streakIcon.visibility = View.GONE
            streakNumber.visibility = View.GONE
            return
        }

        streakIcon.visibility = View.VISIBLE
        streakNumber.visibility = View.VISIBLE
        streakIcon.setImageResource(R.drawable.ic_cook)
        streakNumber.text = streak.toString()
    }

    private fun updateUI(progress: LinearProgressIndicator, label: TextView) {
        val rock = MiningManager.currentRock
        progress.max = rock.swingsRequired
        progress.progress = rock.swingsUsed
        label.text = "${MiningManager.availableSwings} Available Swings"
    }

    override fun onResume() {
        super.onResume()
        MiningManager.loadData(requireContext())

        view?.let {
            val progress = it.findViewById<LinearProgressIndicator>(R.id.mining_progress)
            val swings = it.findViewById<TextView>(R.id.available_swings)
            updateUI(progress, swings)
        }

        loadStreak()
    }
}
