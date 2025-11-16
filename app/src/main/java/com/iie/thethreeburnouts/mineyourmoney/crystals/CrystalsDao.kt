package com.iie.thethreeburnouts.mineyourmoney.crystals

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CrystalsDao {
    @Insert
    suspend fun addCrystal(crystal: CrystalEntity)

    @Query("SELECT * FROM crystals ORDER BY dateUnlocked DESC")
    suspend fun getAllCrystals(): List<CrystalEntity>
}
