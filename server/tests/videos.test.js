const fs = require('fs');
const path = require('path');
const request = require('supertest');
const app = require('../app');
const config = require('../config');

describe('GET /api/videos', () => {
  const realVideoDir = config.VIDEO_DIR;

  beforeAll(() => {
    fs.writeFileSync(path.join(realVideoDir, 'ensantina.mp4'), '');
    fs.writeFileSync(path.join(realVideoDir, 'clip.MOV'), '');
    fs.writeFileSync(path.join(realVideoDir, 'notes.txt'), '');
    fs.writeFileSync(path.join(realVideoDir, '.DS_Store'), '');
    fs.mkdirSync(path.join(realVideoDir, 'folder.mp4'));
  });

  afterEach(() => {
    config.VIDEO_DIR = realVideoDir;
  });

  test('returns 200 with only the video files', async () => {
    const res = await request(app).get('/api/videos');

    expect(res.status).toBe(200);
    expect(res.body.sort()).toEqual(['clip.MOV', 'ensantina.mp4']);
  });

  test('returns an empty array when there are no videos', async () => {
    const emptyDir = path.join(realVideoDir, '..', 'empty-videos');
    fs.mkdirSync(emptyDir, { recursive: true });
    config.VIDEO_DIR = emptyDir;

    const res = await request(app).get('/api/videos');

    expect(res.status).toBe(200);
    expect(res.body).toEqual([]);
  });

  test('returns 500 when the folder cannot be read', async () => {
    config.VIDEO_DIR = path.join(realVideoDir, 'missing-folder');

    const res = await request(app).get('/api/videos');

    expect(res.status).toBe(500);
    expect(res.body).toEqual({ error: 'Error reading video directory' });
  });
});
