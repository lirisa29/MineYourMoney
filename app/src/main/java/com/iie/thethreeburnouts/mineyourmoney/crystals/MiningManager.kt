package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context
import kotlin.random.Random

enum class BudgetStatus {
    NORMAL,
    WITHIN,
    BELOW_MIN
}

object MiningManager {

    var availableSwings = 0
    var lastReset: Long = 0

    lateinit var currentRock: Rock

    fun loadData(context: Context) {
        availableSwings = SwingStorage.getSwings(context)
        lastReset = SwingStorage.getLastReset(context)

        checkDailyReset(context)

        if (!::currentRock.isInitialized) {
            generateNewRock(context)
        }
    }

    fun saveData(context: Context) {
        SwingStorage.setSwings(context, availableSwings)
        SwingStorage.setLastReset(context, lastReset)
    }

    private fun currentDay(): Long {
        return System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    }

    private fun checkDailyReset(context: Context) {
        val today = currentDay()

        if (lastReset == 0L || today > lastReset) {

            var swings = 3

            val extraDays = BuffManager.getExtraSwingDays(context)
            if (extraDays > 0) {
                swings += 1
                BuffManager.addExtraSwingDay(context, -1)
            }

            availableSwings = swings
            lastReset = today
            saveData(context)
        }
    }

    fun addSwing(context: Context, amount: Int) {
        availableSwings += amount
        if (availableSwings < 0) availableSwings = 0
        saveData(context)
    }

    fun useSwing(context: Context): Boolean {
        if (availableSwings <= 0) return false

        val dmgBuff = BuffManager.getDamageBuff(context)

        availableSwings--
        currentRock.swingsUsed += (1 + dmgBuff)

        saveData(context)

        return currentRock.isBroken()
    }

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
        val status = getBudgetStatus(context)
        val roll = Random.nextInt(100)

        return when (status) {
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
            Rarity.COMMON -> 3
            Rarity.RARE -> 6
            Rarity.LEGENDARY -> 12
        }

        currentRock = Rock(
            rarity = rarityEnum,
            swingsRequired = swingsRequired,
            swingsUsed = 0
        )
    }
}
