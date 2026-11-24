const assert = require('node:assert/strict');
const { EventEmitter, once } = require('node:events');
const fs = require('node:fs/promises');
const os = require('node:os');
const path = require('node:path');
const { PassThrough } = require('node:stream');
const { setTimeout: delay } = require('node:timers/promises');
const { test } = require('node:test');
const { createApp } = require('../index');

const videoFilename = 'a clip #1.mp4';
const settings = { videoFilename, hexColor: '#FFA200', tolerance: 0 };

async function startApp(t, javaOptions) {
    const root = await fs.mkdtemp(path.join(os.tmpdir(), 'centroid-api-'));
    const videosDir = path.join(root, 'videos');
    const outputDir = path.join(root, 'outputs');
    await fs.mkdir(videosDir);
    await fs.writeFile(path.join(videosDir, videoFilename), 'test video');
    await fs.mkdir(path.join(videosDir, 'directory.mov'));
    await fs.symlink(path.join(videosDir, videoFilename), path.join(videosDir, 'symlink.mp4'));
    const server = createApp({ videosDir, outputDir, ...javaOptions }).listen(0, '127.0.0.1');
    t.after(async () => {
        await new Promise((resolve) => server.close(resolve));
        await fs.rm(root, { recursive: true, force: true });
    });
    await once(server, 'listening');
    const baseUrl = `http://127.0.0.1:${server.address().port}`;

    const postJson = (url, body) => fetch(baseUrl + url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body,
    });
    return {
        videosDir,
        outputDir,
        get: (url) => fetch(baseUrl + url),
        post: (url, body) => postJson(url, JSON.stringify(body)),
        postJson,
    };
}

test('lists video files and validates both processing endpoints before launching Java', async (t) => {
    const unexpectedJava = () => assert.fail('Invalid settings should not launch Java.');
    const app = await startApp(t, { runPreview: unexpectedJava, spawnJob: unexpectedJava });
    assert.deepEqual(await (await app.get('/api/videos')).json(), [videoFilename]);
    assert.equal(await (await app.get(`/videos/${encodeURIComponent(videoFilename)}`)).text(), 'test video');
    assert.equal((await app.get('/videos/symlink.mp4')).status, 404);

    for (const endpoint of ['/api/preview', '/jobs']) {
        for (const invalid of [
            { videoFilename: '../secret.mp4' },
            { videoFilename: '..\\secret.mp4' },
            { hexColor: '#notrgb' },
            { tolerance: '' },
            { tolerance: 442.1 },
            { tolerance: 443 },
        ]) {
            const response = await app.post(endpoint, { ...settings, ...invalid });
            assert.equal(response.status, 400);
            assert.ok((await response.json()).error);
        }
        for (const missing of ['missing.mp4', 'symlink.mp4', 'directory.mov']) {
            assert.equal((await app.post(endpoint, { ...settings, videoFilename: missing })).status, 404);
        }
    }
});

test('rejects malformed JSON before launching Java', async (t) => {
    const unexpectedJava = t.mock.fn();
    const app = await startApp(t, { runPreview: unexpectedJava, spawnJob: unexpectedJava });

    for (const endpoint of ['/api/preview', '/jobs']) {
        const response = await app.postJson(endpoint, '{"videoFilename":');
        assert.equal(response.status, 400);
        assert.deepEqual(await response.json(), { error: 'Send a valid JSON request.' });
    }
    assert.equal(unexpectedJava.mock.callCount(), 0);
});

test('returns an empty video list when the videos directory is missing', async (t) => {
    const unexpectedJava = t.mock.fn();
    const app = await startApp(t, { runPreview: unexpectedJava, spawnJob: unexpectedJava });
    await fs.rename(app.videosDir, `${app.videosDir}-offline`);

    const response = await app.get('/api/videos');
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), []);
    for (const endpoint of ['/api/preview', '/jobs']) {
        assert.equal((await app.post(endpoint, settings)).status, 404);
    }
    assert.equal(unexpectedJava.mock.callCount(), 0);
});

test('reports a staging failure when the output directory cannot be created', async (t) => {
    const spawnJob = t.mock.fn();
    const app = await startApp(t, { spawnJob });
    await fs.writeFile(app.outputDir, 'file blocking the output directory');

    const response = await app.post('/jobs', settings);
    assert.equal(response.status, 500);
    assert.deepEqual(await response.json(), { error: 'The server could not complete the request.' });
    assert.equal(spawnJob.mock.callCount(), 0);
});

test('serves two frame images from unique paths and parses Java coordinates, including no match', async (t) => {
    const runPreview = async ([headless, classpath, jar, mainClass, input, binary, color, threshold, original]) => {
        assert.equal(headless, '-Djava.awt.headless=true');
        assert.equal(classpath, '-cp');
        assert.equal(path.basename(jar), 'videoprocessor.jar');
        assert.match(mainClass, /VideoPreviewApp$/);
        assert.equal(path.basename(input), videoFilename);
        assert.equal(color, 'FFA200');
        await Promise.all([fs.writeFile(binary, 'binary'), fs.writeFile(original, 'original')]);
        const coordinates = threshold === '0' ? '-1, -1' : '15,22';
        return { stdout: `Frame size: 32,24\nCentroid: ${coordinates}\n` };
    };
    const app = await startApp(t, { runPreview });

    const responses = await Promise.all([
        app.post('/api/preview', settings),
        app.post('/api/preview', { ...settings, tolerance: '150' }),
    ]);
    assert.deepEqual(responses.map((response) => response.status), [200, 200]);
    const [empty, match] = await Promise.all(responses.map((response) => response.json()));
    assert.deepEqual(empty.centroid, { x: -1, y: -1 });
    assert.deepEqual(match.centroid, { x: 15, y: 22 });
    assert.equal(match.width, 32);
    assert.equal(match.height, 24);
    assert.notEqual(empty.previewUrl, match.previewUrl);
    assert.notEqual(empty.originalUrl, match.originalUrl);
    for (const preview of [empty, match]) {
        assert.equal(await (await app.get(preview.previewUrl)).text(), 'binary');
        assert.equal(await (await app.get(preview.originalUrl)).text(), 'original');
    }
});

