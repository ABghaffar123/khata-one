package com.khatabook.app.di

import android.content.Context
import com.khatabook.app.data.KhataDatabase
import com.khatabook.app.data.dao.CaptureDao
import com.khatabook.app.data.dao.CashAccountDao
import com.khatabook.app.data.dao.CustomerDao
import com.khatabook.app.data.dao.InvoiceItemDao
import com.khatabook.app.data.dao.ProductDao
import com.khatabook.app.data.dao.SupplierDao
import com.khatabook.app.data.dao.SupplierTransactionDao
import com.khatabook.app.data.dao.TransactionDao
import com.khatabook.app.data.dao.TransactionEditLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KhataDatabase {
        return KhataDatabase.getDatabase(context)
    }

    @Provides
    fun provideCustomerDao(database: KhataDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideTransactionDao(database: KhataDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCaptureDao(database: KhataDatabase): CaptureDao {
        return database.captureDao()
    }

    @Provides
    fun provideProductDao(database: KhataDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideSupplierDao(database: KhataDatabase): SupplierDao {
        return database.supplierDao()
    }

    @Provides
    fun provideSupplierTransactionDao(database: KhataDatabase): SupplierTransactionDao {
        return database.supplierTransactionDao()
    }

    @Provides
    fun provideInvoiceItemDao(database: KhataDatabase): InvoiceItemDao {
        return database.invoiceItemDao()
    }

    @Provides
    fun provideCashAccountDao(database: KhataDatabase): CashAccountDao {
        return database.cashAccountDao()
    }

    @Provides
    fun provideTransactionEditLogDao(database: KhataDatabase): TransactionEditLogDao {
        return database.transactionEditLogDao()
    }
}
