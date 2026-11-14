import express from 'express';
import dotenv from 'dotenv';
import fs from 'fs/promises';
import path from 'path';
import { spawn } from 'child_process';
import ffmpegPath from 'ffmpeg-static';
import { randomUUID } from 'crypto';

const app = express();
dotenv.config({ path: path.resolve(import.meta.dirname, '..', '.env') });
const PORT = process.env.PORT || 5122;

// resolve folders relative to server/
const ROOT = path.resolve(import.meta.dirname, '..');
const VIDEOS_DIR = path.resolve(ROOT, process.env.VIDEOS_DIR || 'videos');
const RESULTS_DIR = path.resolve(ROOT, process.env.RESULTS_DIR || 'results');
const JAR_DIR = path.resolve(ROOT, process.env.JAR_DIR);

// publicly host raw videos and finished CSVs
app.use('/videos', express.static(VIDEOS_DIR));
app.use('/results', express.static(RESULTS_DIR));

// file types we treat as videos
const VIDEO_EXTENSIONS = ['.mp4', '.mov', '.avi', '.mkv', '.webm'];

const jobs = new Map();

app.use(express.urlencoded({ extended:true }));

// app.get /api/videos -> list of video filenames
app.get('/api/videos', async (req, res) => {
    try {
        const entries = await fs.readdir(VIDEOS_DIR, { withFileTypes: true });
        const videos = entries
            .filter(e => e.isFile())
            .map(e => e.name)
            .filter(name => VIDEO_EXTENSIONS.includes(path.extname(name).toLowerCase()));
        res.status(200).json(videos);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error reading video directory' });
    }
});

// GET /thumbnail/:filename -> first frame as a JPEG
app.get('/thumbnail/:filename', (req, res) => {
    // block path tricks like ../../secret.txt
    const filename = path.basename(req.params.filename);
    const videoPath = path.join(VIDEOS_DIR, filename);

    const ffmpeg = spawn(ffmpegPath, [
        '-i', videoPath,     // input video
        '-frames:v', '1',    // grab only one frame
        '-f', 'image2',      // output as an image
        '-vcodec', 'mjpeg',  // encoded as JPEG
        'pipe:1'             // write to stdout instead of a file
    ], {
        stdio: ['inherit'] // ignore stdin stderr, use stdout
    });

    const chunks = [];
    ffmpeg.stdout.on('data', chunk => chunks.push(chunk));

    ffmpeg.on('close', code => {
        if (code !== 0 || chunks.length === 0) {
            return res.status(500).json({ error: 'Error generating thumbnail' });
        }
        res.status(200).type('image/jpeg').send(Buffer.concat(chunks));
    });

    ffmpeg.on('error', err => {
        console.error(err);
        if (!res.headersSent) res.status(500).json({ error: 'Error generating thumbnail' });
    });
});

// POST /process/:filename?targetColor=<hex>&threshold=<int> -> start async job
app.post('/process/:filename', async (req, res) => {
    const { targetColor, threshold } = req.query;
 
    if (targetColor === undefined || threshold === undefined || targetColor === '' || threshold === '') {
        return res.status(400).json({ error: 'Missing targetColor or threshold query parameter.' });
    }
 
    // accept "ff0000" or "#ff0000"
    const hex = String(targetColor).replace(/^#/, '');
    if (!/^[0-9a-fA-F]{6}$/.test(hex)) {
        return res.status(400).json({ error: 'targetColor must be a 6-digit hex color.' });
    }
 
    const thresholdStr = String(threshold);
    const thresholdNum = Number(thresholdStr);
    if (!/^\d+$/.test(thresholdStr) || thresholdNum < 0 || thresholdNum > 255) {
        return res.status(400).json({ error: 'threshold must be an integer from 0 to 255.' });
    }
 
    // block path tricks like ../../secret.txt
    const filename = path.basename(req.params.filename);
    if (!VIDEO_EXTENSIONS.includes(path.extname(filename).toLowerCase())) {
        return res.status(400).json({ error: 'Unsupported video file type.' });
    }
 
    try {
        await fs.access(path.join(VIDEOS_DIR, filename));
    } catch {
        return res.status(400).json({ error: 'Video file not found.' });
    }
 
    const jobId = randomUUID();
    
    // JAR path arguments
    const inputPath = path.join(VIDEOS_DIR, filename);
    const outputPath = path.join(RESULTS_DIR, `${filename}.csv`);
 
    try {
        // args are passed as an array (no shell), so user input can't inject commands
        const child = spawn(
            'java',
            ['-jar', JAR_DIR, inputPath, outputPath, hex, String(thresholdNum)],
            {
                cwd: ROOT,
                detached: true,   // run independently of the server process
                stdio: 'inherit'   // ignore stdin/stdout/stderr
            }
        );
 
        // wait only for the process to launch (not finish) so we can report start failures
        await new Promise((resolve, reject) => {
            child.once('spawn', resolve);
            child.once('error', reject);
        });
 
        jobs.set(jobId, { status: 'processing' });
 
        // unreference so the child never keeps the server's event loop alive
        child.unref();
 
        // exit/error events still fire while the server is running
        child.on('error', err => {
            console.error(err);
            jobs.set(jobId, { status: 'error', error: `Error processing video: ${err.message}` });
        });
 
        child.on('exit', async (code, signal) => {
            if (code !== 0) {
                const reason = signal ? `terminated by ${signal}` : `exited with code ${code}`;
                jobs.set(jobId, { status: 'error', error: `Error processing video: ${reason}` });
                return;
            }
            // assumes the JAR writes RESULTS_DIR/<filename>.csv
            const csvName = `${filename}.csv`;
            try {
                await fs.access(path.join(RESULTS_DIR, csvName));
                jobs.set(jobId, { status: 'done', result: `/results/${csvName}` });
            } catch {
                console.log('Looking for:', path.join(RESULTS_DIR, csvName));
                jobs.set(jobId, { status: 'error', error: 'Error processing video: result file not found' });
            }
        });
 
        res.status(202).json({ jobId });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error starting job' });
    }
});
 
// GET /process/:jobId/status -> job status
app.get('/process/:jobId/status', (req, res) => {
    try {
        const job = jobs.get(req.params.jobId);
        if (!job) {
            return res.status(404).json({ error: 'Job ID not found' });
        }
        res.status(200).json(job);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Error fetching job status' });
    }
});

app.listen(PORT, () => {
    console.log(`Server started! Access in your browser at the URL below.\nhttp://localhost:${PORT}`);
    console.log(`Videos:  ${VIDEOS_DIR}`);
    console.log(`Results: ${RESULTS_DIR}`);
});