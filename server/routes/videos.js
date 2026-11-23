import { Router } from 'express';
import { readdir } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';

const router = Router();
const VIDEO_EXTENSIONS = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];

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