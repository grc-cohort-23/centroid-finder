import fs from "fs/promises";
import { Router } from "express";
import type { AppConfig } from "./app.js";
import { isVideoFile } from "./videoInfo.js";

// GET /api/videos
// Lists the video files in the videos folder.
export function videosRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/api/videos", async (req, res) => {
        try {
            const files = await fs.readdir(config.videoDir);
            const videos = files.filter(isVideoFile);
            res.json(videos);
        } catch (error) {
            console.error(error);
            res.status(500).json({ error: "Error reading video directory" });
        }
    });

    return router;
}
