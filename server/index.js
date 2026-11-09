const path = require("path");
const fs = require("fs/promises");
const express = require("express");

// The .env file is in the main folder, one level above server/
const ROOT_DIR = path.resolve(__dirname, "..");
require("dotenv").config({ path: path.join(ROOT_DIR, ".env") });

// Turn the paths from .env into full paths (relative to the main folder)
const PORT = process.env.PORT || 3000;
const VIDEOS_DIR = path.resolve(ROOT_DIR, process.env.VIDEOS_DIR || "videos");
const RESULTS_DIR = path.resolve(ROOT_DIR, process.env.RESULTS_DIR || "results");

const app = express();

// Make the videos and results folders public, as the API spec says:
// /videos/ensantina.mp4 and /results/ensantina.mp4.csv
app.use("/videos", express.static(VIDEOS_DIR));
app.use("/results", express.static(RESULTS_DIR));

// Simple test route to check that the server runs
app.get("/", (req, res) => {
  res.send("Salamander server is running");
});


// File types we count as videos
const VIDEO_EXTENSIONS = [".mp4", ".mov", ".avi", ".mkv"];

// GET /api/videos -> ["ensantina.mp4", ...]
app.get("/api/videos", async (req, res) => {
  try {
    const files = await fs.readdir(VIDEOS_DIR);
    const videos = files.filter((file) =>
      VIDEO_EXTENSIONS.includes(path.extname(file).toLowerCase())
    );
    res.json(videos);
  } catch (err) {
    res.status(500).json({ error: "Error reading video directory" });
  }
});

app.listen(PORT, () => {
  console.log(`Server listening on http://localhost:${PORT}`);
  console.log(`Videos folder:  ${VIDEOS_DIR}`);
  console.log(`Results folder: ${RESULTS_DIR}`);
});