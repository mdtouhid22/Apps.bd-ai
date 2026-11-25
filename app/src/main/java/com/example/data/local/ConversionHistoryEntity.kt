package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_history")
data class ConversionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val originalNumber: String,
    val sourceBase: Int,
    val convertedNumber: String,
    val targetBase: Int,
    val binaryValue: String,
    val octalValue: String,
    val decimalValue: String,
    val hexValue: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
