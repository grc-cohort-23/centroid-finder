import path from "node:path";
import { Router } from "express";
import type { AppConfig } from "./app.js";
import { startJob } from "./startJob.js";

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

        try {
            const settings = { jarPath: config.jarPath, videosDir: config.videoDir, resultsDir: config.resultsDir };
            const jobId = await startJob(config.store, settings, filename, targetColor, Number(threshold), config.run);

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
