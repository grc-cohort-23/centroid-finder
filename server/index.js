const express = require('express');
const { spawn } = require('child_process');
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const fs = require('fs');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const app = express();
app.use(express.json());

// Resolve absolute directories based on .env config
const videosDir = path.join(__dirname, '../', process.env.VIDEOS_DIR || 'processor/sampleInput');
const outputDir = path.join(__dirname, '../', process.env.OUTPUT_DIR || 'processor/sampleOutput');

// Expose static files so clients/frontend can access videos and output CSVs
app.use('/videos', express.static(videosDir));
app.use('/outputs', express.static(outputDir));

/**
 * POST /jobs
 * Initiates video processing asynchronously using the existing Java JAR.
 * Expected JSON body: { "videoFilename": "ensantina_small.mp4", "hexColor": "FFA200", "tolerance": "164" }
 */
app.post('/jobs', (req, res) => {
    const { videoFilename, hexColor, tolerance } = req.body;

    if (!videoFilename || !hexColor || !tolerance) {
        return res.status(400).json({ error: 'Missing required parameters: videoFilename, hexColor, tolerance' });
    }

    const jobId = uuidv4();
    const jarPath = path.join(__dirname, '../', process.env.JAR_PATH || 'processor/target/videoprocessor.jar');
    const inputVideo = path.join(videosDir, videoFilename);
    const outputCsv = path.join(outputDir, `${jobId}_output.csv`);

    // Verify input video exists before spawning
    if (!fs.existsSync(inputVideo)) {
        return res.status(404).json({ error: `Video file '${videoFilename}' not found in sampleInput directory.` });
    }

    // Spawn Java process with stdio inherited so output prints to terminal
    const javaProcess = spawn('java', ['-jar', jarPath, inputVideo, outputCsv, hexColor, tolerance], {
        detached: true,
        stdio: ['ignore', 'inherit', 'inherit']
    });

    javaProcess.unref();

    // Respond immediately with 202 Accepted and the jobId
    res.status(202).json({
        jobId: jobId,
        status: 'processing',
        message: 'Video processing job started in the background.'
    });
});

/**
 * GET /jobs/:jobId
 * Checks whether the background Java process has finished by verifying if the output CSV exists.
 */
app.get('/jobs/:jobId', (req, res) => {
    const { jobId } = req.params;
    const csvFilename = `${jobId}_output.csv`;
    const csvPath = path.join(outputDir, csvFilename);

    if (fs.existsSync(csvPath)) {
        res.json({
            jobId: jobId,
            status: 'completed',
            downloadUrl: `/outputs/${csvFilename}`
        });
    } else {
        res.json({
            jobId: jobId,
            status: 'processing'
        });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Express server running on port ${PORT}`);
});