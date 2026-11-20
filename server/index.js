const express = require("express");
const path = require("node:path");
const fs = require("node:fs/promises");

const ffmpegPath = require("ffmpeg-static");
const { execFile } = require("node:child_process");
const { promisify } = require("node:util");

const execFileAsync = promisify(execFile);

const projectRoot = path.resolve(__dirname, "..");

require("dotenv").config({
  path: path.join(projectRoot, ".env"),
});

const app = express();
app.use(require("./processingRoutes"));
const port = process.env.PORT || 3000;

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

app.get("/api/videos", async (req, res) => {
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

app.get("/thumbnail/:filename", async (req, res) => {
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
  } catch {
    return res.status(404).json({ error: "Video not found" });
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

app.use("/videos", express.static(videosDirectory));

if (require.main === module) {
  app.listen(port, () => {
    console.log(`Server listening on http://localhost:${port}`);
  });
}

module.exports = app;
