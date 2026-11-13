/**
 * Unit tests for JobStore in src/services.js.
 * Each test uses its own temporary folder, so the real jobs/ folder is never touched.
 */
const fs = require("fs");
const os = require("os");
const path = require("path");
const { JobStore } = require("../src/services");

let tempDir;
let store;

beforeEach(() => {
  tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "jobstore-test-"));
  store = new JobStore(tempDir);
});

afterEach(() => {
  fs.rmSync(tempDir, { recursive: true, force: true });
});

test("save then find gives the same job back", async () => {
  const job = { status: "processing", video: "ensantina.mp4" };

  await store.save("job-1", job);

  expect(await store.find("job-1")).toEqual(job);
});

test("saves each job as its own JSON file", async () => {
  await store.save("job-1", { status: "processing" });

  expect(fs.existsSync(path.join(tempDir, "job-1.json"))).toBe(true);
});

test("find returns null for an unknown job", async () => {
  expect(await store.find("does-not-exist")).toBeNull();
});

test("update changes only the given fields", async () => {
  await store.save("job-1", { status: "processing", video: "ensantina.mp4" });

  await store.update("job-1", { status: "done" });

  expect(await store.find("job-1")).toEqual({ status: "done", video: "ensantina.mp4" });
});

test("update can add a new field", async () => {
  await store.save("job-1", { status: "processing" });

  await store.update("job-1", { status: "error", error: "something broke" });

  expect(await store.find("job-1")).toEqual({ status: "error", error: "something broke" });
});

test("jobs don't affect each other", async () => {
  await store.save("job-1", { status: "processing" });
  await store.save("job-2", { status: "processing" });

  await store.update("job-1", { status: "done" });

  expect((await store.find("job-2")).status).toBe("processing");
});