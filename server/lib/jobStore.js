import { readFile, writeFile, rename } from 'fs/promises';
import path from 'path';
import { config } from '../config.js';
import { isUuid } from './validate.js';

const jobPath = (jobId) => path.join(config.jobsDir, `${jobId}.json`);

export async function saveJob(job) {
  const tmp = `${jobPath(job.jobId)}.tmp`;
  await writeFile(tmp, JSON.stringify(job, null, 2));
  await rename(tmp, jobPath(job.jobId));
}

export async function getJob(jobId) {
  if (!isUuid(jobId)) return null;
  try {
    return JSON.parse(await readFile(jobPath(jobId), 'utf8'));
  } catch (err) {
    if (err.code === 'ENOENT') return null;
    throw err;
  }
}

export async function updateJob(jobId, changes) {
  const job = await getJob(jobId);
  if (!job) return;
  await saveJob({ ...job, ...changes });
}
