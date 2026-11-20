const fs = require('fs');
const path = require('path');
const request = require('supertest');

// Fake the runner so no Java process is started.
jest.mock('../runner', () => ({ runJob: jest.fn() }));
const { runJob } = require('../runner');

const app = require('../app');
const config = require('../config');
const jobs = require('../jobs');

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

beforeAll(() => {
  fs.writeFileSync(path.join(config.VIDEO_DIR, 'ensantina.mp4'), '');
});

beforeEach(() => {
  runJob.mockClear();
});

describe('POST /process/:filename', () => {
  const post = (query) => request(app).post(`/process/ensantina.mp4${query}`);

  test('returns 202 with a jobId', async () => {
    const res = await post('?targetColor=ff0000&threshold=120');

    expect(res.status).toBe(202);
    expect(res.body).toEqual({ jobId: expect.stringMatching(UUID) });
  });

  test('saves the job as processing', async () => {
    const res = await post('?targetColor=FF00aa&threshold=120');

    expect(jobs.getJob(res.body.jobId)).toMatchObject({
      filename: 'ensantina.mp4',
      targetColor: 'FF00aa',
      threshold: 120,
      status: 'processing',
    });
  });

  test('starts the processor with the job details', async () => {
    const res = await post('?targetColor=ff0000&threshold=120');

    expect(runJob).toHaveBeenCalledTimes(1);
    expect(runJob).toHaveBeenCalledWith({
      id: res.body.jobId,
      filename: 'ensantina.mp4',
      targetColor: 'ff0000',
      threshold: 120,
    });
  });

  test('gives every job a different ID', async () => {
    const first = await post('?targetColor=ff0000&threshold=120');
    const second = await post('?targetColor=ff0000&threshold=120');

    expect(first.body.jobId).not.toBe(second.body.jobId);
  });

  test.each([
    ['no query at all', ''],
    ['no threshold', '?targetColor=ff0000'],
    ['no targetColor', '?threshold=120'],
  ])('returns 400 when there is %s', async (_, query) => {
    const res = await post(query);

    expect(res.status).toBe(400);
    expect(res.body).toEqual({ error: 'Missing targetColor or threshold query parameter.' });
    expect(runJob).not.toHaveBeenCalled();
  });

  test.each([
    ['a color name', '?targetColor=red&threshold=120'],
    ['a short hex color', '?targetColor=f00&threshold=120'],
    ['a hex color with #', '?targetColor=%23ff0000&threshold=120'],
    ['a non-hex color', '?targetColor=gg0000&threshold=120'],
    ['a word threshold', '?targetColor=ff0000&threshold=abc'],
    ['a negative threshold', '?targetColor=ff0000&threshold=-5'],
    ['a decimal threshold', '?targetColor=ff0000&threshold=1.5'],
  ])('returns 400 for %s', async (_, query) => {
    const res = await post(query);

    expect(res.status).toBe(400);
    expect(res.body.error).toMatch(/targetColor must be 6 hex digits/);
    expect(runJob).not.toHaveBeenCalled();
  });

  test('returns 500 when the video does not exist', async () => {
    const res = await request(app).post('/process/missing.mp4?targetColor=ff0000&threshold=120');

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: 'Error starting job' });
    expect(runJob).not.toHaveBeenCalled();
  });

  test('returns 500 for a filename that tries to leave the videos folder', async () => {
    const res = await request(app)
      .post('/process/..%2Fsecret.mp4?targetColor=ff0000&threshold=120');

    expect(res.status).toBe(500);
    expect(runJob).not.toHaveBeenCalled();
  });
});

describe('GET /process/:jobId/status', () => {
  const newJob = (id) => jobs.createJob({
    id, filename: 'ensantina.mp4', targetColor: 'ff0000', threshold: 120,
  });

  test('reports processing while the job is running', async () => {
    newJob('status-processing');

    const res = await request(app).get('/process/status-processing/status');

    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: 'processing' });
  });

  test('reports done with the result link', async () => {
    newJob('status-done');
    jobs.updateJob('status-done', { status: 'done', result: '/results/ensantina.mp4.csv' });

    const res = await request(app).get('/process/status-done/status');

    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: 'done', result: '/results/ensantina.mp4.csv' });
  });

  test('reports error with the error message', async () => {
    newJob('status-error');
    jobs.updateJob('status-error', { status: 'error', error: 'Error processing video: boom' });

    const res = await request(app).get('/process/status-error/status');

    expect(res.status).toBe(200);
    expect(res.body).toEqual({ status: 'error', error: 'Error processing video: boom' });
  });

  test('returns 404 for an unknown job ID', async () => {
    const res = await request(app).get('/process/not-a-real-id/status');

    expect(res.status).toBe(404);
    expect(res.body).toEqual({ error: 'Job ID not found' });
  });

  test('a job started through POST can be looked up', async () => {
    const started = await request(app)
      .post('/process/ensantina.mp4?targetColor=ff0000&threshold=120');

    const res = await request(app).get(`/process/${started.body.jobId}/status`);

    expect(res.body).toEqual({ status: 'processing' });
  });
});
