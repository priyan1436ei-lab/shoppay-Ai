package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ShopPayDatabase
import com.example.data.local.TransactionDao
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomTransactionDatabaseTest {

    private lateinit var db: ShopPayDatabase
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShopPayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = db.transactionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveDailyTransactionWithAllRequiredFields() = runBlocking {
        val transaction = TransactionEntity(
            shopId = "shop_test",
            transactionId = "TXN-TEST-101",
            customerName = "Priya Sharma",
            amount = 1250.50,
            category = "Grocery",
            description = "Organic spices and dairy items",
            paymentMethod = PaymentMethod.UPI.name,
            status = TransactionStatus.SUCCESSFUL.name,
            transactionDate = "2026-08-31",
            transactionTime = "14:30",
            customerNote = "Organic spices and dairy items"
        )

        val id = transactionDao.insertTransaction(transaction)
        assertTrue(id > 0)

        val retrieved = transactionDao.findByTransactionId("shop_test", "TXN-TEST-101")
        assertNotNull(retrieved)
        assertEquals("2026-08-31", retrieved?.transactionDate)
        assertEquals(1250.50, retrieved?.amount ?: 0.0, 0.001)
        assertEquals("Grocery", retrieved?.category)
        assertEquals("Organic spices and dairy items", retrieved?.description)
        assertEquals(PaymentMethod.UPI.name, retrieved?.paymentMethod)
        assertEquals(TransactionStatus.SUCCESSFUL.name, retrieved?.status)
    }

    @Test
    fun queryDailyTransactionsByDateAndCalculateTotal() = runBlocking {
        val txn1 = TransactionEntity(
            shopId = "shop_test",
            transactionId = "TXN-D1",
            customerName = "Customer 1",
            amount = 500.0,
            category = "Retail",
            description = "Stationery notebook",
            transactionDate = "2026-08-31"
        )
        val txn2 = TransactionEntity(
            shopId = "shop_test",
            transactionId = "TXN-D2",
            customerName = "Customer 2",
            amount = 1500.0,
            category = "Retail",
            description = "Desk lamp",
            transactionDate = "2026-08-31"
        )
        val txnOtherDay = TransactionEntity(
            shopId = "shop_test",
            transactionId = "TXN-D3",
            customerName = "Customer 3",
            amount = 700.0,
            category = "Retail",
            description = "Other day purchase",
            transactionDate = "2026-08-30"
        )

        transactionDao.insertTransactions(listOf(txn1, txn2, txnOtherDay))

        val todayTxns = transactionDao.getTransactionsForDate("shop_test", "2026-08-31")
        assertEquals(2, todayTxns.size)

        val dailyTotal = transactionDao.getDailyTotalAmount("shop_test", "2026-08-31")
        assertEquals(2000.0, dailyTotal ?: 0.0, 0.001)

        val categoryTxns = transactionDao.getTransactionsByCategory("shop_test", "Retail").first()
        assertEquals(3, categoryTxns.size)
    }
}
