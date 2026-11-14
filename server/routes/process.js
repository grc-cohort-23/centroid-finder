import { Router } from 'express';
import { spawn } from 'child_process';
import { access } from 'fs/promises';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';
import { config } from '../config.js';
import { isSafeFilename, isValidColor, isValidThreshold } from '../lib/validate.js';
import { saveJob, getJob, updateJob } from '../lib/jobStore.js';

const router = Router();

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

    const child = spawn(
      'java',
      ['-jar', config.jarPath, videoPath, outputPath, targetColor, threshold],
      { detached: true, stdio: 'ignore' }
    );

    child.on('error', async (err) => {
      await updateJob(jobId, { status: 'error', error: `Error processing video: ${err.message}` })
        .catch(console.error);
    });

    child.on('exit', async (code) => {
      try {
        let hasCsv = true;
        await access(outputPath).catch(() => { hasCsv = false; });
        if (code === 0 && hasCsv) {
          await updateJob(jobId, { status: 'done', result: `/results/${jobId}.csv` });
        } else {
          await updateJob(jobId, {
            status: 'error',
            error: 'Error processing video: the processor did not produce a result',
          });
        }
      } catch (err) {
        console.error('Could not update job', jobId, err);
      }
    });

    child.unref();
    res.status(202).json({ jobId });
  } catch (err) {
    res.status(500).json({ error: 'Error starting job' });
  }
});

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