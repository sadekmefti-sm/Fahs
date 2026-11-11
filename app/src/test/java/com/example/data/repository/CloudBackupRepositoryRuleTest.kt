package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.InspectionEntity
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudBackupRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun sampleInspection() = InspectionEntity(
        id = 42L,
        carBrand = "Renault",
        carModel = "Clio 4",
        carGeneration = "Phase 2",
        carYear = 2018,
        fuelType = "DIESEL",
        gearboxType = "MANUELLE",
        engineDescription = "1.5 dCi 90ch",
        vin = "VF15RJL0A58492011",
        plateNumber = "04821 118 16",
        inspectionMode = "FULL",
        overallScore = 85,
        verdict = "GOOD",
        status = "COMPLETED",
        totalEstimatedCostDzd = 15000,
        notes = "RAS",
        totalQuestions = 24,
        answeredQuestions = 24,
        problemCount = 1
    )

    @Test
    fun backupAndFetch_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = CloudBackupRepository(firestore, auth)
        val docId = "insp_${UUID.randomUUID()}"

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.backupInspection(sampleInspection(), docIdOverride = docId)
        }
        assertTrue(createRes.isSuccess)

        val listRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getBackedUpInspections()
        }
        assertTrue(listRes.isSuccess)
        assertTrue(listRes.getOrThrow().any { it.id == docId })
    }

    @Test
    fun getBackupById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudBackupRepository(firestore, auth)
        val docId = "insp_${UUID.randomUUID()}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.backupInspection(sampleInspection(), docIdOverride = docId).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = CloudBackupRepository(firestore, auth)
        val bobRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBackupById(aliceUid, docId)
        }
        assertTrue(bobRes.isFailure)
        val fsEx = bobRes.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeBackups_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repo = CloudBackupRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeBackedUpInspections().first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            assertTrue(e is IllegalStateException || e.unwrapFirestoreException() != null)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
