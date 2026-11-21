import path from "node:path";
import { v4 as uuidv4 } from "uuid";
import type { JobStore } from "./jobStore.js";
import { buildJarArgs, runInBackground, statusFromExitCode } from "./jarRunner.js";

// Runs when the JAR exits: saves the job as done (with the CSV's URL) or as an error.
export async function finishJob(store: JobStore, jobId: string, code: number | null): Promise<void> {
    if (statusFromExitCode(code) === "done") {
        // the URL the client can open, because express.static serves the results folder at /results
        await store.markDone(jobId, `/results/${jobId}.csv`);
    } else if (code === null) {
        await store.markError(jobId, "Error processing video: the JAR could not start");
    } else {
        await store.markError(jobId, `Error processing video: the JAR exited with code ${code}`);
    }
}

// The paths startJob needs (the real server gets them from .env)
export type JobSettings = {
    jarPath: string;
    videosDir: string;
    resultsDir: string;
};

// Starts processing a video: saves the job as processing, starts the JAR in the background,
// and gives back the jobId right away, without waiting for the JAR to finish.
// run is runInBackground in the real server, and a fake one in tests.
export async function startJob(
    store: JobStore,
    settings: JobSettings,
    filename: string,
    targetColor: string,
    threshold: number,
    run: typeof runInBackground = runInBackground
): Promise<string> {
    const jobId = uuidv4();
    await store.createJob(jobId, filename, targetColor, threshold);

    const args = buildJarArgs(
        settings.jarPath,
        path.join(settings.videosDir, filename),
        path.join(settings.resultsDir, `${jobId}.csv`),
        targetColor,
        String(threshold)
    );

    run("java", args, (code) => {
        // if saving the result fails (like Neon is down), log it instead of crashing the server
        finishJob(store, jobId, code).catch((error) => console.error("Could not save the job's result:", error));
    });

    return jobId;
}
