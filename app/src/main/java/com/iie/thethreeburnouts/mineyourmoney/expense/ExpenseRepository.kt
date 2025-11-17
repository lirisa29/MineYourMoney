package com.iie.thethreeburnouts.mineyourmoney.expense

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExpenseRepository(private val dao: ExpensesDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val expensesCol = firestore.collection("expenses")

    // Upload a single expense to Firestore
    suspend fun uploadExpense(expense: Expense) {
        val user = auth.currentUser ?: return

        val expensesCol = firestore.collection("users")
            .document(user.uid)
            .collection("expenses")

        // Make sure the Expense has the local userId
        val expenseToUpload = expense.copy(userId = user.uid.hashCode())
        expensesCol.document(expense.id.toString()).set(expenseToUpload).await()
    }

    // Mark deleted locally and upload deletion to Firestore
    suspend fun deleteExpense(expense: Expense) {
        val now = System.currentTimeMillis()
        dao.markDeleted(expense.id, now, now)

        val user = auth.currentUser ?: return
        val expensesCol = firestore.collection("users")
            .document(user.uid)
            .collection("expenses")

        expensesCol.document(expense.id.toString()).delete().await()
    }

    // Download expenses for this user and apply conflict resolution
    suspend fun downloadExpenses(userId: Int) {
        val user = auth.currentUser ?: return
        val expensesCol = firestore.collection("users")
            .document(user.uid)
            .collection("expenses")

        val snapshot = expensesCol.get().await()
        val remoteExpenses = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Expense::class.java)?.copy(userId = userId) // enforce correct userId
        }

        // Delete local expenses that were marked deleted
        val locallyDeleted = dao.getDeletedExpenses(userId)
        for (deleted in locallyDeleted) {
            expensesCol.document(deleted.id.toString()).delete().await()
            dao.deleteExpense(deleted)
        }

        // Sync additions and updates
        val localExpenses = dao.getAllExpensesIncludingDeleted(userId)
        val localMap = localExpenses.associateBy { it.id }

        for (remote in remoteExpenses) {
            val local = localMap[remote.id]
            when {
                local == null -> dao.addExpense(remote) // new remote expense
                remote.updatedAt > local.updatedAt -> dao.addExpense(remote) // remote newer
                local.updatedAt > remote.updatedAt -> uploadExpense(local) // local newer
            }
        }

        // Remove local expenses that no longer exist remotely
        val remoteIds = remoteExpenses.map { it.id }.toSet()
        val localSyncedExpenses = dao.getAllExpensesSync(userId)
        for (local in localSyncedExpenses) {
            if (local.id !in remoteIds) dao.deleteExpense(local)
        }
    }
}