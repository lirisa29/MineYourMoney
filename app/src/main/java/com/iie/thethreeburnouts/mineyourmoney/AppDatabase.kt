import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iie.thethreeburnouts.mineyourmoney.Expense
import com.iie.thethreeburnouts.mineyourmoney.ExpensesDao
import com.iie.thethreeburnouts.mineyourmoney.Wallet
import com.iie.thethreeburnouts.mineyourmoney.WalletDao

@Database(entities = [User::class, Wallet::class, Expense::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun expensesDao(): ExpensesDao

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
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // For testing only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
