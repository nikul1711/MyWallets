package com.wallet.mywallet.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.wallet.mywallet.db.entity.WalletEntity
import com.wallet.mywallet.db.model.WalletWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Upsert
    suspend fun upsert(wallet: WalletEntity): Long

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<WalletEntity>>

    /**
     * Balance = initial balance + all income - all expenses.
     * It is calculated on the fly, so it can never get out of sync with the transactions.
     */
    @Query(
        """
        SELECT w.*,
               w.initialBalance + COALESCE((
                   SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END)
                   FROM transactions t
                   WHERE t.walletId = w.id
               ), 0) AS balance
        FROM wallets w
        ORDER BY w.name COLLATE NOCASE
        """
    )
    fun observeAllWithBalance(): Flow<List<WalletWithBalance>>

    /** The distinct currencies the user has wallets in. Used by reports. */
    @Query("SELECT DISTINCT currency FROM wallets ORDER BY currency")
    fun observeCurrencies(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun count(): Int
}
