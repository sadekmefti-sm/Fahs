package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.InspectionEntity
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class CloudInspectionBackup(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localId: Long = 0L,
    val carBrand: String = "",
    val carModel: String = "",
    val carGeneration: String = "",
    val carYear: Long = 2018L,
    val odometerKm: Long = 0L,
    val fuelType: String = "ESSENCE",
    val gearboxType: String = "MANUELLE",
    val engineDescription: String = "",
    val hasTurbo: Boolean = false,
    val isFourWheelDrive: Boolean = false,
    val bodyType: String = "CITADINE",
    val vin: String = "",
    val plateNumber: String = "",
    val inspectionMode: String = "FULL",
    val status: String = "IN_PROGRESS",
    val overallScore: Long = -1L,
    val verdict: String = "INCOMPLETE",
    val totalEstimatedCostDzd: Long = 0L,
    val problemCount: Long = 0L,
    val answeredQuestions: Long = 0L,
    val totalQuestions: Long = 0L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toLocalEntity(): InspectionEntity = InspectionEntity(
        id = 0L,
        carBrand = carBrand,
        carModel = carModel,
        carGeneration = carGeneration,
        carYear = carYear.toInt(),
        odometerKm = odometerKm.toInt(),
        fuelType = fuelType,
        gearboxType = gearboxType,
        engineDescription = engineDescription,
        hasTurbo = hasTurbo,
        isFourWheelDrive = isFourWheelDrive,
        bodyType = bodyType,
        vin = vin,
        plateNumber = plateNumber,
        inspectionMode = inspectionMode,
        overallScore = if (overallScore in 0L..100L) overallScore.toInt() else null,
        verdict = if (verdict == "INCOMPLETE" && overallScore !in 0L..100L) null else verdict,
        status = status,
        totalEstimatedCostDzd = totalEstimatedCostDzd.toInt(),
        notes = notes,
        totalQuestions = totalQuestions.toInt(),
        answeredQuestions = answeredQuestions.toInt(),
        problemCount = problemCount.toInt(),
        createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}

class CloudBackupRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Convenience constructor using the provisioned named database ID from firebase_applet_config.xml
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun backupInspection(inspection: InspectionEntity, docIdOverride: String? = null): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = docIdOverride ?: "insp_${inspection.id}"
        val docRef = db.collection("users").document(uid).collection("inspections").document(docId)

        val safeMode = if (inspection.inspectionMode.uppercase() in setOf("QUICK", "FULL")) {
            inspection.inspectionMode.uppercase()
        } else "FULL"
        val safeStatus = if (inspection.status.uppercase() in setOf("IN_PROGRESS", "COMPLETED")) {
            inspection.status.uppercase()
        } else "IN_PROGRESS"
        val safeVerdict = if ((inspection.verdict ?: "INCOMPLETE").uppercase() in setOf("GOOD", "NEGOTIATE", "AVOID", "INCOMPLETE")) {
            (inspection.verdict ?: "INCOMPLETE").uppercase()
        } else "INCOMPLETE"

        val payload = mapOf(
            "userId" to uid,
            "localId" to inspection.id,
            "carBrand" to inspection.carBrand.ifBlank { "Autre" }.take(80),
            "carModel" to inspection.carModel.ifBlank { "Véhicule" }.take(80),
            "carGeneration" to inspection.carGeneration.take(100),
            "carYear" to inspection.carYear.coerceIn(1970, 2035),
            "fuelType" to inspection.fuelType.ifBlank { "ESSENCE" }.take(40),
            "gearboxType" to inspection.gearboxType.ifBlank { "MANUELLE" }.take(50),
            "engineDescription" to inspection.engineDescription.take(100),
            "vin" to inspection.vin.take(30),
            "plateNumber" to inspection.plateNumber.take(40),
            "inspectionMode" to safeMode,
            "status" to safeStatus,
            "overallScore" to (inspection.overallScore?.coerceIn(0, 100) ?: -1),
            "verdict" to safeVerdict,
            "totalEstimatedCostDzd" to inspection.userQuotedRepairDzd.coerceAtLeast(0),
            "problemCount" to inspection.problemCount.coerceAtLeast(0),
            "answeredQuestions" to inspection.answeredQuestions.coerceAtLeast(0),
            "totalQuestions" to inspection.totalQuestions.coerceAtLeast(0),
            "notes" to inspection.notes.take(2000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getBackedUpInspections(): Result<List<CloudInspectionBackup>> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("inspections")
        try {
            val snap = colRef.whereEqualTo("userId", uid).get().await()
            snap.toObjects(CloudInspectionBackup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path)
            throw e
        }
    }

    suspend fun getBackupById(targetUserId: String, docId: String): Result<CloudInspectionBackup?> = runCatching {
        val docRef = db.collection("users").document(targetUserId).collection("inspections").document(docId)
        try {
            val snap = docRef.get().await()
            snap.toObject(CloudInspectionBackup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeBackedUpInspections(): Flow<List<CloudInspectionBackup>> = flow {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("inspections")
        emitAll(
            colRef.whereEqualTo("userId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudInspectionBackup::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun deleteBackup(docId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("inspections").document(docId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
