/**
 * Unit tests for the API endpoints in src/routes.js.
 *
 * The real JAR and ffmpeg are replaced with fakes, so these tests
 * run in milliseconds and don't need Java, ffmpeg, or a real video.
 * Supertest sends requests straight to the app without starting a server.
 */
const fs = require("fs");
const os = require("os");
const path = require("path");
const request = require("supertest");
const createApp = require("../src/app");
const { JobStore } = require("../src/services");

/*
 * Each test gets fresh, empty temporary folders,
 * so tests never touch the real videos/, results/, or jobs/ folders.
 */
let tempDir;
let videosDir;
let resultsDir;
let jobStore;

/*
 * Fake JAR runner. It doesn't run anything; it just remembers what it was
 * asked to do, so each test can decide when and how the "JAR" finishes.
 */
let processorCalls;
function fakeRunProcessor(args, onFinish) {
  processorCalls.push({ args, onFinish });
}

// Fake thumbnail maker: returns a few made-up bytes instead of running ffmpeg
const FAKE_JPEG = Buffer.from([0xff, 0xd8, 0xff, 0xd9]);
async function fakeMakeThumbnail() {
  return FAKE_JPEG;
}

// Builds the app with the fakes and the temporary folders
function buildApp(overrides = {}) {
  return createApp({
    videosDir,
    resultsDir,
    jobStore,
    runProcessor: fakeRunProcessor,
    makeThumbnail: fakeMakeThumbnail,
    ...overrides,
  });
}

// Lets the fake JAR's "finish" callback save its update before checking
function waitForSave() {
  return new Promise((resolve) => setTimeout(resolve, 20));
}

beforeEach(() => {
  tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "salamander-test-"));
  videosDir = path.join(tempDir, "videos");
  resultsDir = path.join(tempDir, "results");
  const jobsDir = path.join(tempDir, "jobs");
  fs.mkdirSync(videosDir);
  fs.mkdirSync(resultsDir);
  fs.mkdirSync(jobsDir);

  // One fake video to work with
  fs.writeFileSync(path.join(videosDir, "ensantina.mp4"), "fake video");

  jobStore = new JobStore(jobsDir);
  processorCalls = [];
});

afterEach(() => {
  fs.rmSync(tempDir, { recursive: true, force: true });
});

// ============================================================
// GET /api/videos
// ============================================================
describe("GET /api/videos", () => {
  test("returns the video file names", async () => {
    fs.writeFileSync(path.join(videosDir, "second.mov"), "fake video");

    const res = await request(buildApp()).get("/api/videos");

    expect(res.status).toBe(200);
    expect(res.body.sort()).toEqual(["ensantina.mp4", "second.mov"]);
  });

  test("skips files that aren't videos", async () => {
    fs.writeFileSync(path.join(videosDir, "notes.txt"), "not a video");
    fs.writeFileSync(path.join(videosDir, ".DS_Store"), "mac junk");

    const res = await request(buildApp()).get("/api/videos");

    expect(res.body).toEqual(["ensantina.mp4"]);
  });

  test("counts uppercase extensions as videos", async () => {
    fs.writeFileSync(path.join(videosDir, "PHONE.MP4"), "fake video");

    const res = await request(buildApp()).get("/api/videos");

    expect(res.body).toContain("PHONE.MP4");
  });

  test("returns an empty list when there are no videos", async () => {
    fs.rmSync(path.join(videosDir, "ensantina.mp4"));

    const res = await request(buildApp()).get("/api/videos");

    expect(res.status).toBe(200);
    expect(res.body).toEqual([]);
  });

  test("returns 500 when the videos folder doesn't exist", async () => {
    const app = buildApp({ videosDir: path.join(tempDir, "missing") });

    const res = await request(app).get("/api/videos");

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: "Error reading video directory" });
  });
});

// ============================================================
// GET /thumbnail/:filename
// ============================================================
describe("GET /thumbnail/:filename", () => {
  test("returns a JPEG for an existing video", async () => {
    const res = await request(buildApp()).get("/thumbnail/ensantina.mp4");

    expect(res.status).toBe(200);
    expect(res.headers["content-type"]).toMatch(/image\/jpeg/);
    expect(Buffer.compare(res.body, FAKE_JPEG)).toBe(0);
  });

  test("returns 500 when the video doesn't exist", async () => {
    const res = await request(buildApp()).get("/thumbnail/nope.mp4");

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: "Error generating thumbnail" });
  });

  test("returns 500 when making the thumbnail fails", async () => {
    const failingThumbnail = async () => {
      throw new Error("ffmpeg broke");
    };
    const app = buildApp({ makeThumbnail: failingThumbnail });

    const res = await request(app).get("/thumbnail/ensantina.mp4");

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: "Error generating thumbnail" });
  });
});

