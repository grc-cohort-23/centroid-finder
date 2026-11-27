const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs/promises");
const os = require("node:os");
const path = require("node:path");
const { randomUUID } = require("node:crypto");
const { once } = require("node:events");

let server;
let baseUrl;
let testDirectory;
let jobsDirectory;
let resultsDirectory;

before(async () => {
  testDirectory = await fs.mkdtemp(
    path.join(os.tmpdir(), "centroid-tests-")
  );

  jobsDirectory = path.join(testDirectory, "jobs");
  resultsDirectory = path.join(testDirectory, "results");

  await fs.mkdir(jobsDirectory);
  await fs.mkdir(resultsDirectory);

  process.env.JOBS_DIR = jobsDirectory;
  process.env.RESULTS_DIR = resultsDirectory;

  const app = require("./index");
  server = app.listen(0);
  await once(server, "listening");

  baseUrl = `http://localhost:${server.address().port}`;
});

after(async () => {
  if (server) {
    await new Promise(resolve => server.close(resolve));
  }

  if (testDirectory) {
    await fs.rm(testDirectory, { recursive: true, force: true });
  }
});

async function createJob(details) {
  const jobId = randomUUID();

  await fs.writeFile(
    path.join(jobsDirectory, `${jobId}.json`),
    JSON.stringify(details)
  );

  return jobId;
}

test("returns 400 when processing settings are missing", async () => {
  const response = await fetch(`${baseUrl}/process/sample.mp4`, {
    method: "POST",
  });

  assert.equal(response.status, 400);
  assert.deepEqual(await response.json(), {
    error: "Missing targetColor or threshold query parameter.",
  });
});

test("returns 404 for an unknown job", async () => {
  const response = await fetch(
    `${baseUrl}/process/${randomUUID()}/status`
  );

  assert.equal(response.status, 404);
  assert.deepEqual(await response.json(), {
    error: "Job ID not found",
  });
});

test("downloads the CSV for a completed job", async () => {
  const jobId = await createJob({ status: "done" });
  const csv = "0.0,10,20\n0.5,-1,-1\n";

  await fs.writeFile(
    path.join(resultsDirectory, `${jobId}.csv`),
    csv
  );

  const response = await fetch(`${baseUrl}/results/${jobId}.csv`);

  assert.equal(response.status, 200);
  assert.match(
    response.headers.get("content-disposition"),
    /attachment/
  );
  assert.equal(await response.text(), csv);
});

test("keeps an unfinished CSV unavailable", async () => {
  const jobId = await createJob({ status: "processing" });

  await fs.writeFile(
    path.join(resultsDirectory, `${jobId}.csv`),
    "0.0,10,20\n"
  );

  const response = await fetch(`${baseUrl}/results/${jobId}.csv`);

  assert.equal(response.status, 404);
  assert.deepEqual(await response.json(), {
    error: "Result is not ready",
  });
});
  for (const color of ["fff", "gggggg", "1234567"]) {
    test(`rejects invalid target color: ${color}`, async () => {
      const settings = new URLSearchParams({
        targetColor: color,
        threshold: "164",
      });
  
      const response = await fetch(
        `${baseUrl}/process/sample.mp4?${settings}`,
        { method: "POST" }
      );
  
      assert.equal(response.status, 400);
      assert.deepEqual(await response.json(), {
        error: "Use a six-digit hex color and a non-negative integer threshold.",
      });
    });
  }
  
  for (const threshold of ["-1", "1.5", "abc"]) {
    test(`rejects invalid threshold: ${threshold}`, async () => {
      const settings = new URLSearchParams({
        targetColor: "ffa200",
        threshold,
      });
  
      const response = await fetch(
        `${baseUrl}/process/sample.mp4?${settings}`,
        { method: "POST" }
      );
  
      assert.equal(response.status, 400);
      assert.deepEqual(await response.json(), {
        error: "Use a six-digit hex color and a non-negative integer threshold.",
      });
    });
  }
  
  test("returns 400 for a filename containing a backslash", async () => {
    const filename = encodeURIComponent("bad\\name.mp4");
  
    const response = await fetch(
      `${baseUrl}/process/${filename}?targetColor=ffa200&threshold=164`,
      { method: "POST" }
    );
  
    assert.equal(response.status, 400);
    assert.deepEqual(await response.json(), {
      error: "Invalid filename",
    });
  
});