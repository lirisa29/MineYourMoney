package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context

object StreakManager {

    private const val PREF = "STREAK_DATA"
    private const val KEY_STREAK = "streak_count"
    private const val KEY_LAST_DAY = "streak_last_day"

    fun getCurrentDay(): Long {
        return System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    }

    fun getStreak(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_STREAK, 0)
    }

    fun updateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val savedDay = prefs.getLong(KEY_LAST_DAY, 0)
        val today = getCurrentDay()

        val streakProtector = BuffManager.getStreakProtector(context)

        val editor = prefs.edit()

        when {
            savedDay == today -> {
                // already counted for today
            }

            savedDay == today - 1 -> {
                // Continue streak
                val newStreak = getStreak(context) + 1
                editor.putInt(KEY_STREAK, newStreak)
            }

            savedDay < today - 1 -> {
                // Missed days
                if (streakProtector) {
                    // Consume streak protector
                    BuffManager.setStreakProtector(context, false)
                } else {
                    editor.putInt(KEY_STREAK, 0)
                }
            }
        }

        editor.putLong(KEY_LAST_DAY, today)
        editor.apply()
    }
}
