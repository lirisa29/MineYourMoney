package com.iie.thethreeburnouts.mineyourmoney

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class RecurringExpenseWorker (
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result = runBlocking {
        val TAG = "RecurringExpenseWorker"
        Log.d(TAG, "Worker started")

        val appContext = context.applicationContext
        val db = AppDatabase.getInstance(appContext)
        val expenseDao = db.expensesDao()
        val walletDao = db.walletDao()

        // Retrieve data passed from WorkManager
        val amount = inputData.getDouble("amount", 0.0)
        val note = inputData.getString("note") ?: ""
        val walletId = inputData.getInt("walletId", -1)
        val recurrence = inputData.getString("recurrence")
        val userId = inputData.getInt("userId", 0)

        Log.d(TAG, "Received inputData -> walletId=$walletId | amount=$amount | recurrence=$recurrence | userId=$userId")

        if (walletId == -1 || amount <= 0.0) {
            Log.e(TAG, "Invalid data! walletId or amount is missing")
            return@runBlocking Result.failure()
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
            Result.success()
        } else {
            Log.w(TAG, "Failed to add recurring expense (insufficient funds or DB error). Retrying 🔁")
            Result.retry()
        }
    }
}