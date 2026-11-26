/**
 * Centralised configuration.
 * Reads .env from the repo root and resolves every path relative to it so
 * callers never need to know where the repo root is.
 *
 * @property {number|string} port      - HTTP port (default 3000)
 * @property {string}        videosDir - Absolute path to the video input directory
 * @property {string}        resultsDir - Absolute path to the CSV output directory
 * @property {string}        jobsDir   - Absolute path to the per-job JSON state directory
 * @property {string}        jarPath   - Absolute path to the processor JAR
 */
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');

dotenv.config({ path: path.join(root, '.env') });

const fromRoot = (p) => path.resolve(root, p);

export const config = {
  port: process.env.PORT || 3000,
  videosDir: fromRoot(process.env.VIDEOS_DIR || 'videos'),
  resultsDir: fromRoot(process.env.RESULTS_DIR || 'results'),
  jobsDir: fromRoot(process.env.JOBS_DIR || 'jobs'),
  jarPath: fromRoot(process.env.JAR_PATH || 'processor/target/videoprocessor.jar'),
};
