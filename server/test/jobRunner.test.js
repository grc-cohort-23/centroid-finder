import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, writeFile, rm } from 'fs/promises';
import { tmpdir } from 'os';
import { join } from 'path';
import { EventEmitter } from 'events';

import { runJob } from '../lib/jobRunner.js';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Creates an isolated temp directory for each test so job files don't bleed
 * between runs. Returns the dir path and a cleanup function.
 */
async function makeTempDir() {
  const dir = await mkdtemp(join(tmpdir(), 'cf-jobrunner-test-'));
  return { dir, cleanup: () => rm(dir, { recursive: true, force: true }) };
}

/**
 * Returns a fake ChildProcess-like EventEmitter that jobRunner can drive.
 * The returned object also exposes `emitExit` and `emitError` helpers so
 * tests can trigger the events at will, without needing Java on the PATH.
 *
 * @param {object} [opts]
 * @param {string} [opts.stderrText] - text that the fake process streams on stderr
 */
function makeFakeChild(opts = {}) {
  const child = new EventEmitter();

  // Mimic the stderr stream: an EventEmitter with a 'data' event.
  child.stderr = new EventEmitter();
  child.unref = () => {};

  // Convenience: stream stderr data before emitting 'exit' or 'error'.
  child.emitStderr = (text) => child.stderr.emit('data', Buffer.from(text));
  child.emitExit = (code) => child.emit('exit', code);
  child.emitError = (err) => child.emit('error', err);

  return child;
}

/**
 * Builds the minimal opts object runJob expects, using a real temp jobsDir
 * so updateJob can write JSON files.
 *
 * @param {string} jobId
 * @param {string} jobsDir   - temp directory that holds job JSON files
 * @param {string} outputPath - path where the "CSV" would appear
 * @param {object} child      - fake ChildProcess returned by makeFakeChild
 */
function makeOpts(jobId, jobsDir, outputPath, child) {
  // spawnFn ignores its arguments and returns the pre-built fake child.
  const spawnFn = () => child;

  return {
    jobId,
    jarPath: '/fake/videoprocessor.jar',
    videoPath: '/fake/video.mp4',
    outputPath,
    targetColor: 'ff0000',
    threshold: '50',
    spawnFn,
  };
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test('exit 0 with a CSV file sets the job to done with the result path', async () => {
  const { dir, cleanup } = await makeTempDir();
  try {
    const jobId = 'aaaaaaaa-0000-0000-0000-000000000001';
    const outputPath = join(dir, `${jobId}.csv`);

    // Pre-create the job record so updateJob has something to merge into.
    process.env.JOBS_DIR = dir;
    const { saveJob } = await import('../lib/jobStore.js');
    await saveJob({ jobId, status: 'processing' });

    const child = makeFakeChild();
    runJob(makeOpts(jobId, dir, outputPath, child));

    // Write the CSV so the existence check passes, then fire the exit event.
    await writeFile(outputPath, 'timestamp,x,y\n0.00,10,20\n');
    child.emitExit(0);

    // Give the async exit handler a tick to finish.
    await new Promise((r) => setTimeout(r, 20));

    const { getJob } = await import('../lib/jobStore.js');
    const job = await getJob(jobId);
    assert.equal(job.status, 'done');
    assert.equal(job.result, `/results/${jobId}.csv`);
  } finally {
    await cleanup();
  }
});

test('exit 0 without a CSV file sets the job to error', async () => {
  const { dir, cleanup } = await makeTempDir();
  try {
    const jobId = 'aaaaaaaa-0000-0000-0000-000000000002';
    const outputPath = join(dir, `${jobId}.csv`); // file never created

    process.env.JOBS_DIR = dir;
    const { saveJob } = await import('../lib/jobStore.js');
    await saveJob({ jobId, status: 'processing' });

    const child = makeFakeChild();
    runJob(makeOpts(jobId, dir, outputPath, child));
    child.emitExit(0); // exit 0 but no CSV

    await new Promise((r) => setTimeout(r, 20));

    const { getJob } = await import('../lib/jobStore.js');
    const job = await getJob(jobId);
    assert.equal(job.status, 'error');
    assert.ok(job.error.includes('Error processing video'));
  } finally {
    await cleanup();
  }
});

test('non-zero exit sets the job to error and includes stderr text', async () => {
  const { dir, cleanup } = await makeTempDir();
  try {
    const jobId = 'aaaaaaaa-0000-0000-0000-000000000003';
    const outputPath = join(dir, `${jobId}.csv`);

    process.env.JOBS_DIR = dir;
    const { saveJob } = await import('../lib/jobStore.js');
    await saveJob({ jobId, status: 'processing' });

    const child = makeFakeChild();
    runJob(makeOpts(jobId, dir, outputPath, child));
    child.emitStderr('Invalid color argument');
    child.emitExit(1);

    await new Promise((r) => setTimeout(r, 20));

    const { getJob } = await import('../lib/jobStore.js');
    const job = await getJob(jobId);
    assert.equal(job.status, 'error');
    assert.ok(job.error.includes('Invalid color argument'));
  } finally {
    await cleanup();
  }
});

test('spawn error event sets the job to error', async () => {
  const { dir, cleanup } = await makeTempDir();
  try {
    const jobId = 'aaaaaaaa-0000-0000-0000-000000000004';
    const outputPath = join(dir, `${jobId}.csv`);

    process.env.JOBS_DIR = dir;
    const { saveJob } = await import('../lib/jobStore.js');
    await saveJob({ jobId, status: 'processing' });

    const child = makeFakeChild();
    runJob(makeOpts(jobId, dir, outputPath, child));
    child.emitError(new Error('spawn ENOENT'));

    await new Promise((r) => setTimeout(r, 20));

    const { getJob } = await import('../lib/jobStore.js');
    const job = await getJob(jobId);
    assert.equal(job.status, 'error');
    assert.ok(job.error.includes('spawn ENOENT'));
  } finally {
    await cleanup();
  }
});
