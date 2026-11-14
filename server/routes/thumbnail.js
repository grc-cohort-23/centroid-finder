const express = require('express');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const ffmpegPath = require('ffmpeg-static');
const config = require('../config');

const router = express.Router();

// GET /thumbnail/:filename -> JPEG of the video's first frame
router.get('/thumbnail/:filename', (req, res) => {
  const { filename } = req.params;

  const fail = (reason) => {
    console.error(`Thumbnail for "${filename}" failed:`, reason);
    // Only respond if we haven't already started sending something.
    if (!res.headersSent) {
      res.status(500).json({ error: 'Error generating thumbnail' });
    }
  };

  // Reject names like "../../secret.mp4" so nobody can read files
  // outside the videos folder.
  if (path.basename(filename) !== filename) {
    return fail('filename contains a path');
  }

  const videoPath = path.join(config.VIDEO_DIR, filename);
  if (!fs.existsSync(videoPath)) {
    return fail('file not found');
  }

  // Ask ffmpeg for exactly one frame, encoded as JPEG, written to stdout.
  const ffmpeg = spawn(ffmpegPath, [
    '-loglevel', 'error',
    '-i', videoPath,
    '-frames:v', '1',
    '-f', 'image2',
    '-c:v', 'mjpeg',
    'pipe:1',
  ]);

  const chunks = [];
  let errorOutput = '';

  ffmpeg.stdout.on('data', (chunk) => chunks.push(chunk));
  ffmpeg.stderr.on('data', (chunk) => { errorOutput += chunk; });

  // 'error' fires if ffmpeg couldn't be started at all.
  ffmpeg.on('error', (err) => fail(err.message));

  // 'close' fires when ffmpeg has exited and its output is complete.
  ffmpeg.on('close', (code) => {
    const image = Buffer.concat(chunks);
    if (code !== 0 || image.length === 0) {
      return fail(errorOutput.trim() || `ffmpeg exited with code ${code}`);
    }
    res.type('image/jpeg').send(image);
  });
});

module.exports = router;
