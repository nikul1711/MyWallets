package com.wallet.mywallet.repository

import com.wallet.mywallet.db.dao.CategoryDao
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.observeByType(type.name)
}
