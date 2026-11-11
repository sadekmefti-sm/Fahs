package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carBrand: String,
    val carModel: String,
    val carGeneration: String = "",
    val carYear: Int,
    val fuelType: String = "ESSENCE", // ESSENCE, DIESEL, GPL, HYBRIDE
    val gearboxType: String = "MANUAL", // MANUAL, AUTOMATIC
    val engineDescription: String = "",
    val vin: String = "",
    val plateNumber: String = "",
    val inspectionMode: String = "FULL", // QUICK, FULL
    val overallScore: Int? = null,
    val verdict: String? = null, // GOOD, NEGOTIATE, AVOID
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED
    val totalEstimatedCostDzd: Int = 0,
    val notes: String = "",
    val totalQuestions: Int = 0,
    val answeredQuestions: Int = 0,
    val problemCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
