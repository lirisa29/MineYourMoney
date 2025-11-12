package com.iie.thethreeburnouts.mineyourmoney.login

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "users") //(Google Developers Training team, 2024)
data class User( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val username: String, //(Google Developers Training team, 2024)
    val password: String //(Google Developers Training team, 2024)
) : Parcelable