package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_answers",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["inspectionId", "itemId"], unique = true)]
)
data class ChecklistAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inspectionId: Long,
    val itemId: String,
    val category: String,
    val questionFr: String,
    val questionAr: String,
    val questionEn: String,
    val status: String = "UNCHECKED", // GOOD, PROBLEM, UNCHECKED
    val severity: String = "IMPORTANT", // MINOR, IMPORTANT, CRITICAL
    val estimatedCostDzd: Int = 0,
    val note: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
