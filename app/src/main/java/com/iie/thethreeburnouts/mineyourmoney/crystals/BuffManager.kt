package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context
import kotlin.random.Random

// uses your existing enum com.iie.thethreeburnouts.mineyourmoney.crystals.BuffType

data class BuffReward(
    val type: BuffType,
    val amount: Int = 1
)

object BuffManager {

    private const val PREF = "BUFF_DATA"
    private const val KEY_STREAK_PROTECTOR = "buff_streak_protector"
    private const val KEY_DAMAGE_BUFF = "buff_damage"
    private const val KEY_EXTRA_SWING_DAYS = "buff_extra_swing_days"

    // ----------------------
    // GETTERS
    // ----------------------
    fun getStreakProtector(context: Context): Boolean {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_STREAK_PROTECTOR, false)
    }

    fun getDamageBuff(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_DAMAGE_BUFF, 0)
    }

    fun getExtraSwingDays(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_EXTRA_SWING_DAYS, 0)
    }

    // ----------------------
    // SETTERS / UPDATERS
    // ----------------------
    fun setStreakProtector(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_STREAK_PROTECTOR, enabled)
            .apply()
    }

    fun addDamageBuff(context: Context, amount: Int) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_DAMAGE_BUFF, 0)
        prefs.edit().putInt(KEY_DAMAGE_BUFF, (current + amount).coerceAtLeast(0)).apply()
    }

    /**
     * Add (or subtract) extra swing days. Accepts negative to consume days.
     */
    fun addExtraSwingDay(context: Context, amount: Int) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_EXTRA_SWING_DAYS, 0)
        prefs.edit().putInt(KEY_EXTRA_SWING_DAYS, (current + amount).coerceAtLeast(0)).apply()
    }

    // ----------------------
    // BUFF DROP LOGIC
    // ----------------------
    /**
     * Randomly decides whether to drop a buff after a crystal break.
     * Returns the BuffReward applied (and persisted) or null if nothing dropped.
     *
     * Current behaviour:
     * - 20% total chance to drop any buff
     * - Randomly picks one of the 3 buff types and persists it
     */
    fun maybeDropRandomBuff(context: Context): BuffReward? {
        val globalRoll = Random.nextInt(100)
        if (globalRoll >= 20) return null // 0..19 = 20% chance

        // choose buff
        return when (Random.nextInt(3)) {
            0 -> {
                // give streak protector
                setStreakProtector(context, true)
                BuffReward(BuffType.STREAK_PROTECTOR, 1)
            }
            1 -> {
                // increase damage buff (stackable)
                addDamageBuff(context, 1)
                BuffReward(BuffType.DAMAGE_BUFF, 1)
            }
            else -> {
                // add 1 extra swing day
                addExtraSwingDay(context, 1)
                BuffReward(BuffType.EXTRA_SWING_DAYS, 1)
            }
        }
    }
}
