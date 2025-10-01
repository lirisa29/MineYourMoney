import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iie.thethreeburnouts.mineyourmoney.Wallet
import com.iie.thethreeburnouts.mineyourmoney.WalletRepository

@Database(entities = [User::class, Wallet::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletRepository(): WalletRepository

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .allowMainThreadQueries() // For testing only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
