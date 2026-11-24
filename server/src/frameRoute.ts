import path from "path";
import { execFile } from "child_process";
import { Router } from "express";
import type { AppConfig } from "./app.js";

// Biggest PNG we expect from ffmpeg. A 4K frame as PNG can be about 25 MB.
const MAX_FRAME_BYTES = 50 * 1024 * 1024;

// Reads ?t=1.5 as a number of seconds. undefined if it's missing, not a number, or below 0.
export function parseSeconds(value: unknown): number | undefined {
    if (typeof value !== "string" || value.trim() === "") return undefined;
    const seconds = Number(value);
    if (!Number.isFinite(seconds) || seconds < 0) return undefined;
    return seconds;
}

// GET /frame/:filename?t=<seconds>
// Sends back one frame of the video as a PNG, like /thumbnail but at any time.
// PNG (not JPEG) so the colors are exact, which matters for picking the target color.
export function frameRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/frame/:filename", (req, res) => {
        const seconds = parseSeconds(req.query.t);
        if (seconds === undefined) {
            res.status(400).json({ error: "t must be a number of seconds, 0 or more" });
            return;
        }

        // basename keeps only the file name, so "../" can't reach other folders (same as /thumbnail)
        const videoPath = path.join(config.videoDir, path.basename(req.params.filename));

        // -ss before -i jumps to the closest keyframe, then ffmpeg decodes forward to the exact time.
        // Then 1 frame, as a PNG, written to its output (pipe:1) instead of a file.
        const args = [
            "-v", "error",
            "-ss", String(seconds),
            "-i", videoPath,
            "-frames:v", "1",
            "-f", "image2", "-c:v", "png",
            "pipe:1",
        ];

        execFile("ffmpeg", args, { encoding: "buffer", maxBuffer: MAX_FRAME_BYTES }, (error, stdout) => {
            if (error) {
                // no video, not a video, or ffmpeg isn't installed
                console.error(error.message);
                res.status(500).json({ error: "Error extracting frame" });
                return;
            }
            // ffmpeg finishes without an error but sends nothing when t is after the end of the video
            if (stdout.length === 0) {
                res.status(404).json({ error: "No frame at that time" });
                return;
            }
            res.type("image/png").send(stdout);
        });
    });

    return router;
}
