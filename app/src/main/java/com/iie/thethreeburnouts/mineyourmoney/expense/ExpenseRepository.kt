package com.iie.thethreeburnouts.mineyourmoney.expense

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExpenseRepository(private val dao: ExpensesDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val expensesCol = firestore.collection("expenses")

    // Upload a single expense to Firestore
    suspend fun uploadExpense(expense: Expense) {
        expensesCol
            .document(expense.id.toString())
            .set(expense)
            .await()
    }

    // Mark deleted locally and upload deletion to Firestore
    suspend fun deleteExpense(expense: Expense) {
        val now = System.currentTimeMillis()
        dao.markDeleted(expense.id, now, now)
        firestore.collection("expenses").document(expense.id.toString()).delete().await()
    }

    // Download expenses for this user and apply conflict resolution
    suspend fun downloadExpenses(userId: Int) {
        val snapshot = expensesCol.whereEqualTo("userId", userId).get().await()
        val remoteExpenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }

        // 1. Upload local deleted expenses first
        val locallyDeleted = dao.getDeletedExpenses(userId)
        for (deleted in locallyDeleted) {
            firestore.collection("expenses").document(deleted.id.toString()).delete().await()
            dao.deleteExpense(deleted) // remove from Room after successful Firestore deletion
        }

        // 2. Handle additions and updates
        val localExpenses = dao.getAllExpensesIncludingDeleted(userId)
        val localMap = localExpenses.associateBy { it.id }

        for (remote in remoteExpenses) {
            val local = localMap[remote.id]
            when {
                local == null -> dao.addExpense(remote) // New remote expense
                remote.updatedAt > local.updatedAt -> dao.addExpense(remote) // Remote newer
                local.updatedAt > remote.updatedAt -> uploadExpense(local) // Local newer
            }
        }

        // After syncing, remove local expenses that no longer exist remotely
        val remoteIds = snapshot.documents.mapNotNull { it.getLong("id")?.toInt() }.toSet()
        val localSyncedExpenses = dao.getAllExpensesSync(userId)

        for (local in localSyncedExpenses) {
            if (local.id !in remoteIds) {
                dao.deleteExpense(local)
            }
        }
    }
}