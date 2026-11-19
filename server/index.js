const express = require('express');
const { spawn, execFile } = require('node:child_process');
const { v4: uuidv4 } = require('uuid');
const fs = require('node:fs/promises');
const path = require('node:path');
const { createInterface } = require('node:readline');
const { promisify } = require('node:util');

const repoRoot = path.resolve(__dirname, '..');
if (require.main === module) {
    require('dotenv').config({ path: path.join(repoRoot, '.env') });
}

const execute = promisify(execFile);
const previewClass = 'io.github.tonymythical.centroidFinder.VideoPreviewApp';
const videoExtension = /\.(mp4|mov)$/i;
const uuidPattern = /^[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}$/i;

function isVideoFilename(filename) {
    return typeof filename === 'string' && videoExtension.test(filename)
        && !/[/\\\x00-\x1f]/.test(filename) && !filename.startsWith('.');
}

async function isFile(filename) {
    const info = await fs.lstat(filename).catch((error) => {
        if (error.code !== 'ENOENT') throw error;
    });
    return Boolean(info?.isFile());
}

function createApp({
    videosDir = path.resolve(repoRoot, process.env.VIDEOS_DIR || 'processor/sampleInput'),
    outputDir = path.resolve(repoRoot, process.env.OUTPUT_DIR || 'processor/sampleOutput'),
    jarPath = path.resolve(repoRoot, process.env.JAR_PATH || 'processor/target/videoprocessor.jar'),
    runPreview = (args) => execute('java', args, { timeout: 30_000 }),
    spawnJob = (args) => spawn('java', args, {
        detached: true,
        stdio: ['ignore', 'inherit', 'pipe'],
    }),
} = {}) {
    const app = express();
    const previewsDir = path.join(outputDir, 'previews');
    const jobs = new Map();
    app.use(express.json());

    app.use('/videos', async (req, res, next) => {
        const filename = decodeURIComponent(req.path.slice(1));
        if (!isVideoFilename(filename) || !(await isFile(path.join(videosDir, filename)))) {
            return res.status(404).json({ error: 'The selected video was not found.' });
        }
        next();
    }, express.static(videosDir));
    app.use('/outputs', express.static(outputDir));
    app.use('/previews', express.static(previewsDir));

    app.get('/api/videos', async (req, res) => {
        const entries = await fs.readdir(videosDir, { withFileTypes: true }).catch((error) => {
            if (error.code === 'ENOENT') return [];
            throw error;
        });
        res.json(entries.filter((entry) => entry.isFile() && isVideoFilename(entry.name))
            .map((entry) => entry.name).sort());
    });

    async function loadSettings(req, res, next) {
        const { videoFilename, hexColor, tolerance } = req.body ?? {};
        if (!isVideoFilename(videoFilename)) {
            return res.status(400).json({ error: 'Select a valid MP4 or MOV video filename.' });
        }
        if (typeof hexColor !== 'string' || !/^#?[a-f0-9]{6}$/i.test(hexColor)) {
            return res.status(400).json({ error: 'Enter a six-digit hex color.' });
        }
        const threshold = typeof tolerance === 'string' && /^\d+$/.test(tolerance)
            ? Number(tolerance) : tolerance;
        if (!Number.isInteger(threshold) || threshold < 0 || threshold > 442) {
            return res.status(400).json({ error: 'Threshold must be a whole number from 0 to 442.' });
        }

        const inputVideo = path.join(videosDir, videoFilename);
        if (!(await isFile(inputVideo))) {
            return res.status(404).json({ error: 'The selected video was not found.' });
        }

        res.locals.settings = { inputVideo, color: hexColor.replace(/^#/, ''), threshold: String(threshold) };
        next();
    }

    app.post('/api/preview', loadSettings, async (req, res) => {
        const { inputVideo, color, threshold } = res.locals.settings;
        const previewId = uuidv4();
        const directory = path.join(previewsDir, previewId);
        const original = path.join(directory, 'original.png');
        const binarized = path.join(directory, 'binarized.png');
        try {
            await fs.mkdir(directory, { recursive: true });
            const { stdout } = await runPreview([
                '-Djava.awt.headless=true', '-cp', jarPath, previewClass, inputVideo, binarized, color, threshold, original,
            ]);

            const dimensions = stdout.match(/^Frame size: (\d+),\s*(\d+)\s*$/m);
            const coordinates = stdout.match(/^Centroid: (-?\d+),\s*(-?\d+)\s*$/m);
            const [, width, height] = (dimensions ?? []).map(Number);
            const [, x, y] = (coordinates ?? []).map(Number);
            const hasValidCentroid = (x === -1 && y === -1) || (x >= 0 && x < width && y >= 0 && y < height);
            if (!width || !height || !hasValidCentroid || !(await isFile(original)) || !(await isFile(binarized))) {
                throw new Error('Java did not produce both images and valid frame coordinates.');
            }
            res.json({
                originalUrl: `/previews/${previewId}/original.png`,
                previewUrl: `/previews/${previewId}/binarized.png`,
                width,
                height,
                centroid: { x, y },
            });
        } catch (error) {
            await fs.rm(directory, { recursive: true, force: true }).catch(console.error);
            console.error('Preview generation failed:', error.message);
            res.status(500).json({ error: 'Could not generate a preview. Check that the video uses a supported codec and the Java processor is built.' });
        }
    });

    app.post('/jobs', loadSettings, async (req, res) => {
        const { inputVideo, color, threshold } = res.locals.settings;
        const jobId = uuidv4();
        const filename = `${jobId}_output.csv`;
        const output = path.join(outputDir, filename);
        const staged = path.join(outputDir, `.${filename}`);
        await fs.mkdir(outputDir, { recursive: true });

        jobs.set(jobId, { status: 'processing' });
        const javaProcess = spawnJob(['-jar', jarPath, inputVideo, staged, color, threshold]);
        let reportedError = false;
        // The original Java CLI can log a processing error and still exit with code 0.
        createInterface({ input: javaProcess.stderr }).on('line', (line) => {
            console.error(line);
            if (/^Error processing video(?: frames)?:/.test(line)) reportedError = true;
        });
        javaProcess.once('error', (error) => console.error('Could not start Java:', error.message));
        javaProcess.once('close', async (code) => {
            try {
                if (code !== 0 || reportedError) throw new Error('The Java process reported a failure.');
                await fs.rename(staged, output);
                jobs.delete(jobId);
            } catch (error) {
                console.error('Video processing failed:', error.message);
                jobs.set(jobId, { status: 'failed', error: 'Video processing failed. Check the video and try again.' });
                await fs.rm(staged, { force: true }).catch(console.error);
            }
        });
        javaProcess.unref();

        res.status(202).json({ jobId, status: 'processing', message: 'Video processing job started in the background.' });
    });

    app.get('/jobs/:jobId', async (req, res) => {
        res.set('Cache-Control', 'no-store');
        const { jobId } = req.params;
        if (!uuidPattern.test(jobId)) {
            return res.status(400).json({ error: 'Invalid job ID.' });
        }

        const job = jobs.get(jobId);
        if (job) return res.json({ jobId, ...job });

        const csvFilename = `${jobId}_output.csv`;
        if (await isFile(path.join(outputDir, csvFilename))) {
            return res.json({ jobId, status: 'completed', downloadUrl: `/outputs/${csvFilename}` });
        }

        res.status(404).json({ error: 'Job not found. Start processing again if the server was restarted.' });
    });

    app.use((error, req, res, next) => {
        if (res.headersSent) return next(error);
        if (error instanceof URIError) {
            return res.status(400).json({ error: 'Invalid URL.' });
        }
        if (error.type === 'entity.parse.failed') {
            return res.status(400).json({ error: 'Send a valid JSON request.' });
        }
        console.error(error.message);
        res.status(error.status || 500).json({ error: 'The server could not complete the request.' });
    });

    return app;
}

if (require.main === module) {
    const port = process.env.PORT || 3000;
    createApp().listen(port, (error) => {
        if (!error) console.log(`Express server running on port ${port}`);
    })
        .on('error', (error) => {
            console.error('Could not start Express:', error.message);
            process.exitCode = 1;
        });
}

module.exports = { createApp };
