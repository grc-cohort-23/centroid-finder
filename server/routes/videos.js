const express = require('express');
const fs = require('fs/promises');
const path = require('path');
const multer = require('multer');
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

// An error that carries the HTTP status to answer with.
const httpError = (status, message) => Object.assign(new Error(message), { status });

// Builds the multer middleware. It is created per request so it always uses
// the current video folder and size limit from config.
function videoUpload() {
  return multer({
    limits: { fileSize: config.MAX_UPLOAD_BYTES, files: 1 },

    storage: multer.diskStorage({
      destination: (req, file, cb) => cb(null, config.VIDEO_DIR),
      filename: async (req, file, cb) => {
        // multer already strips folders from the name; basename is a second guard.
        const name = path.basename(file.originalname);
        try {
          await fs.access(path.join(config.VIDEO_DIR, name));
          cb(httpError(409, `A video named "${name}" already exists.`));
        } catch {
          cb(null, name);
        }
      },
    }),

    fileFilter: (req, file, cb) => {
      const ext = path.extname(file.originalname).toLowerCase();
      if (!VIDEO_EXTENSIONS.has(ext)) {
        return cb(httpError(400, `Unsupported file type. Use ${[...VIDEO_EXTENSIONS].join(', ')}.`));
      }
      cb(null, true);
    },
  }).single('video');
}

// POST /api/videos (multipart/form-data, field "video") -> 201 { filename }
router.post('/api/videos', (req, res) => {
  videoUpload()(req, res, (err) => {
    if (err) {
      if (err.code === 'LIMIT_FILE_SIZE') {
        const mb = Math.round(config.MAX_UPLOAD_BYTES / (1024 * 1024));
        return res.status(413).json({ error: `File is too large (limit ${mb} MB).` });
      }
      if (err.status) {
        return res.status(err.status).json({ error: err.message });
      }
      if (err instanceof multer.MulterError) {
        return res.status(400).json({ error: err.message });
      }
      console.error('Error saving upload:', err.message);
      return res.status(500).json({ error: 'Error saving upload' });
    }

    if (!req.file) {
      return res.status(400).json({ error: 'No video uploaded. Send it in a "video" form field.' });
    }

    res.status(201).json({ filename: req.file.filename });
  });
});

module.exports = router;
