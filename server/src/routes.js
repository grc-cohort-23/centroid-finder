// The API endpoints:
//   GET  /api/videos
//   GET  /thumbnail/:filename
const express = require("express");
const fs = require("fs/promises");
const path = require("path");

const VIDEO_EXTENSIONS = [".mp4", ".mov", ".avi", ".mkv"];

function createRoutes({ videosDir, makeThumbnail }) {
  const router = express.Router();

  // ---------- GET /api/videos -> ["ensantina.mp4", ...] ----------
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

  // ---------- GET /thumbnail/:filename -> first frame as a JPEG ----------
  router.get("/thumbnail/:filename", async (req, res) => {
    // basename strips folder parts, so "../../secret" can't escape the videos folder
    const videoPath = path.join(videosDir, path.basename(req.params.filename));
    try {
      await fs.access(videoPath);
      const jpeg = await makeThumbnail(videoPath);
      res.type("image/jpeg").send(jpeg);
    } catch (err) {
      res.status(500).json({ error: "Error generating thumbnail" });
    }
  });

  return router;
}

module.exports = createRoutes;