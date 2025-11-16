package com.iie.thethreeburnouts.mineyourmoney.crystals

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crystals")
data class CrystalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rarity: String,
    val dateUnlocked: Long
)
