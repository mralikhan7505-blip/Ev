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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read user profiles or memories", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.doc(`users/${ALICE_UID}`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/memories`).get());
});

test("Alice can create and read her own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}`).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.doc(`users/${ALICE_UID}`).get());
});

test("Bob cannot read or write Alice's memories (Cross-user isolation)", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.doc(`users/${ALICE_UID}/memories/mem1`).set({
      title: "Hacked Memory",
      content: "Bob should not write here",
      category: "PREFERENCE",
      isActive: true,
      createdAt: new Date(),
    })
  );
  await assertFails(bobDb.doc(`users/${ALICE_UID}/memories/mem1`).get());
});

test("Alice can create a learning topic and flashcards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/topics/topic1`).set({
      title: "Kotlin Compose",
      category: "Coding",
      summary: "Declarative UI",
      masteryPercent: 50,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/topics/topic1/flashcards/card1`).set({
      front: "What is state?",
      back: "Value that changes over time.",
      isMastered: false,
      createdAt: new Date(),
    })
  );
});
