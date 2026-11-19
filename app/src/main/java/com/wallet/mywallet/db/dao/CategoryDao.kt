package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wallet.mywallet.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name COLLATE NOCASE")
    fun observeByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
