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
    val odometerKm: Int = 0,
    val hasTurbo: Boolean = false,
    val isFourWheelDrive: Boolean = false,
    val bodyType: String = "CITADINE",
    val vin: String = "",
    val plateNumber: String = "",
    val inspectionMode: String = "FULL", // QUICK, FULL
    val overallScore: Int? = null,
    val verdict: String? = null, // GOOD, NEGOTIATE, AVOID
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED
    val totalEstimatedCostDzd: Int = 0,
    val sellerAskingPriceMillions: Double = 0.0,
    val userQuotedRepairDzd: Int = 0,
    val hasObdScan: Boolean = false,
    val obdCommunicationSuccess: Boolean = false,
    val obdProtocolDescription: String = "",
    val obdDtcCodesSummary: String = "",
    val obdDtcCount: Int = 0,
    val obdClearedStatus: String = "NOT_AVAILABLE",
    val obdDistanceSinceClearedKm: Int = -1,
    val obdWarmupsSinceCleared: Int = -1,
    val obdMilBulbLitAtKeyOn: Boolean = true,
    val obdMilCommandedByEcu: String = "NOT_AVAILABLE",
    val obdEcuVin: String = "",
    val obdEcuVinComparison: String = "NOT_AVAILABLE",
    val notes: String = "",
    val totalQuestions: Int = 0,
    val answeredQuestions: Int = 0,
    val problemCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
