import { Router } from 'express';
import { access } from 'fs/promises';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';
import { config } from '../config.js';
import { isSafeFilename, isValidColor, isValidThreshold } from '../lib/validate.js';
import { saveJob, getJob, updateJob } from '../lib/jobStore.js';
import { runJob } from '../lib/jobRunner.js';

const router = Router();

/**
 * POST /process/:filename?targetColor=RRGGBB&threshold=N
 *
 * Validates the request, creates a job record with status `"processing"`,
 * starts the Java processor (`java -jar videoprocessor.jar`) as a detached
 * background process, and returns 202 immediately so the client can poll
 * `GET /process/:jobId/status` for the result. A job only becomes `"done"` when
 * the JAR exits with code 0 AND its CSV file exists; anything else is an
 * `"error"` carrying the processor's stderr text.
 *
 * @param {import('express').Request}  req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *   202 — `{ jobId: "<uuid>" }`
 *   400 — `{ error: "..." }` for missing or invalid query params / filename
 *   404 — `{ error: "Video not found" }` if the file does not exist
 *   500 — `{ error: "Error starting job" }` on unexpected failure
 */
router.post('/process/:filename', async (req, res) => {
  const { filename } = req.params;
  const { targetColor, threshold } = req.query;

  if (!targetColor || !threshold) {
    return res.status(400).json({ error: 'Missing targetColor or threshold query parameter.' });
  }
  if (!isSafeFilename(filename) || !isValidColor(targetColor) || !isValidThreshold(threshold)) {
    return res.status(400).json({
      error: 'Invalid filename, targetColor (RRGGBB hex) or threshold (whole number).',
    });
  }

  const videoPath = path.join(config.videosDir, filename);
  try {
    await access(videoPath);
  } catch {
    return res.status(404).json({ error: 'Video not found' });
  }

  try {
    const jobId = uuidv4();
    const outputPath = path.join(config.resultsDir, `${jobId}.csv`);

    await saveJob({
      jobId,
      status: 'processing',
      video: filename,
      targetColor,
      threshold,
      createdAt: new Date().toISOString(),
    });

    // Hand off spawn + exit handling to jobRunner so this route stays focused
    // on validation and responding. HTTP behavior is identical: 202 fires before
    // the child process exits.
    runJob({ jobId, jarPath: config.jarPath, videoPath, outputPath, targetColor, threshold });

    res.status(202).json({ jobId });
  } catch (err) {
    res.status(500).json({ error: 'Error starting job' });
  }
});

/**
 * GET /process/:jobId/status
 *
 * Returns the current state of a job created by POST /process.
 *
 * @param {import('express').Request}  req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *   200 — `{ status: "processing" }` while the JAR is running
 *         `{ status: "done", result: "/results/<id>.csv" }` on success
 *         `{ status: "error", error: "<message>" }` on failure
 *   404 — `{ error: "Job ID not found" }` if the jobId is unknown
 *   500 — `{ error: "Error fetching job status" }` on unexpected failure
 */
router.get('/process/:jobId/status', async (req, res) => {
  try {
    const job = await getJob(req.params.jobId);
    if (!job) return res.status(404).json({ error: 'Job ID not found' });

    if (job.status === 'done') return res.json({ status: 'done', result: job.result });
    if (job.status === 'error') return res.json({ status: 'error', error: job.error });
    res.json({ status: 'processing' });
  } catch (err) {
    res.status(500).json({ error: 'Error fetching job status' });
  }
});

export default router;
