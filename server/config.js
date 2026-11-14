const path = require('path');

// The .env file lives at the project root, one level above server/.
const ROOT = path.join(__dirname, '..');
require('dotenv').config({ path: path.join(ROOT, '.env'), quiet: true });

// Paths in .env are relative to the project root; resolve them to absolute paths.
const fromRoot = (p) => path.resolve(ROOT, p);

module.exports = {
  VIDEO_DIR: fromRoot(process.env.VIDEO_DIR || 'videos'),
  RESULTS_DIR: fromRoot(process.env.RESULTS_DIR || 'results'),
  JAR_PATH: fromRoot(process.env.JAR_PATH || 'processor/target/videoprocessor.jar'),
  // ':memory:' is a special SQLite name for a throwaway in-memory database (used by tests).
  DB_PATH: process.env.DB_PATH === ':memory:' ? ':memory:' : fromRoot(process.env.DB_PATH || 'jobs.db'),
  PORT: Number(process.env.PORT) || 3000,
};
