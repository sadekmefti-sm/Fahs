package com.example.data.repository

import com.example.data.local.ChecklistAnswerEntity
import com.example.data.local.InspectionDao
import com.example.data.local.InspectionEntity
import kotlinx.coroutines.flow.Flow

class InspectionRepository(private val inspectionDao: InspectionDao) {

    val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()
    val allAnswers: Flow<List<ChecklistAnswerEntity>> = inspectionDao.getAllAnswers()
    val completedCount: Flow<Int> = inspectionDao.getCompletedCount()
    val inProgressCount: Flow<Int> = inspectionDao.getInProgressCount()

    fun observeInspection(id: Long): Flow<InspectionEntity?> {
        return inspectionDao.observeInspectionById(id)
    }

    suspend fun getInspection(id: Long): InspectionEntity? {
        return inspectionDao.getInspectionById(id)
    }

    suspend fun insertInspection(inspection: InspectionEntity): Long {
        return inspectionDao.insertInspection(inspection)
    }

    suspend fun updateInspection(inspection: InspectionEntity) {
        inspectionDao.updateInspection(inspection)
    }

    suspend fun deleteInspection(id: Long) {
        inspectionDao.deleteInspectionById(id)
    }

    fun getAnswers(inspectionId: Long): Flow<List<ChecklistAnswerEntity>> {
        return inspectionDao.getAnswersForInspection(inspectionId)
    }

    suspend fun saveAnswer(answer: ChecklistAnswerEntity): Long {
        return inspectionDao.insertAnswer(answer)
    }

    suspend fun saveAnswers(answers: List<ChecklistAnswerEntity>) {
        inspectionDao.insertAnswers(answers)
    }
}
