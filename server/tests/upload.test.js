const fs = require('fs');
const path = require('path');
const request = require('supertest');
const app = require('../app');
const config = require('../config');

describe('POST /api/videos', () => {
  const realVideoDir = config.VIDEO_DIR;
  const realLimit = config.MAX_UPLOAD_BYTES;

  afterEach(() => {
    config.VIDEO_DIR = realVideoDir;
    config.MAX_UPLOAD_BYTES = realLimit;
  });

  test('saves the file into the videos folder and returns 201 with its name', async () => {
    const res = await request(app).post('/api/videos').attach('video', Buffer.from('fake video'), 'upload-ok.mp4');

    expect(res.status).toBe(201);
    expect(res.body).toEqual({ filename: 'upload-ok.mp4' });
    expect(fs.readFileSync(path.join(realVideoDir, 'upload-ok.mp4'), 'utf8')).toBe('fake video');
  });

  test('the uploaded video then shows up in GET /api/videos', async () => {
    await request(app).post('/api/videos').attach('video', Buffer.from('x'), 'upload-listed.webm');

    const res = await request(app).get('/api/videos');

    expect(res.body).toContain('upload-listed.webm');
  });

  test('returns 400 when no file is sent', async () => {
    const res = await request(app).post('/api/videos');

    expect(res.status).toBe(400);
    expect(res.body.error).toMatch(/no video uploaded/i);
  });

  test('returns 400 for a file that is not a video', async () => {
    const res = await request(app).post('/api/videos').attach('video', Buffer.from('hi'), 'notes.txt');

    expect(res.status).toBe(400);
    expect(res.body.error).toMatch(/unsupported file type/i);
    expect(fs.existsSync(path.join(realVideoDir, 'notes.txt'))).toBe(false);
  });

  test('returns 409 instead of overwriting an existing video', async () => {
    fs.writeFileSync(path.join(realVideoDir, 'taken.mp4'), 'original');

    const res = await request(app).post('/api/videos').attach('video', Buffer.from('new'), 'taken.mp4');

    expect(res.status).toBe(409);
    expect(fs.readFileSync(path.join(realVideoDir, 'taken.mp4'), 'utf8')).toBe('original');
  });

  test('never writes outside the videos folder', async () => {
    const res = await request(app).post('/api/videos').attach('video', Buffer.from('x'), '../escaped.mp4');

    expect(res.status).toBe(201);
    expect(res.body.filename).toBe('escaped.mp4');
    expect(fs.existsSync(path.join(realVideoDir, 'escaped.mp4'))).toBe(true);
    expect(fs.existsSync(path.join(realVideoDir, '..', 'escaped.mp4'))).toBe(false);
  });

  test('returns 413 when the file is over the size limit', async () => {
    config.MAX_UPLOAD_BYTES = 4;

    const res = await request(app).post('/api/videos').attach('video', Buffer.from('0123456789'), 'too-big.mp4');

    expect(res.status).toBe(413);
    expect(res.body.error).toMatch(/too large/i);
    expect(fs.existsSync(path.join(realVideoDir, 'too-big.mp4'))).toBe(false);
  });
});
