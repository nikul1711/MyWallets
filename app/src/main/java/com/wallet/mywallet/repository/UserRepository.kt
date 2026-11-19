package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.UserDao
import com.wallet.mywallet.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositories sit between ViewModels and DAOs. ViewModels never see SQL or DAOs.
 * If a data source changes later (cloud backup, for example), only the repository changes.
 */
class UserRepository(private val userDao: UserDao) {

    fun observeUser(): Flow<UserEntity?> = userDao.observe()

    suspend fun getUser(): UserEntity? = userDao.get()

    suspend fun updateProfile(displayName: String, email: String?, defaultCurrency: String) {
        val current = userDao.get() ?: return
        userDao.upsert(
            current.copy(
                displayName = displayName,
                email = email,
                defaultCurrency = defaultCurrency,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
