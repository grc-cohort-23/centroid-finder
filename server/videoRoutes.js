const express = require("express");
const path = require("node:path");
const fs = require("node:fs/promises");
const ffmpegPath = require("ffmpeg-static");
const { execFile } = require("node:child_process");
const { promisify } = require("node:util");

const router = express.Router();
const execFileAsync = promisify(execFile);
const projectRoot = path.resolve(__dirname, "..");

const videosDirectory = path.resolve(
  projectRoot,
  process.env.VIDEOS_DIR || "processor/sampleInput"
);

const videoExtensions = new Set([
  ".mp4",
  ".mov",
  ".avi",
  ".mkv",
  ".webm",
  ".m4v",
]);

router.get("/api/videos", async (req, res) => {
  try {
    const files = await fs.readdir(videosDirectory, {
      withFileTypes: true,
    });

    const videos = files
      .filter(
        (file) =>
          file.isFile() &&
          videoExtensions.has(path.extname(file.name).toLowerCase())
      )
      .map((file) => file.name)
      .sort();

    res.json(videos);
  } catch (error) {
    console.error(error);
    res.status(500).json({
      error: "Error reading video directory",
    });
  }
});

router.get("/thumbnail/:filename", async (req, res) => {
  const filename = req.params.filename;

  if (
    filename === "." ||
    filename === ".." ||
    filename.includes("/") ||
    filename.includes("\\")
  ) {
    return res.status(400).json({ error: "Invalid filename" });
  }

  const videoPath = path.join(videosDirectory, filename);

  try {
    const stats = await fs.stat(videoPath);

    if (!stats.isFile()) {
      return res.status(404).json({ error: "Video not found" });
    }
  } catch (error) {
    if (error.code === "ENOENT" || error.code === "ENOTDIR") {
      return res.status(404).json({ error: "Video not found" });
    }

    console.error("Error checking video file:", error);
    return res.status(500).json({ error: "Error reading video file" });
  }

  try {
    const { stdout } = await execFileAsync(
      ffmpegPath,
      [
        "-loglevel",
        "error",
        "-i",
        videoPath,
        "-frames:v",
        "1",
        "-f",
        "image2pipe",
        "-vcodec",
        "mjpeg",
        "pipe:1",
      ],
      {
        encoding: "buffer",
        maxBuffer: 10 * 1024 * 1024,
      }
    );

    if (!stdout.length) {
      return res.status(500).json({
        error: "Could not generate thumbnail",
      });
    }

    res.type("jpeg").send(stdout);
  } catch (error) {
    console.error("Thumbnail generation failed:", error);
    res.status(500).json({
      error: "Could not generate thumbnail",
    });
  }
});

router.use("/videos", express.static(videosDirectory));

module.exports = router;
