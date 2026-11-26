import { readFile, writeFile, rename } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';
import { isUuid } from './validate.js';

const jobPath = (jobId) => path.join(config.jobsDir, `${jobId}.json`);

/**
 * Persists a job record to disk atomically.
 *
 * Writes to a `.tmp` file first, then renames it to the real path. This
 * write-then-rename pattern ensures a concurrent reader never sees a
 * partially written file — the rename is atomic on POSIX systems.
 *
 * @param {{ jobId: string, status: string, [key: string]: any }} job
 * @returns {Promise<void>}
 */
export async function saveJob(job) {
  const tmp = `${jobPath(job.jobId)}.tmp`;
  await writeFile(tmp, JSON.stringify(job, null, 2));
  await rename(tmp, jobPath(job.jobId));
}

/**
 * Reads a job record from disk.
 *
 * Returns `null` (instead of throwing) for the two expected missing-job
 * cases: an invalid jobId format and a file that doesn't exist yet.
 *
 * @param {string} jobId - UUID of the job
 * @returns {Promise<object|null>} The parsed job object, or null if not found
 * @throws If the file exists but cannot be read or parsed
 */
export async function getJob(jobId) {
  if (!isUuid(jobId)) return null;
  try {
    return JSON.parse(await readFile(jobPath(jobId), 'utf8'));
  } catch (err) {
    if (err.code === 'ENOENT') return null;
    throw err;
  }
}

/**
 * Merges `changes` into an existing job record and saves it.
 * No-ops silently if the job does not exist.
 *
 * @param {string} jobId    - UUID of the job to update
 * @param {object} changes  - Fields to merge (e.g. `{ status: "done", result: "…" }`)
 * @returns {Promise<void>}
 */
export async function updateJob(jobId, changes) {
  const job = await getJob(jobId);
  if (!job) return;
  await saveJob({ ...job, ...changes });
}
