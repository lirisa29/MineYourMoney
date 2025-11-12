package com.iie.thethreeburnouts.mineyourmoney.budget

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val minLimit: Double,
    val maxLimit: Double,
    val totalSpent: Double,
    val lastUpdatedMonth: Int
)