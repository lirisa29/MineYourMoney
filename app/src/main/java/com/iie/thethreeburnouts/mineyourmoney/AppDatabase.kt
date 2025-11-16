import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iie.thethreeburnouts.mineyourmoney.budget.Budget
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetDao
import com.iie.thethreeburnouts.mineyourmoney.expense.Expense
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpensesDao
import com.iie.thethreeburnouts.mineyourmoney.login.User
import com.iie.thethreeburnouts.mineyourmoney.login.UserDao
import com.iie.thethreeburnouts.mineyourmoney.wallet.Wallet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletDao

@Database(entities = [User::class, Wallet::class, Expense::class, Budget::class], version = 14)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun expensesDao(): ExpensesDao
    abstract fun budgetDao(): BudgetDao

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
