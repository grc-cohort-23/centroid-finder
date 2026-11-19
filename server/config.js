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
