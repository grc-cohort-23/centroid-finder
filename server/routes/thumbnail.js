import { Router } from 'express';
import { execFile } from 'child_process';
import { access } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';
import { isSafeFilename } from '../lib/validate.js';

const router = Router();

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