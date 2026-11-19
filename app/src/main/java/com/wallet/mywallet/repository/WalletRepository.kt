package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.WalletDao
import com.wallet.mywallet.db.entity.WalletEntity
import com.wallet.mywallet.db.model.WalletWithBalance
import kotlinx.coroutines.flow.Flow

class WalletRepository(private val walletDao: WalletDao) {

    fun observeWalletsWithBalance(): Flow<List<WalletWithBalance>> = walletDao.observeAllWithBalance()

    fun observeWallets(): Flow<List<WalletEntity>> = walletDao.observeAll()

    fun observeCurrencies(): Flow<List<String>> = walletDao.observeCurrencies()

    suspend fun getWallet(id: Long): WalletEntity? = walletDao.getById(id)

    suspend fun count(): Int = walletDao.count()

    suspend fun save(wallet: WalletEntity): Long = walletDao.upsert(wallet)

    suspend fun delete(wallet: WalletEntity) = walletDao.delete(wallet)
}
