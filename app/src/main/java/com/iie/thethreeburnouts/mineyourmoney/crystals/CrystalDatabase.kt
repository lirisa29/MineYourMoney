package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CrystalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CrystalDatabase : RoomDatabase() {

    abstract fun crystalsDao(): CrystalsDao

    companion object {
        @Volatile
        private var INSTANCE: CrystalDatabase? = null

        fun getInstance(context: Context): CrystalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrystalDatabase::class.java,
                    "crystals.db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
