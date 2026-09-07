package com.khatabook.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.khatabook.app.data.dao.CaptureDao
import com.khatabook.app.data.dao.CashAccountDao
import com.khatabook.app.data.dao.CustomerDao
import com.khatabook.app.data.dao.InvoiceItemDao
import com.khatabook.app.data.dao.ProductDao
import com.khatabook.app.data.dao.SupplierDao
import com.khatabook.app.data.dao.SupplierTransactionDao
import com.khatabook.app.data.dao.TransactionDao
import com.khatabook.app.data.dao.TransactionEditLogDao
import com.khatabook.app.data.entity.CaptureEntity
import com.khatabook.app.data.entity.CashAccountEntity
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.InvoiceItemEntity
import com.khatabook.app.data.entity.ProductEntity
import com.khatabook.app.data.entity.SupplierEntity
import com.khatabook.app.data.entity.SupplierTransactionEntity
import com.khatabook.app.data.entity.TransactionEditLogEntity
import com.khatabook.app.data.entity.TransactionEntity

@Database(
    entities = [
        CustomerEntity::class,
        TransactionEntity::class,
        CaptureEntity::class,
        ProductEntity::class,
        SupplierEntity::class,
        SupplierTransactionEntity::class,
        InvoiceItemEntity::class,
        CashAccountEntity::class,
        TransactionEditLogEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KhataDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun captureDao(): CaptureDao
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierTransactionDao(): SupplierTransactionDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun cashAccountDao(): CashAccountDao
    abstract fun transactionEditLogDao(): TransactionEditLogDao

    companion object {
        @Volatile
        private var INSTANCE: KhataDatabase? = null

        fun getDatabase(context: Context): KhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhataDatabase::class.java,
                    "khata_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
