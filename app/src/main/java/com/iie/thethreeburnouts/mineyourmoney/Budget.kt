package com.iie.thethreeburnouts.mineyourmoney

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val monthlyLimit: Double,
    val totalSpent: Double,
    val lastUpdatedMonth: Int
)