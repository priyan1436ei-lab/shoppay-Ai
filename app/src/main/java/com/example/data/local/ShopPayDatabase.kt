package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AiInsightEntity
import com.example.data.model.ShopProfile
import com.example.data.model.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        ShopProfile::class,
        AiInsightEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ShopPayDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun shopProfileDao(): ShopProfileDao
    abstract fun aiInsightDao(): AiInsightDao

    companion object {
        @Volatile
        private var INSTANCE: ShopPayDatabase? = null

        fun getDatabase(context: Context): ShopPayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopPayDatabase::class.java,
                    "shoppay_ai_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
