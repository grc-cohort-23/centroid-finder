import { Router } from 'express';
import { execFile } from 'child_process';
import { access } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';
import { isSafeFilename } from '../lib/validate.js';

const router = Router();

/**
 * GET /thumbnail/:filename
 *
 * Extracts the first frame of a video and streams it as a JPEG.
 * Uses ffmpeg's `pipe:1` output to avoid writing a temp file.
 *
 * @param {import('express').Request}  req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *   200 — `image/jpeg` binary
 *   400 — `{ error: "Invalid filename" }` if the name fails the safety check
 *   404 — `{ error: "Video not found" }` if the file does not exist
 *   500 — `{ error: "Error generating thumbnail" }` if ffmpeg fails or returns
 *          no bytes
 */
router.get('/thumbnail/:filename', async (req, res) => {
  const { filename } = req.params;

  if (!isSafeFilename(filename)) {
    return res.status(400).json({ error: 'Invalid filename' });
  }

  const videoPath = path.join(config.videosDir, filename);
  try {
    await access(videoPath);
  } catch {
    return res.status(404).json({ error: 'Video not found' });
  }

  const args = ['-v', 'error', '-i', videoPath, '-frames:v', '1',
                '-f', 'image2', '-c:v', 'mjpeg', 'pipe:1'];

  execFile('ffmpeg', args, { encoding: 'buffer', maxBuffer: 10 * 1024 * 1024 },
    (err, stdout) => {
      if (err || stdout.length === 0) {
        return res.status(500).json({ error: 'Error generating thumbnail' });
      }
      res.type('image/jpeg').send(stdout);
    });
});

export default router;