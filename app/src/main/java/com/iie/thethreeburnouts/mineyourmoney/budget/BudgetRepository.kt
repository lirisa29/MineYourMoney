package com.iie.thethreeburnouts.mineyourmoney.budget

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BudgetRepository (private val dao: BudgetDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getBudgetLive(userId: Int) = dao.getBudgetLive(userId)

    suspend fun getBudget(userId: Int) = dao.getBudget(userId)

    suspend fun saveBudget(budget: Budget) {
        val updated = budget.copy(updatedAt = System.currentTimeMillis())
        dao.insertOrUpdateBudget(updated)

        uploadToFirestore(updated)
    }

    suspend fun addSpending(userId: Int, amount: Double) {
        dao.addSpending(userId, amount)

        val updated = dao.getBudget(userId)?.copy(
            updatedAt = System.currentTimeMillis()
        )
        updated?.let {
            dao.insertOrUpdateBudget(it)
            uploadToFirestore(it)
        }
    }

    suspend fun refundSpending(userId: Int, amount: Double) {
        dao.refundSpending(userId, amount)

        val updated = dao.getBudget(userId)?.copy(
            updatedAt = System.currentTimeMillis()
        )
        updated?.let {
            dao.insertOrUpdateBudget(it)
            uploadToFirestore(it)
        }
    }

    private suspend fun uploadToFirestore(budget: Budget) {
        val user = auth.currentUser ?: return

        val data = mapOf(
            "minLimit" to budget.minLimit,
            "maxLimit" to budget.maxLimit,
            "totalSpent" to budget.totalSpent,
            "lastUpdatedMonth" to budget.lastUpdatedMonth,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("budget")
            .document("budgetDoc")
            .set(data)
            .await()
    }

    suspend fun downloadFromFirestore(userId: Int) {
        val user = auth.currentUser ?: return

        val snapshot = firestore.collection("users")
            .document(user.uid)
            .collection("budget")
            .document("budgetDoc")
            .get()
            .await()

        if (!snapshot.exists()) return

        val remote = Budget(
            userId = userId,
            id = dao.getBudget(userId)?.id ?: 0, // preserve existing local id
            minLimit = snapshot.getDouble("minLimit") ?: 0.0,
            maxLimit = snapshot.getDouble("maxLimit") ?: 0.0,
            totalSpent = snapshot.getDouble("totalSpent") ?: 0.0,
            lastUpdatedMonth = (snapshot.getLong("lastUpdatedMonth") ?: 0L).toInt(),
            updatedAt = snapshot.getLong("updatedAt") ?: 0L
        )

        val local = dao.getBudget(userId)

        when {
            local == null -> {
                // No local → save remote
                dao.insertOrUpdateBudget(remote)
            }

            remote.updatedAt > local.updatedAt -> {
                // Remote newer → overwrite local
                dao.insertOrUpdateBudget(remote)
            }

            local.updatedAt > remote.updatedAt -> {
                // Local newer → upload local
                uploadToFirestore(local)
            }
        }
    }
}