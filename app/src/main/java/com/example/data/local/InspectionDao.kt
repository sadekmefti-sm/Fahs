package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY updatedAt DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionById(id: Long): InspectionEntity?

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    fun observeInspectionById(id: Long): Flow<InspectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    @Delete
    suspend fun deleteInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Long)

    @Query("SELECT * FROM checklist_answers WHERE inspectionId = :inspectionId")
    fun getAnswersForInspection(inspectionId: Long): Flow<List<ChecklistAnswerEntity>>

    @Query("SELECT * FROM checklist_answers")
    fun getAllAnswers(): Flow<List<ChecklistAnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: ChecklistAnswerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<ChecklistAnswerEntity>)

    @Query("SELECT COUNT(*) FROM inspections WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inspections WHERE status = 'IN_PROGRESS'")
    fun getInProgressCount(): Flow<Int>
}
