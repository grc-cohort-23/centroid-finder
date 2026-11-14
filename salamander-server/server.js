const express = require("express");
const { execFile } = require("child_process");
const fs = require("fs");
const path = require("path");
require("dotenv").config(); 


const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

app.get("/", (req, res) => {
    res.send("Salamander Video Server is running!");
});

app.get("/api/videos", (req, res) => {
    const videosFolder = path.join(__dirname, "videos");

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

    const videoPath = path.join(__dirname, "videos", filename);
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




app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});