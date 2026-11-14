import { test, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, writeFile, rm, mkdir } from 'fs/promises';
import { tmpdir } from 'os';
import { join } from 'path';

// Set up isolated temp dirs BEFORE importing app so config picks them up.
// dotenv won't override env vars that are already set, so this takes priority over .env.
const videosDir = await mkdtemp(join(tmpdir(), 'cf-test-videos-'));
const resultsDir = await mkdtemp(join(tmpdir(), 'cf-test-results-'));
const jobsDir = await mkdtemp(join(tmpdir(), 'cf-test-jobs-'));

process.env.VIDEOS_DIR = videosDir;
process.env.RESULTS_DIR = resultsDir;
process.env.JOBS_DIR = jobsDir;
process.env.JAR_PATH = '/nonexistent/videoprocessor.jar';

// Touch a dummy video file so the videos endpoint and existence checks work.
await writeFile(join(videosDir, 'test.mp4'), '');

// Dynamic import so config.js reads our env vars above.
const { default: app } = await import('../app.js');
const { default: supertest } = await import('supertest');
const request = supertest(app);

after(async () => {
  await rm(videosDir, { recursive: true, force: true });
  await rm(resultsDir, { recursive: true, force: true });
  await rm(jobsDir, { recursive: true, force: true });
});

// --- GET /api/videos ---

test('GET /api/videos returns 200 with an array', async () => {
  const res = await request.get('/api/videos');
  assert.equal(res.status, 200);
  assert.ok(Array.isArray(res.body));
});

test('GET /api/videos includes test.mp4', async () => {
  const res = await request.get('/api/videos');
  assert.ok(res.body.includes('test.mp4'));
});

// --- GET /thumbnail/:filename ---

test('GET /thumbnail with path traversal is blocked', async () => {
  // Express normalizes '../' out of the URL before the handler sees it,
  // so the route never matches — 404 is the correct secure outcome here.
  const res = await request.get('/thumbnail/../secret.txt');
  assert.notEqual(res.status, 200);
});

test('GET /thumbnail with nonexistent file returns 404', async () => {
  const res = await request.get('/thumbnail/doesnotexist.mp4');
  assert.equal(res.status, 404);
  assert.equal(res.body.error, 'Video not found');
});

// --- POST /process/:filename ---

test('POST /process with no query params returns 400', async () => {
  const res = await request.post('/process/test.mp4');
  assert.equal(res.status, 400);
  assert.ok(res.body.error);
});

test('POST /process missing threshold returns 400', async () => {
  const res = await request.post('/process/test.mp4?targetColor=ff0000');
  assert.equal(res.status, 400);
  assert.ok(res.body.error);
});

test('POST /process missing targetColor returns 400', async () => {
  const res = await request.post('/process/test.mp4?threshold=50');
  assert.equal(res.status, 400);
  assert.ok(res.body.error);
});

test('POST /process with bad hex color returns 400', async () => {
  const res = await request.post('/process/test.mp4?targetColor=gg1234&threshold=50');
  assert.equal(res.status, 400);
  assert.ok(res.body.error);
});

test('POST /process with path traversal filename is blocked', async () => {
  // Express normalizes '../' away before the handler runs — route doesn't match, still blocked.
  const res = await request.post('/process/../secret.mp4?targetColor=ff0000&threshold=50');
  assert.notEqual(res.status, 200);
});

test('POST /process with nonexistent video returns 404', async () => {
  const res = await request.post('/process/ghost.mp4?targetColor=ff0000&threshold=50');
  assert.equal(res.status, 404);
  assert.ok(res.body.error);
});

test('POST /process with valid params returns 202 with jobId', async () => {
  const res = await request.post('/process/test.mp4?targetColor=ff0000&threshold=50');
  assert.equal(res.status, 202);
  assert.ok(typeof res.body.jobId === 'string');
});

// --- GET /process/:jobId/status ---

test('GET /process status with non-UUID returns 404', async () => {
  const res = await request.get('/process/not-a-real-id/status');
  assert.equal(res.status, 404);
  assert.ok(res.body.error);
});

test('GET /process status with valid UUID but no matching job returns 404', async () => {
  const res = await request.get('/process/00000000-0000-0000-0000-000000000000/status');
  assert.equal(res.status, 404);
  assert.equal(res.body.error, 'Job ID not found');
});

test('GET /process status returns processing or error after POST', async () => {
  const postRes = await request.post('/process/test.mp4?targetColor=570D0E&threshold=35');
  assert.equal(postRes.status, 202);
  const { jobId } = postRes.body;

  // Give the spawn error handler a tick to fire, then check status.
  await new Promise((r) => setTimeout(r, 50));
  const statusRes = await request.get(`/process/${jobId}/status`);
  assert.equal(statusRes.status, 200);
  assert.ok(['processing', 'error'].includes(statusRes.body.status));
});
