package com.iie.thethreeburnouts.mineyourmoney

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class RecurringExpenseWorker (
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) { //(App Dev Insights, 2024)

    override fun doWork(): Result = runBlocking { //(App Dev Insights, 2024)
        val TAG = "RecurringExpenseWorker"
        Log.d(TAG, "Worker started")

        val appContext = context.applicationContext
        val db = AppDatabase.getInstance(appContext)
        val expenseDao = db.expensesDao()
        val walletDao = db.walletDao()

        // Retrieve data passed from WorkManager
        val amount = inputData.getDouble("amount", 0.0) //(App Dev Insights, 2024)
        val note = inputData.getString("note") ?: "" //(App Dev Insights, 2024)
        val walletId = inputData.getInt("walletId", -1) //(App Dev Insights, 2024)
        val recurrence = inputData.getString("recurrence") //(App Dev Insights, 2024)
        val userId = inputData.getInt("userId", 0) //(App Dev Insights, 2024)

        Log.d(TAG, "Received inputData -> walletId=$walletId | amount=$amount | recurrence=$recurrence | userId=$userId")

        if (walletId == -1 || amount <= 0.0) {
            Log.e(TAG, "Invalid data! walletId or amount is missing")
            return@runBlocking Result.failure() //(App Dev Insights, 2024)
        }

        val expense = Expense(
            amount = amount,
            note = note,
            walletId = walletId,
            recurrence = recurrence,
            date = System.currentTimeMillis(),
            userId = userId
        )

        Log.d(TAG, "Attempting to insert recurring expense: $expense")

        val newExpenseId = expenseDao.checkIfSufficientFunds(expense, walletDao)

        if (newExpenseId != -1L) {
            Log.d(TAG, "Recurring expense added successfully (ID: $newExpenseId) ✅")
            Result.success()//(App Dev Insights, 2024)
        } else {
            Log.w(TAG, "Failed to add recurring expense (insufficient funds or DB error). Retrying 🔁")
            Result.retry() //(App Dev Insights, 2024)
        }
    }
}
//Reference List:
/* App Dev Insights. 2024. Work Manager — Android. [Online].
Available at: https://medium.com/@appdevinsights/work-manager-android-6ea8daad56ee [Accessed 6 October 2025). */