package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context

object SwingStorage {

    private const val PREFS = "swing_prefs"
    private const val KEY_SWINGS = "available_swings"
    private const val KEY_LAST_RESET = "last_reset"

    fun getSwings(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_SWINGS, 0)
    }

    fun setSwings(context: Context, value: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_SWINGS, value)
            .apply()
    }

    fun getLastReset(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_RESET, 0)
    }

    fun setLastReset(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_RESET, timestamp)
            .apply()
    }
}
