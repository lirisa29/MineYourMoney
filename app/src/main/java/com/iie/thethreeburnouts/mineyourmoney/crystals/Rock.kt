package com.iie.thethreeburnouts.mineyourmoney.crystals

data class Rock(
    val rarity: Rarity,
    val swingsRequired: Int,
    var swingsUsed: Int
) {
    fun isBroken(): Boolean = swingsUsed >= swingsRequired
}
