package com.iie.thethreeburnouts.mineyourmoney

object WalletRepository {
    private val wallets = mutableListOf<Wallet>()

    fun addWallet(wallet: Wallet) {
        wallets.add(wallet)
    }

    fun getWallets(): List<Wallet> = wallets

    fun walletExists(name: String): Boolean {
        return wallets.any { it.name.equals(name, ignoreCase = true) }
    }

    fun getSortedWallets(sortType: SortType): List<Wallet> {
        return when (sortType) {
            SortType.DEFAULT -> wallets.sortedBy { it.name.lowercase() }
            SortType.BALANCE_HIGH -> wallets.sortedByDescending { it.balance }
            SortType.BALANCE_LOW -> wallets.sortedBy { it.balance }
        }
    }
}