package com.iie.thethreeburnouts.mineyourmoney.wallet

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WalletRepository(private val dao: WalletDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getWalletsLive(userId: Int) = dao.getAllWalletsLive(userId)

    suspend fun getAllWallets(userId: Int) = dao.getAllWallets(userId)

    // -----------------------------
    // LOCAL + FIRESTORE SAVE
    // -----------------------------
    suspend fun addWallet(wallet: Wallet) {
        // Always update timestamp BEFORE saving
        val updated = wallet.copy(
            updatedAt = System.currentTimeMillis()
        )

        // Save locally
        dao.addWallet(updated)

        // Upload to Firestore
        uploadWallet(updated)
    }

    suspend fun subtractFromWallet(walletId: Int, amountChange: Double){
        val wallet = dao.getWalletById(walletId) ?: return

        dao.subtractFromWallet(walletId, amountChange)

        // Read the updated wallet
        val updated = dao.getWalletById(walletId)?.copy(
            updatedAt = System.currentTimeMillis()
        )

        // Save updated timestamp locally
        updated?.let {
            dao.addWallet(it) // insertOrUpdate
            uploadWallet(it)
        }
    }

    suspend fun addToWallet(walletId: Int, amountChange: Double){
        val wallet = dao.getWalletById(walletId) ?: return

        dao.addToWallet(walletId, amountChange)

        // Read the updated wallet
        val updated = dao.getWalletById(walletId)?.copy(
            updatedAt = System.currentTimeMillis()
        )

        // Save updated timestamp locally
        updated?.let {
            dao.addWallet(it) // insertOrUpdate
            uploadWallet(it)
        }
    }

    suspend fun deleteWallet(wallet: Wallet) {
        val deleted = wallet.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        dao.addWallet(deleted) // mark locally
        deleteFromFirestore(wallet) // delete remotely
    }

    // -----------------------------
    // FIRESTORE UPLOAD
    // -----------------------------
    private suspend fun uploadWallet(wallet: Wallet) {
        val user = auth.currentUser ?: return

        val data = mapOf(
            "name" to wallet.name,
            "balance" to wallet.balance,
            "iconResId" to wallet.iconResId,
            "color" to wallet.color,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("wallets")
            .document(wallet.id.toString())
            .set(data)
            .await()
    }

    private suspend fun deleteFromFirestore(wallet: Wallet) {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("wallets")
            .document(wallet.id.toString())
            .delete()
            .await()

        dao.deleteWallet(wallet)
    }

    suspend fun downloadFromFirestore(userId: Int) {
        val user = auth.currentUser ?: return

        // 1. Fetch remote wallets
        val snapshot = firestore.collection("users")
            .document(user.uid)
            .collection("wallets")
            .get()
            .await()

        val remoteWallets = snapshot.documents.mapNotNull { doc ->
            val id = doc.id.toIntOrNull() ?: return@mapNotNull null
            Wallet(
                id = id,
                userId = userId,
                name = doc.getString("name") ?: "",
                balance = doc.getDouble("balance") ?: 0.0,
                iconResId = (doc.getLong("iconResId") ?: 0L).toInt(),
                color = (doc.getLong("color") ?: 0L).toInt(),
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        }

        // 2. Upload locally deleted wallets first
        val locallyDeleted = dao.getDeletedWallets(userId) // <-- you'll need this DAO query
        for (deleted in locallyDeleted) {
            firestore.collection("users")
                .document(user.uid)
                .collection("wallets")
                .document(deleted.id.toString())
                .delete()
                .await()

            dao.deleteWallet(deleted) // remove from Room after successful deletion
        }

        // 3. Handle additions and updates
        val localWallets = dao.getAllWalletsIncludingDeleted(userId) // include deleted for conflict resolution
        val localMap = localWallets.associateBy { it.id }

        for (remote in remoteWallets) {
            val local = localMap[remote.id]
            when {
                local == null -> dao.addWallet(remote) // new remote wallet
                remote.updatedAt > local.updatedAt -> dao.addWallet(remote) // remote newer
                local.updatedAt > remote.updatedAt -> uploadWallet(local) // local newer
            }
        }

        // 4. Remove local wallets that no longer exist remotely
        val remoteIds = remoteWallets.map { it.id }.toSet()
        val syncedLocalWallets = dao.getAllWallets(userId)

        for (local in syncedLocalWallets) {
            if (local.id !in remoteIds) {
                dao.deleteWallet(local)
            }
        }
    }
}