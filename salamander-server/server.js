const express = require("express");
const { execFile } = require("child_process");
const fs = require("fs");
const path = require("path");
const { randomUUID } = require("crypto");
const { isValidColor, isValidThreshold } = require("./validation");


require("dotenv").config(); 


const app = express();
const PORT = process.env.PORT || 3000;
const jobs = new Map();

app.use(express.json());
app.use("/results", express.static(path.join(__dirname, "results")));

app.get("/", (req, res) => {
    res.send("Salamander Video Server is running!");
});

app.get("/api/videos", (req, res) => {
    const videosFolder = process.env.VIDEOS_DIR || path.join(__dirname, "videos");

    fs.readdir(videosFolder, (error, files) => {
        if (error) {
            return res.status(500).json({ error: "Could not read videos folder" });
        }

        res.json({ videos: files });
    });
});


app.get("/thumbnail/:filename", (req, res) => {
    const filename = req.params.filename;

    if (path.basename(filename) !== filename) {
        return res.status(400).json({ error: "Invalid filename" });
    }

    const videosFolder = process.env.VIDEOS_DIR || path.join(__dirname, "videos");
    const videoPath = path.join(videosFolder, filename);
    const thumbnailPath = path.join(__dirname, "results", filename + ".jpg");

    if (!fs.existsSync(videoPath)) {
        return res.status(404).json({ error: "Video not found" });
    }

    execFile("ffmpeg", [
        "-y",
        "-i", videoPath,
        "-frames:v", "1",
        thumbnailPath
    ], (error) => {
        if (error) {
            console.error("Thumbnail error:", error);
            return res.status(500).json({ error: "Could not create thumbnail" });
        }

        res.sendFile(thumbnailPath);
    });
});


app.post("/process/:filename", (req, res) => {
    const filename = req.params.filename;
    const { targetColor, threshold } = req.body;

    // Check that the video filename is valid
    if (path.basename(filename) !== filename) {
        return res.status(400).json({ error: "Invalid filename" });
    }

    // Check that the video exists
    const videosFolder = process.env.VIDEOS_DIR || path.join(__dirname, "videos");
    const videoPath = path.join(videosFolder, filename);

    if (!fs.existsSync(videoPath)) {
        return res.status(404).json({ error: "Video not found" });
    }

    // Check that processing options were provided
    if (targetColor === undefined || threshold === undefined) {
        return res.status(400).json({
            error: "Target color and threshold are required"
        });
    }

    // Validate target color (6 hexadecimal characters)
    if (!isValidColor(targetColor)) {
        return res.status(400).json({
            error: "Invalid target color"
        });
    }

    // Validate threshold
    if (!isValidThreshold(threshold)) {
        return res.status(400).json({
            error: "Invalid threshold"
        });
    }

    // Create a unique job ID
    const jobId = randomUUID();

    // Set the output CSV file path
    const outputCsv = path.join(__dirname, "results", `${jobId}.csv`);

    // Set the Java JAR file path
    const jarPath = process.env.JAR_PATH || path.join(__dirname, "..", "target", "videoprocessor.jar");

    // Save the job information
    jobs.set(jobId, {
        status: "running",
        filename: filename
    });

    // Start the Java video processor
    execFile("java", [
        "-jar",
        jarPath,
        videoPath,
        outputCsv,
        targetColor,
        String(threshold)
    ], (error) => {
        if (error) {
            console.error("Video processing error:", error);

            jobs.set(jobId, {
                status: "failed",
                filename: filename
            });

            return;
        }

        jobs.set(jobId, {
            status: "completed",
            filename: filename,
            outputCsv: outputCsv
        });
    });

    // Respond without waiting for processing to finish
    res.status(202).json({
        jobId: jobId,
        status: "running"
    });

});


app.get("/process/:jobId/status", (req, res) => {
    const jobId = req.params.jobId;

    // Find the job using its ID
    const job = jobs.get(jobId);

    // Return 404 if the job does not exist
    if (!job) {
        return res.status(404).json({
            error: "Job not found"
        });
    }

    // Return the current job information
    res.json({
        jobId: jobId,
        status: job.status,
        filename: job.filename,
        result: job.status === "completed"
            ? `/results/${jobId}.csv`
            : null
    });
});

if (require.main === module) {
    app.listen(PORT, () => {
        console.log(`Server running on http://localhost:${PORT}`);
    });
}

module.exports = app;