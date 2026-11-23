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
test("each job's log file sits next to its JSON file", () => {
  expect(store.logFileFor("job-1")).toBe(path.join(tempDir, "job-1.log"));
});

// ============================================================
// failUnfinished: run when the server starts. A job still marked
// "processing" was cut off by the last shutdown and can never finish.
// ============================================================
describe("failUnfinished", () => {
  const REASON = "Error processing video: server restarted";

  test("marks a processing job as error and keeps its other fields", async () => {
    await store.save("job-1", { status: "processing", video: "ensantina.mp4", threshold: 80 });

    await store.failUnfinished(REASON);

    expect(await store.find("job-1")).toEqual({
      status: "error",
      error: REASON,
      video: "ensantina.mp4",
      threshold: 80,
    });
  });

  test("leaves finished jobs alone", async () => {
    await store.save("done-job", { status: "done", result: "/results/done-job.csv" });
    await store.save("failed-job", { status: "error", error: "Invalid threshold: 999 (exit code 1)" });

    await store.failUnfinished(REASON);

    expect(await store.find("done-job")).toEqual({ status: "done", result: "/results/done-job.csv" });
    expect(await store.find("failed-job")).toEqual({ status: "error", error: "Invalid threshold: 999 (exit code 1)" });
  });

  test("returns the IDs of the jobs it changed", async () => {
    await store.save("job-1", { status: "processing" });
    await store.save("job-2", { status: "done" });
    await store.save("job-3", { status: "processing" });

    const changed = await store.failUnfinished(REASON);

    expect(changed.sort()).toEqual(["job-1", "job-3"]);
  });

  test("returns an empty list when there are no jobs", async () => {
    expect(await store.failUnfinished(REASON)).toEqual([]);
  });

  test("ignores files that aren't job files, like the .log files", async () => {
    await store.save("job-1", { status: "processing" });
    fs.writeFileSync(path.join(tempDir, "job-1.log"), "Invalid threshold: 999\n");

    const changed = await store.failUnfinished(REASON);

    expect(changed).toEqual(["job-1"]);
    expect(fs.readFileSync(path.join(tempDir, "job-1.log"), "utf8")).toBe("Invalid threshold: 999\n");
  });

  test("skips a broken job file and still fixes the others", async () => {
    fs.writeFileSync(path.join(tempDir, "broken.json"), "{ not json");
    await store.save("job-1", { status: "processing" });
    const warn = jest.spyOn(console, "warn").mockImplementation(() => {});

    const changed = await store.failUnfinished(REASON);

    expect(changed).toEqual(["job-1"]);
    expect(fs.readFileSync(path.join(tempDir, "broken.json"), "utf8")).toBe("{ not json");
    expect(warn).toHaveBeenCalledWith(expect.stringContaining("broken"));
    warn.mockRestore();
  });
});
