const fs = require('fs');
const path = require('path');
const request = require('supertest');

// Fake ffmpeg so the tests don't need a real video or encoder.
jest.mock('child_process', () => ({ spawn: jest.fn() }));
const { spawn } = require('child_process');
const { fakeProcess } = require('./fakeProcess');

const app = require('../app');
const config = require('../config');

const FAKE_JPEG = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10]);

// Makes the next spawn() behave like ffmpeg: sends `output`, then exits with `code`.
function ffmpegWill({ output = Buffer.alloc(0), stderr = '', code = 0 } = {}) {
  spawn.mockImplementation(() => {
    const proc = fakeProcess();
    setImmediate(() => {
      if (output.length) proc.stdout.emit('data', output);
      if (stderr) proc.stderr.emit('data', Buffer.from(stderr));
      proc.emit('close', code);
    });
    return proc;
  });
}

describe('GET /thumbnail/:filename', () => {
  beforeAll(() => {
    fs.writeFileSync(path.join(config.VIDEO_DIR, 'ensantina.mp4'), '');
  });

  beforeEach(() => {
    spawn.mockReset();
  });

  test('returns 200 with the first frame as a JPEG', async () => {
    ffmpegWill({ output: FAKE_JPEG });

    const res = await request(app).get('/thumbnail/ensantina.mp4');

    expect(res.status).toBe(200);
    expect(res.headers['content-type']).toMatch(/image\/jpeg/);
    expect(Buffer.compare(res.body, FAKE_JPEG)).toBe(0);
  });

  test('asks ffmpeg for one frame of the right video', async () => {
    ffmpegWill({ output: FAKE_JPEG });

    await request(app).get('/thumbnail/ensantina.mp4');

    const args = spawn.mock.calls[0][1];
    expect(args).toContain(path.join(config.VIDEO_DIR, 'ensantina.mp4'));
    expect(args.join(' ')).toContain('-frames:v 1');
  });

  test('returns 500 when the video does not exist', async () => {
    const res = await request(app).get('/thumbnail/missing.mp4');

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: 'Error generating thumbnail' });
    expect(spawn).not.toHaveBeenCalled();
  });

  test('returns 500 for a filename that tries to leave the videos folder', async () => {
    const res = await request(app).get('/thumbnail/..%2F..%2Fsecret.mp4');

    expect(res.status).toBe(500);
    expect(spawn).not.toHaveBeenCalled();
  });

  test('returns 500 when ffmpeg exits with an error', async () => {
    ffmpegWill({ stderr: 'Invalid data found when processing input', code: 1 });

    const res = await request(app).get('/thumbnail/ensantina.mp4');

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: 'Error generating thumbnail' });
  });

  test('returns 500 when ffmpeg exits cleanly but produces no image', async () => {
    ffmpegWill({ code: 0 });

    const res = await request(app).get('/thumbnail/ensantina.mp4');

    expect(res.status).toBe(500);
  });

  test('returns 500 when ffmpeg cannot be started', async () => {
    spawn.mockImplementation(() => {
      const proc = fakeProcess();
      setImmediate(() => proc.emit('error', new Error('spawn ENOENT')));
      return proc;
    });

    const res = await request(app).get('/thumbnail/ensantina.mp4');

    expect(res.status).toBe(500);
  });
});
