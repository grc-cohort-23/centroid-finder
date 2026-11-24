import path from "node:path";
import { stat } from "node:fs/promises";
import { Router } from "express";
import type { AppConfig } from "./app.js";
import { startJob } from "./startJob.js";
import { isVideoFile } from "./videoInfo.js";

// The same rules as ArgumentParser in the JAR, so bad input is caught here instead of failing later
// exactly 6 characters, each 0-9 or A-F (upper or lowercase), like ff0000
const HEX_COLOR = /^[0-9A-Fa-f]{6}$/;
// only digits, so it's a whole number that is 0 or higher (no letters, decimals, or minus sign)
const WHOLE_NUMBER = /^\d+$/;
// the biggest number an INTEGER can hold, in both our jobs table and a Java int (ArgumentParser)
const MAX_THRESHOLD = 2147483647;

// true if the path is a real file (not a folder, and not missing)
async function isRealFile(filePath: string): Promise<boolean> {
    try {
        return (await stat(filePath)).isFile();
    } catch {
        // stat throws when nothing is at that path
        return false;
    }
}

// POST /process/:filename?targetColor=...&threshold=...
// Starts a job in the background and sends back its jobId right away.
export function processRoute(config: AppConfig): Router {
    const router = Router();

    router.post("/process/:filename", async (req, res) => {
        // basename keeps only the file name, so "../" can't reach other folders (same as /thumbnail)
        const filename = path.basename(req.params.filename);
        const targetColor = req.query.targetColor;
        const threshold = req.query.threshold;

        // both query parameters are required (400 = the client's fault)
        if (typeof targetColor !== "string" || typeof threshold !== "string") {
            res.status(400).json({ error: "Missing targetColor or threshold query parameter." });
            return;
        }

        if (!HEX_COLOR.test(targetColor)) {
            res.status(400).json({ error: "targetColor must be 6 hex digits in RRGGBB format (example: ff0000)." });
            return;
        }

        if (!WHOLE_NUMBER.test(threshold)) {
            res.status(400).json({ error: "threshold must be a whole number, 0 or higher (example: 120)." });
            return;
        }

        // too big for the database's INTEGER column, which would give a 500 instead of a 400
        const thresholdValue = Number(threshold);
        if (thresholdValue > MAX_THRESHOLD) {
            res.status(400).json({ error: `threshold must be ${MAX_THRESHOLD} or less.` });
            return;
        }

        if (!isVideoFile(filename)) {
            res.status(400).json({ error: "The file must be a video (.mp4 or .mov)." });
            return;
        }

        // checked last, because it's the only check that has to look at the disk
        if (!(await isRealFile(path.join(config.videoDir, filename)))) {
            res.status(400).json({ error: "No video with that name was found." });
            return;
        }

        try {
            const settings = { jarPath: config.jarPath, videosDir: config.videoDir, resultsDir: config.resultsDir };
            const jobId = await startJob(config.store, settings, filename, targetColor, thresholdValue, config.run);

            // 202 = "got it, working on it" (the JAR keeps running in the background)
            res.status(202).json({ jobId });
        } catch (error) {
            // 500 = the server's fault, like the database being down
            console.error("Could not start job:", error);
            res.status(500).json({ error: "Error starting job" });
        }
    });

    return router;
}
