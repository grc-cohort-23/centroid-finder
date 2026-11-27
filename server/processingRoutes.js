const express = require("express");
const path = require("node:path");
const fs = require("node:fs/promises");
const { spawn } = require("node:child_process");
const { once } = require("node:events");
const { randomUUID } = require("node:crypto");

const router = express.Router();
const projectRoot = path.resolve(__dirname, "..");

const videosDirectory = path.resolve(
  projectRoot,
  process.env.VIDEOS_DIR || "processor/sampleInput"
);

const jobsDirectory = path.resolve(
  projectRoot,
  process.env.JOBS_DIR || "server/jobs"
);

const resultsDirectory = path.resolve(
  projectRoot,
  process.env.RESULTS_DIR || "server/results"
);

const jobIdPattern =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

router.post("/process/:filename", async (req, res) => {
  const filename = req.params.filename;
  const { targetColor, threshold } = req.query;

  if (
    typeof targetColor !== "string" ||
    typeof threshold !== "string" ||
    targetColor === "" ||
    threshold === ""
  ) {
    return res.status(400).json({
      error: "Missing targetColor or threshold query parameter.",
    });
  }

  if (
    !/^[0-9a-f]{6}$/i.test(targetColor) ||
    !/^\d+$/.test(threshold) ||
    !Number.isSafeInteger(Number(threshold))
  ) {
    return res.status(400).json({
      error: "Use a six-digit hex color and a non-negative integer threshold.",
    });
  }

  if (
    filename === "." ||
    filename === ".." ||
    filename.includes("/") ||
    filename.includes("\\") ||
    filename.includes("\0")
  ) {
    return res.status(400).json({ error: "Invalid filename" });
  }

  const inputPath = path.join(videosDirectory, filename);

  try {
    const video = await fs.stat(inputPath);

    if (!video.isFile()) {
      return res.status(404).json({ error: "Video not found" });
    }
  } catch (error) {
    if (error.code === "ENOENT") {
      return res.status(404).json({ error: "Video not found" });
    }

    console.error(error);
    return res.status(500).json({ error: "Error starting job" });
  }

  try {
    await fs.mkdir(jobsDirectory, { recursive: true });
    await fs.mkdir(resultsDirectory, { recursive: true });

    const jobId = randomUUID();
    const jobPath = path.join(jobsDirectory, `${jobId}.json`);

    const job = {
      status: "processing",
      javaBin: process.env.JAVA_BIN || "java",
      jarPath: path.resolve(
        projectRoot,
        process.env.JAR_PATH ||
          "processor/target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar"
      ),
      inputPath,
      outputPath: path.join(resultsDirectory, `${jobId}.csv`),
      targetColor,
      threshold: Number(threshold),
      result: `/results/${jobId}.csv`,
    };

    await fs.writeFile(jobPath, JSON.stringify(job, null, 2));

    try {
      const worker = spawn(
        process.execPath,
        [path.join(__dirname, "processJob.js"), jobPath],
        {
          detached: true,
          stdio: "ignore",
        }
      );

      await once(worker, "spawn");
      worker.unref();
    } catch (error) {
      await fs.unlink(jobPath);
      throw error;
    }

    res.status(202).json({ jobId });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Error starting job" });
  }
});

router.get("/process/:jobId/status", async (req, res) => {
  const jobId = req.params.jobId;

  if (!jobIdPattern.test(jobId)) {
    return res.status(404).json({ error: "Job ID not found" });
  }

  try {
    const jobPath = path.join(jobsDirectory, `${jobId}.json`);
    const job = JSON.parse(await fs.readFile(jobPath, "utf8"));

    if (job.status === "done") {
      return res.json({
        status: "done",
        result: job.result,
      });
    }

    if (job.status === "error") {
      return res.json({
        status: "error",
        error: job.error,
      });
    }

    res.json({ status: "processing" });
  } catch (error) {
    if (error.code === "ENOENT") {
      return res.status(404).json({ error: "Job ID not found" });
    }

    console.error(error);
    res.status(500).json({ error: "Error fetching job status" });
  }
});

router.get("/results/:filename", async (req, res) => {
  const filename = req.params.filename;
  const jobId = filename.slice(0, -4);

  if (!filename.endsWith(".csv") || !jobIdPattern.test(jobId)) {
    return res.status(404).json({ error: "Result not found" });
  }

  try {
    const jobPath = path.join(jobsDirectory, `${jobId}.json`);
    const job = JSON.parse(await fs.readFile(jobPath, "utf8"));

    if (job.status !== "done") {
      return res.status(404).json({ error: "Result is not ready" });
    }

    const csvPath = path.join(resultsDirectory, filename);

    res.download(csvPath, filename, (error) => {
      if (error && !res.headersSent) {
        res.status(500).json({ error: "Error downloading result" });
      }
    });
  } catch (error) {
    if (error.code === "ENOENT") {
      return res.status(404).json({ error: "Result not found" });
    }

    console.error(error);
    res.status(500).json({ error: "Error downloading result" });
  }
});

module.exports = router;
