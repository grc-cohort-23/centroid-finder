import fs from "fs/promises";
import path from "path";
import { Router } from "express";
import type { AppConfig } from "./app.js";
import { isVideoFile, probeVideo, videoStatus, type VideoInfo } from "./videoInfo.js";

// GET /api/videos/details
// Like GET /api/videos, but each video also has a status (from its newest job) and,
// if ffprobe can read it, its length and size. GET /api/videos stays a plain list of
// names, because that's what the API spec says.
export function videoDetailsRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/api/videos/details", async (req, res) => {
        try {
            const files = (await fs.readdir(config.videoDir)).filter(isVideoFile);
            const jobs = await config.store.listJobs(); // newest first

            // Promise.all runs ffprobe on every video at the same time, instead of one by one
            const videos = await Promise.all(
                files.map(async (filename): Promise<VideoInfo> => {
                    const probe = await probeVideo(path.join(config.videoDir, filename));
                    return { filename, status: videoStatus(filename, jobs), ...probe };
                })
            );
            res.json(videos);
        } catch (error) {
            console.error("Could not list video details:", error);
            res.status(500).json({ error: "Error reading video details" });
        }
    });

    return router;
}
