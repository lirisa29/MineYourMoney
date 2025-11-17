package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context
import android.widget.Toast
import kotlin.random.Random

enum class BudgetStatus {
    NORMAL,
    WITHIN,
    BELOW_MIN
}

object MiningManager {

    // ───────────────────────────────────────────
    // CONFIG
    // ───────────────────────────────────────────
    private const val BASE_DAILY_SWINGS = 5
    private const val STREAK_BONUS_THRESHOLD = 10
    private const val BONUS_SWINGS = 5     // So 5 + 5 = 10 daily swings
    private const val MAX_STORED_SWINGS = 50
    private const val KEY_DAY = "swing_day"
    private const val KEY_DAILY_USED = "daily_swings_used"

    var availableSwings = 0
    lateinit var currentRock: Rock

    // ───────────────────────────────────────────
    // LOAD & SAVE
    // ───────────────────────────────────────────
    fun loadData(context: Context) {
        availableSwings = SwingStorage.getSwings(context)
        resetDailyIfNeeded(context)

        if (!::currentRock.isInitialized) {
            generateNewRock(context)
        }
    }

    fun saveData(context: Context) {
        SwingStorage.setSwings(context, availableSwings)
    }

    // ───────────────────────────────────────────
    // DAILY SWING LIMIT MANAGEMENT
    // ───────────────────────────────────────────
    private fun currentDay(): Long =
        System.currentTimeMillis() / (1000 * 60 * 60 * 24)

    private fun prefs(context: Context) =
        context.getSharedPreferences("SWING_DATA", Context.MODE_PRIVATE)

    private fun resetDailyIfNeeded(context: Context) {
        val p = prefs(context)
        val lastDay = p.getLong(KEY_DAY, -1)
        val today = currentDay()

        if (lastDay != today) {
            p.edit()
                .putLong(KEY_DAY, today)
                .putInt(KEY_DAILY_USED, 0)
                .apply()
        }
    }

    private fun getDailyUsed(context: Context): Int =
        prefs(context).getInt(KEY_DAILY_USED, 0)

    private fun setDailyUsed(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_DAILY_USED, value).apply()
    }

    fun getDailySwingLimit(context: Context): Int {
        val streak = StreakManager.getStreak(context)
        return if (streak >= STREAK_BONUS_THRESHOLD)
            BASE_DAILY_SWINGS + BONUS_SWINGS
        else
            BASE_DAILY_SWINGS
    }

    // ───────────────────────────────────────────
    // CAN RECEIVE SWING?
    // ───────────────────────────────────────────
    fun canReceiveSwing(context: Context): Boolean {
        val dailyLimit = getDailySwingLimit(context)
        val dailyUsed = getDailyUsed(context)

        return availableSwings < MAX_STORED_SWINGS && dailyUsed < dailyLimit
    }

    // ───────────────────────────────────────────
    // ADD SWING
    // ───────────────────────────────────────────
    fun addSwing(context: Context, amount: Int) {
        resetDailyIfNeeded(context)

        val dailyLimit = getDailySwingLimit(context)
        val dailyUsed = getDailyUsed(context)

        // At max stored swings
        if (availableSwings >= MAX_STORED_SWINGS) {
            Toast.makeText(context, "You already have the maximum stored swings!", Toast.LENGTH_SHORT).show()
            return
        }

        // Reached daily usage limit
        if (dailyUsed >= dailyLimit) {
            Toast.makeText(context, "You've used all your swings for today!", Toast.LENGTH_SHORT).show()
            return
        }

        availableSwings += amount

        if (availableSwings > MAX_STORED_SWINGS)
            availableSwings = MAX_STORED_SWINGS

        saveData(context)

        Toast.makeText(context, "+1 Swing earned!", Toast.LENGTH_SHORT).show()
    }

    // ───────────────────────────────────────────
    // USE SWING (ALWAYS allowed if stored swings exist)
    // ───────────────────────────────────────────
    fun useSwing(context: Context): Boolean {
        resetDailyIfNeeded(context)

        if (availableSwings <= 0) return false

        val dmgBuff = BuffManager.getDamageBuff(context)

        availableSwings--
        saveData(context)

        // Count daily usage
        val used = getDailyUsed(context) + 1
        setDailyUsed(context, used)

        currentRock.swingsUsed += (1 + dmgBuff)

        return currentRock.isBroken()
    }

    // ───────────────────────────────────────────
    // CRYSTAL BREAK & NEW ROCK
    // ───────────────────────────────────────────
    suspend fun finalizeBreak(context: Context, repo: CrystalRepository): BreakResult {
        val rarityStr = rollCrystalRarity(context)
        repo.addCrystal(rarityStr)

        val buff = BuffManager.maybeDropRandomBuff(context)

        val nextRarity = rollCrystalRarity(context)
        generateNewRock(context, nextRarity)

        saveData(context)

        return BreakResult(
            rarity = rarityStr,
            grantedBuff = buff?.type,
            amount = buff?.amount ?: 0
        )
    }

    // ───────────────────────────────────────────
    // RARITY LOGIC
    // ───────────────────────────────────────────
    fun getBudgetStatus(context: Context): BudgetStatus {
        val prefs = context.getSharedPreferences("BUDGET_DATA", Context.MODE_PRIVATE)
        val within = prefs.getBoolean("stayed_within_budget", false)
        val belowMin = prefs.getBoolean("below_min_budget", false)

        return when {
            belowMin -> BudgetStatus.BELOW_MIN
            within -> BudgetStatus.WITHIN
            else -> BudgetStatus.NORMAL
        }
    }

    fun rollCrystalRarity(context: Context): String {
        val roll = Random.nextInt(100)

        return when (getBudgetStatus(context)) {
            BudgetStatus.NORMAL -> when {
                roll < 70 -> "COMMON"
                roll < 95 -> "RARE"
                else -> "LEGENDARY"
            }
            BudgetStatus.WITHIN -> when {
                roll < 50 -> "COMMON"
                roll < 90 -> "RARE"
                else -> "LEGENDARY"
            }
            BudgetStatus.BELOW_MIN -> when {
                roll < 20 -> "COMMON"
                roll < 60 -> "RARE"
                else -> "LEGENDARY"
            }
        }
    }

    private fun mapRarity(str: String): Rarity =
        when (str.uppercase()) {
            "RARE" -> Rarity.RARE
            "LEGENDARY" -> Rarity.LEGENDARY
            else -> Rarity.COMMON
        }

    fun generateNewRock(context: Context, forcedRarity: String? = null) {
        val rarityStr = forcedRarity ?: rollCrystalRarity(context)
        val rarityEnum = mapRarity(rarityStr)

        val swingsRequired = when (rarityEnum) {
            Rarity.COMMON -> 10
            Rarity.RARE -> 20
            Rarity.LEGENDARY -> 30
        }

        currentRock = Rock(
            rarity = rarityEnum,
            swingsRequired = swingsRequired,
            swingsUsed = 0
        )
    }
}
