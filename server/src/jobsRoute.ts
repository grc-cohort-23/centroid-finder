import { Router } from "express";
import type { AppConfig } from "./app.js";
import type { Job, JobStatus } from "./jobStore.js";

// One job the way the frontend wants it (the same as JobRecord in the frontend's types.ts).
// The ? fields are left out when they don't apply, like resultUrl for a job that isn't done.
export type JobRecord = {
    jobId: string;
    filename: string;
    targetColor: string;
    threshold: number;
    status: JobStatus;
    startedAt: string;     // ISO date, like 2026-11-19T18:24:00.000Z
    finishedAt?: string;   // not saved yet (the jobs table has no finished_at column)
    resultUrl?: string;
    error?: string;
};

// Turns a Job from the job store into a JobRecord for the frontend
export function toJobRecord(job: Job): JobRecord {
    const record: JobRecord = {
        jobId: job.id,
        filename: job.filename,
        targetColor: job.targetColor,
        threshold: job.threshold,
        status: job.status,
        startedAt: job.createdAt.toISOString(),
    };
    // result already holds the public URL, like /results/<jobId>.csv (see finishJob),
    // the same value GET /process/:jobId/status sends back
    if (job.status === "done" && job.result) {
        record.resultUrl = job.result;
    }
    if (job.status === "error" && job.error) {
        record.error = job.error;
    }
    return record;
}

// GET /api/jobs: every job, newest first
// GET /api/jobs/:id: one job
export function jobsRoute(config: AppConfig): Router {
    const router = Router();

    router.get("/api/jobs", async (req, res) => {
        try {
            const jobs = await config.store.listJobs();
            res.json(jobs.map(toJobRecord));
        } catch (error) {
            console.error("Could not list jobs:", error);
            res.status(500).json({ error: "Error listing jobs" });
        }
    });

    router.get("/api/jobs/:id", async (req, res) => {
        try {
            const job = await config.store.findJob(req.params.id);
            if (!job) {
                res.status(404).json({ error: "Job ID not found" });
                return;
            }
            res.json(toJobRecord(job));
        } catch (error) {
            console.error("Could not fetch job:", error);
            res.status(500).json({ error: "Error fetching job" });
        }
    });

    return router;
}
