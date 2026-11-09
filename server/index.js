const path = require("path");
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

app.listen(PORT, () => {
  console.log(`Server listening on http://localhost:${PORT}`);
  console.log(`Videos folder:  ${VIDEOS_DIR}`);
  console.log(`Results folder: ${RESULTS_DIR}`);
});