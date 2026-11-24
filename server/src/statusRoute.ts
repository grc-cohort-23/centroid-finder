import { Router } from "express";
import type { AppConfig } from "./app.js";

// GET /process/:jobId/status
// Tells the client if a job is still processing, done, or failed.
export function statusRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/process/:jobId/status", async (req, res) => {
        const jobId = req.params.jobId;

        try {
            const job = await config.store.findJob(jobId);

            // no job with this id (404 = it doesn't exist)
            if (!job) {
                res.status(404).json({ error: "Job ID not found" });
                return;
            }

            // a failed job is still a 200: the status request itself worked
            if (job.status === "done") {
                res.status(200).json({ status: "done", result: job.result });
            } else if (job.status === "error") {
                res.status(200).json({ status: "error", error: job.error });
            } else {
                res.status(200).json({ status: "processing" });
            }
        } catch (error) {
            // 500 = the server's fault, like the database being down
            console.error("Could not fetch job status:", error);
            res.status(500).json({ error: "Error fetching job status" });
        }
    });

    return router;
}