// ============================================================
// POST /process/:filename
// ============================================================
describe("POST /process/:filename", () => {
  test("starts a job and answers 202 with a jobId", async () => {
    const res = await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: "5C0005", threshold: "80" });

    expect(res.status).toBe(202);
    expect(res.body.jobId).toMatch(/^[0-9a-f-]{36}$/);
  });

  test("passes the right values to the JAR", async () => {
    const res = await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: "5C0005", threshold: "80" });

    expect(processorCalls).toHaveLength(1);
    const { args } = processorCalls[0];
    expect(args.videoPath).toBe(path.join(videosDir, "ensantina.mp4"));
    expect(args.resultPath).toBe(path.join(resultsDir, `${res.body.jobId}.csv`));
    expect(args.targetColor).toBe("5C0005");
    expect(args.threshold).toBe("80");
  });

  test("saves the job as processing", async () => {
    const res = await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: "5C0005", threshold: "80" });

    const job = await jobStore.find(res.body.jobId);
    expect(job.status).toBe("processing");
    expect(job.video).toBe("ensantina.mp4");
    expect(job.result).toBe(`/results/${res.body.jobId}.csv`);
  });

  test("removes a leading # from the color", async () => {
    await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: "#5C0005", threshold: "80" });

    expect(processorCalls[0].args.targetColor).toBe("5C0005");
  });

  test("gives each job a different ID", async () => {
    const app = buildApp();
    const first = await request(app).post("/process/ensantina.mp4").query({ targetColor: "5C0005", threshold: "80" });
    const second = await request(app).post("/process/ensantina.mp4").query({ targetColor: "5C0005", threshold: "80" });

    expect(first.body.jobId).not.toBe(second.body.jobId);
  });

  test("returns 400 when targetColor is missing", async () => {
    const res = await request(buildApp()).post("/process/ensantina.mp4").query({ threshold: "80" });

    expect(res.status).toBe(400);
    expect(res.body).toEqual({ error: "Missing targetColor or threshold query parameter." });
    expect(processorCalls).toHaveLength(0);
  });

  test("returns 400 when threshold is missing", async () => {
    const res = await request(buildApp()).post("/process/ensantina.mp4").query({ targetColor: "5C0005" });

    expect(res.status).toBe(400);
    expect(processorCalls).toHaveLength(0);
  });

  test.each(["zz", "12345", "1234567", "GGGGGG"])("returns 400 for a bad color: %s", async (color) => {
    const res = await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: color, threshold: "80" });

    expect(res.status).toBe(400);
    expect(processorCalls).toHaveLength(0);
  });

  test.each(["-5", "abc", "16.5"])("returns 400 for a bad threshold: %s", async (threshold) => {
    const res = await request(buildApp())
      .post("/process/ensantina.mp4")
      .query({ targetColor: "5C0005", threshold });

    expect(res.status).toBe(400);
    expect(processorCalls).toHaveLength(0);
  });

  test("returns 400 when the video doesn't exist", async () => {
    const res = await request(buildApp())
      .post("/process/nope.mp4")
      .query({ targetColor: "5C0005", threshold: "80" });

    expect(res.status).toBe(400);
    expect(processorCalls).toHaveLength(0);
  });
});

// ============================================================
// GET /process/:jobId/status
// ============================================================
describe("GET /process/:jobId/status", () => {
  // Starts a job and returns its ID
  async function startJob(app) {
    const res = await request(app)
      .post("/process/ensantina.mp4")
      .query({ targetColor: "5C0005", threshold: "80" });
    return res.body.jobId;
  }

  test("says processing while the JAR is still running", async () => {
    const app = buildApp();
    const jobId = await startJob(app);

    const res = await request(app).get(`/process/${jobId}/status`);

    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: "processing" });
  });

  test("says done, with the result link, when the JAR succeeds", async () => {
    const app = buildApp();
    const jobId = await startJob(app);

    // The fake JAR finishes with no error
    processorCalls[0].onFinish(null);
    await waitForSave();

    const res = await request(app).get(`/process/${jobId}/status`);

    expect(res.body).toEqual({ status: "done", result: `/results/${jobId}.csv` });
  });

  test("says error, with a reason, when the JAR fails", async () => {
    const app = buildApp();
    const jobId = await startJob(app);

    // The fake JAR finishes with an error
    processorCalls[0].onFinish(new Error("processor exited with code 1"));
    await waitForSave();

    const res = await request(app).get(`/process/${jobId}/status`);

    expect(res.body).toEqual({
      status: "error",
      error: "Error processing video: processor exited with code 1",
    });
  });

  test("returns 404 for a job ID that doesn't exist", async () => {
    const res = await request(buildApp()).get("/process/123e4567-e89b-12d3-a456-426614174000/status");

    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: "Job ID not found" });
  });

  test("returns 404 for something that isn't a job ID", async () => {
    const res = await request(buildApp()).get("/process/hello/status");

    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: "Job ID not found" });
  });
});