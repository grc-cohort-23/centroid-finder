// Reads .env and turns the paths into full paths.
// Everything else gets its settings from here, so paths live in one place.
const path = require("path");

// The .env file is in the main folder, two levels above server/src/
const ROOT_DIR = path.resolve(__dirname, "..", "..");
require("dotenv").config({ path: path.join(ROOT_DIR, ".env") });

// Paths in .env are relative to the main folder
function fromRoot(value, fallback) {
  return path.resolve(ROOT_DIR, value || fallback);
}

module.exports = {
  port: Number(process.env.PORT) || 3000,
  videosDir: fromRoot(process.env.VIDEOS_DIR, "videos"),
  resultsDir: fromRoot(process.env.RESULTS_DIR, "results"),
    jobsDir: fromRoot(process.env.JOBS_DIR, "jobs"),
  jarPath: fromRoot(process.env.JAR_PATH, "processor/target/videoprocessor.jar"),
};