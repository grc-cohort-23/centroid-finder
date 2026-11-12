/**
 * The API endpoints:
 *   GET  /api/videos
 *   GET  /thumbnail/:filename
 *   POST /process/:filename
 *   GET  /process/:jobId/status
 */
const express = require("express");
const fs = require("fs/promises");
const path = require("path");
const { v4: uuidv4 } = require("uuid");

// File types we count as videos
const VIDEO_EXTENSIONS = [".mp4", ".mov", ".avi", ".mkv"];

// 6 hex digits, with an optional # in front (like ff0000 or #ff0000)
const HEX_COLOR = /^#?[0-9a-fA-F]{6}$/;

// Digits only, so 0 or a positive whole number
const WHOLE_NUMBER = /^\d+$/;
// Job IDs are UUIDs
const UUID = /^[0-9a-fA-F-]{36}$/;

/**
 * Builds a router with all the API endpoints.
 * Everything the routes need is passed in, so tests can pass in fakes.
 */
function createRoutes({ videosDir, resultsDir, jobStore, runProcessor, makeThumbnail }) {
  const router = express.Router();

  /**
   * GET /api/videos
   * Returns the video file names in the videos folder, like ["ensantina.mp4"].
   */
  router.get("/api/videos", async (req, res) => {
    try {
      const files = await fs.readdir(videosDir);
      const videos = files.filter((file) =>
        VIDEO_EXTENSIONS.includes(path.extname(file).toLowerCase())
      );
      res.json(videos);
    } catch (err) {
      res.status(500).json({ error: "Error reading video directory" });
    }
  });

  /**
   * GET /thumbnail/:filename
   * Returns the first frame of the video as a JPEG.
   */
  router.get("/thumbnail/:filename", async (req, res) => {
    /*
     * basename keeps only the file name, so a request like
     * "../../secret" can't reach files outside the videos folder.
     */
    const videoPath = path.join(videosDir, path.basename(req.params.filename));

    try {
      await fs.access(videoPath);
      const jpeg = await makeThumbnail(videoPath);
      res.type("image/jpeg").send(jpeg);
    } catch (err) {
      res.status(500).json({ error: "Error generating thumbnail" });
    }
  });

  /**
   * POST /process/:filename?targetColor=ff0000&threshold=120
   * Starts processing a video in the background.
   * Answers right away with 202 { jobId }, without waiting for the JAR.
   */
  router.post("/process/:filename", async (req, res) => {
    const filename = path.basename(req.params.filename);
    const { targetColor, threshold } = req.query;

    // Check the inputs
    if (!targetColor || !threshold) {
      return res.status(400).json({ error: "Missing targetColor or threshold query parameter." });
    }
    if (!HEX_COLOR.test(targetColor)) {
      return res.status(400).json({ error: "targetColor must be a 6-digit hex color, like ff0000." });
    }
    if (!WHOLE_NUMBER.test(threshold)) {
      return res.status(400).json({ error: "threshold must be a whole number, 0 or bigger." });
    }
    // always plain RRGGBB by remove a leading #
    const color = targetColor.replace(/^#/, "");

    const videoPath = path.join(videosDir, filename);
    try {
      await fs.access(videoPath);
    } catch {
      return res.status(400).json({ error: `Video not found: ${filename}` });
    }

    try {
      const jobId = uuidv4();

      // Each job gets its own CSV, so the same video can be processed twice at once
      const resultName = `${jobId}.csv`;

      // Save the job as "processing" before starting the JAR
      await jobStore.save(jobId, {
        status: "processing",
        video: filename,
        targetColor: color,
        threshold: Number(threshold),
        result: `/results/${resultName}`,
      });

      /*
       * Start the JAR in the background.
       * When it finishes, mark the job "done", or "error" if it failed.
       */
      runProcessor(
        { videoPath, resultPath: path.join(resultsDir, resultName), targetColor: color, threshold },
        (error) => {
          const changes = error
            ? { status: "error", error: `Error processing video: ${error.message}` }
            : { status: "done" };

          jobStore.update(jobId, changes).catch((err) =>
            console.error(`Could not update job ${jobId}:`, err)
          );
        }
      );

      // Answer right away, without waiting for the JAR
      res.status(202).json({ jobId });
    } catch (err) {
      res.status(500).json({ error: "Error starting job" });
    }
  });
  /**
 * GET /process/:jobId/status
 * Says whether a job is still running, finished, or failed.
 *   { "status": "processing" }
 *   { "status": "done", "result": "/results/<jobId>.csv" }
 *   { "status": "error", "error": "..." }
 */
  router.get("/process/:jobId/status", async (req, res) => {
    const { jobId } = req.params;

    /*
     * A real job ID always looks like a UUID.
     * Rejecting anything else also blocks tricks like "../" in the URL.
     */
    if (!UUID.test(jobId)) {
      return res.status(404).json({ error: "Job ID not found" });
    }

    try {
      const job = await jobStore.find(jobId);

      if (!job) {
        return res.status(404).json({ error: "Job ID not found" });
      }

      // Answer in exactly the shapes the API spec shows
      if (job.status === "done") {
        return res.json({ status: "done", result: job.result });
      }
      if (job.status === "error") {
        return res.json({ status: "error", error: job.error });
      }
      return res.json({ status: "processing" });
    } catch (err) {
      res.status(500).json({ error: "Error fetching job status" });
    }
  });

  return router;
}

module.exports = createRoutes;