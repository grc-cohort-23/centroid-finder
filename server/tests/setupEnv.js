// Runs before every test file (see "jest.setupFiles" in package.json).
// Points the server at a throwaway database and temp folders so tests never
// touch the real jobs.db, videos/ or results/. dotenv does not override
// variables that are already set, so these win over the project's .env.
const fs = require('fs');
const os = require('os');
const path = require('path');

const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'centroid-test-'));
fs.mkdirSync(path.join(tmp, 'videos'));
fs.mkdirSync(path.join(tmp, 'results'));

process.env.DB_PATH = ':memory:';
process.env.VIDEO_DIR = path.join(tmp, 'videos');
process.env.RESULTS_DIR = path.join(tmp, 'results');
process.env.JAR_PATH = path.join(tmp, 'videoprocessor.jar');

// The routes log failures on purpose; keep the test output readable.
jest.spyOn(console, 'error').mockImplementation(() => {});
jest.spyOn(console, 'log').mockImplementation(() => {});
