package com.iie.thethreeburnouts.mineyourmoney.budget

import android.content.Context

object BudgetStatusManager {

    private const val PREF = "BUDGET_DATA"
    private const val KEY_WITHIN = "stayed_within_budget"
    private const val KEY_BELOW_MIN = "below_min_budget"

    suspend fun updateBudgetFlags(context: Context, dao: BudgetDao, userId: Int) {
        val budget = dao.getBudget(userId) ?: return

        val within = budget.totalSpent <= budget.maxLimit
        val belowMin = budget.totalSpent < budget.minLimit

        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

        prefs.edit()
            .putBoolean(KEY_WITHIN, within)
            .putBoolean(KEY_BELOW_MIN, belowMin)
            .apply()
    }
}
