import { Router } from 'express';
import { readdir } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';

const router = Router();
const VIDEO_EXTENSIONS = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];

/**
 * GET /api/videos
 *
 * Returns the list of video files available for processing.
 *
 * @param {import('express').Request}  req
 * @param {import('express').Response} res
 * @returns {Promise<void>}
 *   200 — JSON array of filenames, e.g. `["salamander1.mp4"]`
 *   500 — `{ error: "Error reading video directory" }` if the directory is
 *          missing or unreadable
 */
router.get('/api/videos', async (req, res) => {
  try {
    const files = await readdir(config.videosDir);
    const videos = files.filter((f) =>
      VIDEO_EXTENSIONS.includes(path.extname(f).toLowerCase())
    );
    res.json(videos);
  } catch (err) {
    res.status(500).json({ error: 'Error reading video directory' });
  }
});

export default router;