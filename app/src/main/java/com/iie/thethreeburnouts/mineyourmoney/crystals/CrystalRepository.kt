package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context

class CrystalRepository(context: Context) {

    private val dao = CrystalDatabase.getInstance(context).crystalsDao()

    // Accept STRING instead of Rarity enum
    suspend fun addCrystal(rarity: String) {
        dao.addCrystal(
            CrystalEntity(
                rarity = rarity,  // already string
                dateUnlocked = System.currentTimeMillis()
            )
        )
    }

    suspend fun getCrystals(): List<CrystalEntity> {
        return dao.getAllCrystals()
    }
}