test('reports and removes incomplete previews', async (t) => {
    const runPreview = async (args) => {
        await fs.writeFile(args[5], 'binary');
        return { stdout: 'Frame size: 32,24\nCentroid: 15,22\n' };
    };
    const app = await startApp(t, { runPreview });

    const response = await app.post('/api/preview', settings);
    assert.equal(response.status, 500);
    assert.match((await response.json()).error, /Could not generate a preview/);
    assert.deepEqual(await fs.readdir(path.join(app.outputDir, 'previews')), []);
});

async function waitForJob(app, jobId, expectedStatus) {
    for (let attempt = 0; attempt < 50; attempt++) {
        const response = await app.get(`/jobs/${jobId}`);
        assert.equal(response.status, 200);
        const job = await response.json();
        if (job.status !== 'processing') {
            assert.equal(job.status, expectedStatus);
            return job;
        }
        await delay(20);
    }
    assert.fail(`Job did not reach ${expectedStatus}`);
}

function fakeChildProcess() {
    const child = new EventEmitter();
    child.stderr = new PassThrough();
    child.unref = () => {};
    return child;
}

test('creates a missing output directory and publishes a CSV only after Java succeeds', async (t) => {
    const child = fakeChildProcess();
    let staged;
    const spawnJob = ([mode, jar, input, output, color, threshold]) => {
        assert.equal(mode, '-jar');
        assert.equal(path.basename(input), videoFilename);
        assert.equal(color, 'FFA200');
        assert.equal(threshold, '0');
        staged = output;
        return child;
    };
    const app = await startApp(t, { spawnJob });
    await assert.rejects(fs.stat(app.outputDir), { code: 'ENOENT' });
    const response = await app.post('/jobs', settings);
    assert.equal(response.status, 202);
    const { jobId } = await response.json();
    await fs.writeFile(staged, '0.0,15,22\n');
    assert.equal((await (await app.get(`/jobs/${jobId}`)).json()).status, 'processing');
    assert.equal((await app.get(`/outputs/${path.basename(staged)}`)).status, 404);
    assert.equal((await app.get(`/outputs/${jobId}_output.csv`)).status, 404);
    child.stderr.end();
    child.emit('close', 0);
    const job = await waitForJob(app, jobId, 'completed');
    assert.equal(await (await app.get(job.downloadUrl)).text(), '0.0,15,22\n');
});

test('fails a job when Java exits successfully without a staged CSV', async (t) => {
    const child = fakeChildProcess();
    const app = await startApp(t, { spawnJob: () => child });
    const response = await app.post('/jobs', settings);
    assert.equal(response.status, 202);
    const { jobId } = await response.json();

    child.stderr.end();
    child.emit('close', 0);
    const job = await waitForJob(app, jobId, 'failed');
    assert.match(job.error, /Video processing failed/);
    assert.equal(job.downloadUrl, undefined);
    assert.equal((await app.get(`/outputs/${jobId}_output.csv`)).status, 404);
});

test('does not publish a partial CSV when Java exits nonzero', async (t) => {
    const child = fakeChildProcess();
    let staged;
    const app = await startApp(t, {
        spawnJob: (args) => {
            staged = args[3];
            return child;
        },
    });
    const response = await app.post('/jobs', settings);
    assert.equal(response.status, 202);
    const { jobId } = await response.json();
    await fs.writeFile(staged, '0.0,15,22\n');

    child.stderr.end();
    child.emit('close', 1);
    const job = await waitForJob(app, jobId, 'failed');
    assert.match(job.error, /Video processing failed/);
    assert.equal(job.downloadUrl, undefined);
    assert.equal((await app.get(`/outputs/${jobId}_output.csv`)).status, 404);
});

test('rejects a partial CSV when the original Java code logs a failure but exits zero', async (t) => {
    const child = fakeChildProcess();
    let staged;
    const spawnJob = (args) => {
        staged = args[3];
        return child;
    };
    const app = await startApp(t, { spawnJob });
    const { jobId } = await (await app.post('/jobs', settings)).json();
    await fs.writeFile(staged, '0.0,15,22\n');
    child.stderr.end('Error processing video frames: could not decode frame\n');
    child.emit('close', 0);
    const job = await waitForJob(app, jobId, 'failed');
    assert.match(job.error, /Video processing failed/);
    assert.equal((await app.get(`/outputs/${jobId}_output.csv`)).status, 404);
    assert.equal((await app.get('/jobs/11111111-1111-4111-8111-111111111111')).status, 404);
    assert.equal((await app.get('/jobs/invalid')).status, 400);
});
