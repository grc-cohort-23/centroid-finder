import { test, after } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, writeFile, rm } from 'fs/promises';
import { tmpdir } from 'os';
import { join } from 'path';
import { EventEmitter } from 'events';

// config.js reads JOBS_DIR once, when it is first imported, and jobRunner.js pulls it in
// through jobStore.js. So the temp directory has to exist and JOBS_DIR has to be set BEFORE
// those modules are imported. (A static `import { runJob } ...` at the top of this file would
// load config.js first and the job files would land in the real server/jobs folder, which
// doesn't exist on a fresh clone.) This is the same pattern routes.test.js uses.
// `node --test` runs every test file in its own process, so the env var can't leak elsewhere.
const jobsDir = await mkdtemp(join(tmpdir(), 'cf-jobrunner-test-jobs-'));
const outputDir = await mkdtemp(join(tmpdir(), 'cf-jobrunner-test-results-'));
process.env.JOBS_DIR = jobsDir;

const { runJob } = await import('../lib/jobRunner.js');
const { saveJob, getJob } = await import('../lib/jobStore.js');

after(async () => {
  await rm(jobsDir, { recursive: true, force: true });
  await rm(outputDir, { recursive: true, force: true });
});

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Returns a fake ChildProcess-like EventEmitter that runJob can drive, so the tests
 * never need Java on the PATH. It exposes helpers to trigger stderr output, 'exit'
 * and 'error' at will.
 */
function makeFakeChild() {
  const child = new EventEmitter();

  // Mimic the stderr stream: an EventEmitter with a 'data' event.
  child.stderr = new EventEmitter();
  child.unref = () => {};

  child.emitStderr = (text) => child.stderr.emit('data', Buffer.from(text));
  child.emitExit = (code) => child.emit('exit', code);
  child.emitError = (err) => child.emit('error', err);

  return child;
}

/**
 * Creates a "processing" job record, starts runJob with a fake child, and returns
 * everything a test needs to drive and inspect the run.
 *
 * @param {string} jobId - a UUID (getJob rejects anything that isn't one)
 * @returns {Promise<{ child: object, outputPath: string, spawnCalls: Array }>}
 */
async function startFakeJob(jobId) {
  const outputPath = join(outputDir, `${jobId}.csv`);
  await saveJob({ jobId, status: 'processing' });

  const child = makeFakeChild();
  const spawnCalls = [];
  const spawnFn = (...args) => {
    spawnCalls.push(args);
    return child;
  };

  runJob({
    jobId,
    jarPath: '/fake/videoprocessor.jar',
    videoPath: '/fake/video.mp4',
    outputPath,
    targetColor: 'ff0000',
    threshold: '50',
    spawnFn,
  });

  return { child, outputPath, spawnCalls };
}

/** Gives runJob's async exit/error handler time to finish writing the job file. */
const settle = () => new Promise((resolve) => setTimeout(resolve, 20));

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test('spawns java -jar with the JAR argument order the processor expects', async () => {
  const { spawnCalls } = await startFakeJob('aaaaaaaa-0000-4000-8000-000000000000');

  assert.equal(spawnCalls.length, 1);
  const [command, args] = spawnCalls[0];
  assert.equal(command, 'java');
  assert.deepEqual(args.slice(0, 1), ['-jar']);
  assert.equal(args[1], '/fake/videoprocessor.jar');
  assert.equal(args[2], '/fake/video.mp4');
  assert.ok(args[3].endsWith('aaaaaaaa-0000-4000-8000-000000000000.csv'));
  assert.deepEqual(args.slice(4), ['ff0000', '50']);
});

test('exit 0 with a CSV file sets the job to done with the result path', async () => {
  const jobId = 'aaaaaaaa-0000-4000-8000-000000000001';
  const { child, outputPath } = await startFakeJob(jobId);

  // The CSV must exist before 'exit' fires, because the exit handler checks for it.
  await writeFile(outputPath, 'timestamp,x,y\n0.00,10,20\n');
  child.emitExit(0);
  await settle();

  const job = await getJob(jobId);
  assert.equal(job.status, 'done');
  assert.equal(job.result, `/results/${jobId}.csv`);
});

test('exit 0 without a CSV file sets the job to error', async () => {
  const jobId = 'aaaaaaaa-0000-4000-8000-000000000002';
  const { child } = await startFakeJob(jobId); // the CSV is never created

  child.emitExit(0);
  await settle();

  const job = await getJob(jobId);
  assert.equal(job.status, 'error');
  assert.ok(job.error.includes('Error processing video'));
  assert.ok(job.error.includes('the processor did not produce a result'));
});

test('non-zero exit sets the job to error and includes the stderr text', async () => {
  const jobId = 'aaaaaaaa-0000-4000-8000-000000000003';
  const { child } = await startFakeJob(jobId);

  child.emitStderr('Invalid color argument');
  child.emitExit(1);
  await settle();

  const job = await getJob(jobId);
  assert.equal(job.status, 'error');
  assert.ok(job.error.includes('Invalid color argument'));
});

test('non-zero exit sets the job to error even when a CSV exists', async () => {
  const jobId = 'aaaaaaaa-0000-4000-8000-000000000005';
  const { child, outputPath } = await startFakeJob(jobId);

  // A job is done only when the exit code is 0 AND the CSV exists, so a leftover or
  // partial CSV must not turn a failed run into a "done" job.
  await writeFile(outputPath, 'timestamp,x,y\n');
  child.emitExit(2);
  await settle();

  const job = await getJob(jobId);
  assert.equal(job.status, 'error');
});

test('spawn error event sets the job to error', async () => {
  const jobId = 'aaaaaaaa-0000-4000-8000-000000000004';
  const { child } = await startFakeJob(jobId);

  child.emitError(new Error('spawn ENOENT'));
  await settle();

  const job = await getJob(jobId);
  assert.equal(job.status, 'error');
  assert.ok(job.error.includes('spawn ENOENT'));
});

test('a synchronous spawn failure throws to the caller instead of becoming a rejected promise', () => {
  // routes/process.js calls runJob inside a try/catch and answers 500 "Error starting job"
  // when it throws. That only works if runJob throws synchronously, which is why it must
  // not be an async function.
  assert.throws(
    () =>
      runJob({
        jobId: 'aaaaaaaa-0000-4000-8000-000000000006',
        jarPath: '/fake/videoprocessor.jar',
        videoPath: '/fake/video.mp4',
        outputPath: join(outputDir, 'never-written.csv'),
        targetColor: 'ff0000',
        threshold: '50',
        spawnFn: () => {
          throw new TypeError('bad spawn arguments');
        },
      }),
    /bad spawn arguments/
  );
});
