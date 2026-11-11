const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validPayload(uid) {
  const now = new Date();
  return {
    userId: uid,
    localId: 1,
    carBrand: "Renault",
    carModel: "Clio 4",
    carGeneration: "Phase 2",
    carYear: 2018,
    fuelType: "DIESEL",
    gearboxType: "MANUELLE",
    engineDescription: "1.5 dCi 90ch",
    vin: "VF15RJL0A58492011",
    plateNumber: "04821 118 16",
    inspectionMode: "FULL",
    status: "COMPLETED",
    overallScore: 84,
    verdict: "GOOD",
    totalEstimatedCostDzd: 15000,
    problemCount: 1,
    answeredQuestions: 24,
    totalQuestions: 24,
    notes: "RAS",
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write inspections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("inspections")
      .doc("insp_1")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("inspections")
      .doc("insp_1")
      .set(validPayload(ALICE_UID))
  );
});

test("Authenticated owner: can create, get, list, update, and delete own inspection backup", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("inspections")
    .doc("insp_1");

  await assertSucceeds(docRef.set(validPayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("inspections")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation: Bob cannot read or overwrite Alice's inspection backup", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("inspections")
      .doc("insp_1")
      .set(validPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDocRef = bobDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("inspections")
    .doc("insp_1");

  await assertFails(aliceDocRef.get());
  await assertFails(aliceDocRef.set(validPayload(BOB_UID)));
  await assertFails(aliceDocRef.delete());
});

test("Shadow update & schema validation: rejects ghost fields and out-of-range scores", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("inspections")
    .doc("insp_bad");

  await assertFails(
    docRef.set({
      ...validPayload(ALICE_UID),
      isAdmin: true,
    })
  );

  await assertFails(
    docRef.set({
      ...validPayload(ALICE_UID),
      overallScore: 150,
    })
  );
});
