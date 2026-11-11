import { test, describe, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} from '@firebase/rules-unit-testing';
import {
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  collection,
  getDocs,
  query,
  where,
  serverTimestamp
} from 'firebase/firestore';

let testEnv;

function validInspectionPayload(userId = 'alice') {
  return {
    userId,
    localId: 1,
    carBrand: 'Renault',
    carModel: 'Symbol',
    carGeneration: 'Symbol 3 (2013–2021)',
    carYear: 2018,
    fuelType: 'ESSENCE',
    gearboxType: 'MANUELLE',
    engineDescription: '1.2 16V',
    vin: 'VF14SRAP458901234',
    plateNumber: '04821 118 16',
    inspectionMode: 'FULL',
    status: 'COMPLETED',
    overallScore: 86,
    verdict: 'GOOD',
    totalEstimatedCostDzd: 15000,
    problemCount: 1,
    answeredQuestions: 32,
    totalQuestions: 32,
    notes: 'Bon état général',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  };
}

describe('Fahs Cloud Inspection Backup & Shop Settings Rules', () => {
  before(async () => {
    testEnv = await initializeTestEnvironment({
      projectId: 'demo-fahs-inspections',
      firestore: {
        rules: readFileSync('firestore.rules', 'utf8')
      }
    });
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  after(async () => {
    if (testEnv) {
      await testEnv.cleanup();
    }
  });

  test('authenticated owner can create, get, list, update (including incomplete -1 score), and delete their own inspection backup', async () => {
    const aliceDb = testEnv.authenticatedContext('alice').firestore();
    const docRef = doc(aliceDb, 'users/alice/inspections/insp_1');

    await assertSucceeds(setDoc(docRef, validInspectionPayload('alice')));
    await assertSucceeds(getDoc(docRef));

    const q = query(collection(aliceDb, 'users/alice/inspections'), where('userId', '==', 'alice'));
    await assertSucceeds(getDocs(q));

    await assertSucceeds(updateDoc(docRef, {
      overallScore: -1,
      verdict: 'INCOMPLETE',
      updatedAt: serverTimestamp()
    }));

    await assertSucceeds(deleteDoc(docRef));
  });

  test('unauthenticated user cannot read or write any inspection backup', async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), 'users/alice/inspections/insp_1'), validInspectionPayload('alice'));
    });

    const unauthDb = testEnv.unauthenticatedContext().firestore();
    const docRef = doc(unauthDb, 'users/alice/inspections/insp_1');

    await assertFails(getDoc(docRef));
    await assertFails(setDoc(docRef, validInspectionPayload('alice')));
    await assertFails(deleteDoc(docRef));
  });

  test('another authenticated user (bob) cannot read, update, or delete alice inspection backups', async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), 'users/alice/inspections/insp_1'), validInspectionPayload('alice'));
    });

    const bobDb = testEnv.authenticatedContext('bob').firestore();
    const aliceDocFromBob = doc(bobDb, 'users/alice/inspections/insp_1');

    await assertFails(getDoc(aliceDocFromBob));
    await assertFails(updateDoc(aliceDocFromBob, { overallScore: 10, updatedAt: serverTimestamp() }));
    await assertFails(deleteDoc(aliceDocFromBob));
  });

  test('rejects invalid payload with out-of-range score or unexpected fields', async () => {
    const aliceDb = testEnv.authenticatedContext('alice').firestore();
    const badScoreRef = doc(aliceDb, 'users/alice/inspections/insp_bad_score');

    await assertFails(setDoc(badScoreRef, {
      ...validInspectionPayload('alice'),
      overallScore: 150
    }));

    const extraFieldRef = doc(aliceDb, 'users/alice/inspections/insp_extra');
    await assertFails(setDoc(extraFieldRef, {
      ...validInspectionPayload('alice'),
      isAdmin: true
    }));
  });

  test('allows public read-only get on /shop_settings/config and denies client writes', async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), 'shop_settings/config'), {
        whatsappNumber: '+971524830059',
        contactEmail: 'sadekmefti@gmail.com',
        donglePriceDzd: 2500,
        inStock: true
      });
    });

    const anonDb = testEnv.unauthenticatedContext().firestore();
    const configRef = doc(anonDb, 'shop_settings/config');
    await assertSucceeds(getDoc(configRef));

    await assertFails(setDoc(configRef, { donglePriceDzd: 100 }));
    const aliceDb = testEnv.authenticatedContext('alice').firestore();
    await assertFails(setDoc(doc(aliceDb, 'shop_settings/config'), { donglePriceDzd: 100 }));
  });
});
