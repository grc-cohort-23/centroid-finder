import path from "path";
import { execFile } from "child_process";
import { Router } from "express";
import type { AppConfig } from "./app.js";

// GET /thumbnail/:filename
// Sends back the video's first frame as a JPEG, using ffmpeg.
export function thumbnailRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/thumbnail/:filename", (req, res) => {
        // basename keeps only the file name, so "../" can't reach other folders
        const videoPath = path.join(config.videoDir, path.basename(req.params.filename));

        // Ask ffmpeg for 1 frame as a JPEG, written to its output (pipe:1) instead of a file
        const args = ["-v", "error", "-i", videoPath, "-frames:v", "1", "-f", "image2", "-c:v", "mjpeg", "pipe:1"];

        execFile("ffmpeg", args, { encoding: "buffer", maxBuffer: 10 * 1024 * 1024 }, (error, stdout) => {
            if (error) {
                // no video, not a video, or ffmpeg isn't installed
                console.error(error.message);
                res.status(500).json({ error: "Error generating thumbnail" });
                return;
            }
            res.type("image/jpeg").send(stdout);
        });
    });

    return router;
}
