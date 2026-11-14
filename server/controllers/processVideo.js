import { existsSync } from 'fs';
import { v4 as generateUUID } from 'uuid';
import { addJob, findJob, updateJobStatus } from '../services/db.service.js';
import {spawn} from 'child_process';

/**
 * Runs a video through the video processor, 
 * creating a jobId to track the current status of
 * the processor.
 * 
 */
export default function processVideo(req, res) {
    // Create jobId
    const jobId = generateUUID();
    // Confirm jobId does not exist
    if (findJob(jobId) != undefined) {
        res.status(500).send({ message: "Error starting job" });
    };
    // Confirm queries were provided
    if (! (req.query.targetColor && req.query.threshold) ) {
        res.status(400).send({ "error": "Missing targetColor or threshold query parameter."})
    }
    const inputPath = req.params.filename;
    const targetColor = req.query.targetColor;
    const threshold = req.query.threshold;

    const jobMetadata = {
        id: jobId,
        targetColor,
        threshold,
        inputPath,
        status: "processing"
    }

    // Insert into database if successful
    try {
        addJob(jobMetadata);
    } catch (e) {
        console.error(`Database Error: ${e}`);
    }

    // Run jar with job metadata
    // TODO: Run jar
    console.log("Starting");
    const jar = spawn("java", ["-jar", "../processor/target/videoprocessor.jar", `videos/${inputPath}`, `public/results/${jobId}.csv`, targetColor, threshold], { stdio: "inherit" });

    jar.on("close", () => {
        const fileMadeSuccess = (existsSync(`public/results/${jobId}.csv`));
        updateJobStatus(jobId, fileMadeSuccess ? "done" : "error");
    });

    // Send jobId back in accepted response
    res.status(202).send({ jobId })
}