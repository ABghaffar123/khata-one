package com.khatabook.app.data

import androidx.room.TypeConverter
import com.khatabook.app.data.entity.ProductUnit
import com.khatabook.app.data.entity.SupplierTransactionType
import com.khatabook.app.data.entity.TransactionType

class Converters {

    @TypeConverter
    fun fromTransactionType(type: TransactionType): Int = type.index

    @TypeConverter
    fun toTransactionType(index: Int): TransactionType = TransactionType.values()[index]

    @TypeConverter
    fun fromSupplierTransactionType(type: SupplierTransactionType): Int = type.index

    @TypeConverter
    fun toSupplierTransactionType(index: Int): SupplierTransactionType =
        SupplierTransactionType.values()[index]

    @TypeConverter
    fun fromProductUnit(unit: ProductUnit): String = unit.name

    @TypeConverter
    fun toProductUnit(name: String): ProductUnit = ProductUnit.valueOf(name)
}
