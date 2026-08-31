package com.example.data.local

import androidx.room.*
import com.example.data.model.AiInsightEntity
import com.example.data.model.ShopProfile
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE shopId = :shopId ORDER BY transactionDate DESC, transactionTime DESC, id DESC")
    fun getAllTransactions(shopId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId ORDER BY transactionDate DESC, transactionTime DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(shopId: String, limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND transactionDate = :date ORDER BY transactionTime DESC, id DESC")
    fun getTransactionsForDateFlow(shopId: String, date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND transactionDate = :date AND status = 'SUCCESSFUL'")
    suspend fun getTransactionsForDate(shopId: String, date: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND category = :category ORDER BY transactionDate DESC, transactionTime DESC")
    fun getTransactionsByCategory(shopId: String, category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND transactionDate >= :startDate AND transactionDate <= :endDate ORDER BY transactionDate DESC, transactionTime DESC")
    suspend fun getTransactionsInRange(shopId: String, startDate: String, endDate: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND transactionDate >= :startDate AND transactionDate <= :endDate ORDER BY transactionDate DESC, transactionTime DESC")
    fun getTransactionsInRangeFlow(shopId: String, startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND isUnusual = 1")
    fun getUnusualTransactions(shopId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shopId = :shopId AND transactionId = :txnId LIMIT 1")
    suspend fun findByTransactionId(shopId: String, txnId: String): TransactionEntity?

    @Query("SELECT transactionId FROM transactions WHERE shopId = :shopId")
    suspend fun getAllTransactionIds(shopId: String): List<String>

    @Query("SELECT SUM(amount) FROM transactions WHERE shopId = :shopId AND transactionDate = :date AND status = 'SUCCESSFUL'")
    suspend fun getDailyTotalAmount(shopId: String, date: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions WHERE shopId = :shopId")
    suspend fun deleteAllForShop(shopId: String)
}

@Dao
interface ShopProfileDao {
    @Query("SELECT * FROM shops LIMIT 1")
    fun getActiveProfile(): Flow<ShopProfile?>

    @Query("SELECT * FROM shops LIMIT 1")
    suspend fun getActiveProfileDirect(): ShopProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ShopProfile)

    @Query("UPDATE shops SET monthlyRevenueTarget = :target WHERE id = :shopId")
    suspend fun updateMonthlyTarget(shopId: String, target: Double)

    @Query("DELETE FROM shops")
    suspend fun clearProfiles()
}

@Dao
interface AiInsightDao {
    @Query("SELECT * FROM ai_insights WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getInsights(shopId: String): Flow<List<AiInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsights(insights: List<AiInsightEntity>)

    @Query("DELETE FROM ai_insights WHERE shopId = :shopId")
    suspend fun deleteInsightsForShop(shopId: String)
}
