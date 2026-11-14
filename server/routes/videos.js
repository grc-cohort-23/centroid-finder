const express = require('express');
const fs = require('fs/promises');
const path = require('path');
const config = require('../config');

const router = express.Router();

// Only these file types count as videos. Anything else in the folder
// (like macOS's hidden .DS_Store file) is left out of the list.
const VIDEO_EXTENSIONS = new Set(['.mp4', '.mov', '.avi', '.mkv', '.webm']);

// GET /api/videos -> ["ensantina.mp4", ...]
router.get('/api/videos', async (req, res) => {
  try {
    const entries = await fs.readdir(config.VIDEO_DIR, { withFileTypes: true });

    const videos = entries
      .filter((entry) => entry.isFile())
      .filter((entry) => VIDEO_EXTENSIONS.has(path.extname(entry.name).toLowerCase()))
      .map((entry) => entry.name);

    res.json(videos);
  } catch (err) {
    console.error('Error reading video directory:', err.message);
    res.status(500).json({ error: 'Error reading video directory' });
  }
});

module.exports = router;
