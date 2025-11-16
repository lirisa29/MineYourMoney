package com.iie.thethreeburnouts.mineyourmoney.crystals

data class BreakResult(
    val rarity: String,
    val grantedBuff: BuffType?,   // null if there is no buff
    val amount: Int = 0           // amount for damage or days
)
