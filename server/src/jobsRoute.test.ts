import { describe, it, expect } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";
import type { Job } from "./jobStore.js";

// Settings for a test app: fake folders, the fake job store, and a runner that does nothing
function makeConfig(store: FakeJobStore): AppConfig {
    return {
        videoDir: "/videos",
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store,
        run: () => {},
    };
}

// A job store that always fails, like when the database is down
class BrokenJobStore extends FakeJobStore {
    async findJob(id: string): Promise<Job | undefined> {
        throw new Error("database is down");
    }
    async listJobs(): Promise<Job[]> {
        throw new Error("database is down");
    }
}

// A store with 3 jobs: an old done one, an error one, and a new one still processing
async function storeWithJobs(): Promise<FakeJobStore> {
    const store = new FakeJobStore();
    await store.createJob("job-1", "intro.mp4", "ff0000", 120);
    await store.markDone("job-1", "/results/job-1.csv");
    await store.createJob("job-2", "demo.mov", "00ff00", 80);
    await store.markError("job-2", "Error processing video: the JAR exited with code 1");
    await store.createJob("job-3", "intro.mp4", "0000ff", 50);

    // give each job its own time, so the order is clear
    const times = ["2026-11-19T10:00:00.000Z", "2026-11-19T11:00:00.000Z", "2026-11-19T12:00:00.000Z"];
    ["job-1", "job-2", "job-3"].forEach((id, i) => {
        const job = store.jobs.get(id);
        if (job) job.createdAt = new Date(times[i] ?? "");
    });
    return store;
}

describe("GET /api/jobs", () => {
    it("sends 200 with every job, newest first, in the frontend's JobRecord shape", async () => {
        // Arrange
        const app = createApp(makeConfig(await storeWithJobs()));

        // Act
        const response = await request(app).get("/api/jobs");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual([
            {
                jobId: "job-3",
                filename: "intro.mp4",
                targetColor: "0000ff",
                threshold: 50,
                status: "processing",
                startedAt: "2026-11-19T12:00:00.000Z",
            },
            {
                jobId: "job-2",
                filename: "demo.mov",
                targetColor: "00ff00",
                threshold: 80,
                status: "error",
                startedAt: "2026-11-19T11:00:00.000Z",
                error: "Error processing video: the JAR exited with code 1",
            },
            {
                jobId: "job-1",
                filename: "intro.mp4",
                targetColor: "ff0000",
                threshold: 120,
                status: "done",
                startedAt: "2026-11-19T10:00:00.000Z",
                resultUrl: "/results/job-1.csv",
            },
        ]);
    });

    it("sends 200 with an empty list when there are no jobs", async () => {
        // Arrange
        const app = createApp(makeConfig(new FakeJobStore()));

        // Act
        const response = await request(app).get("/api/jobs");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual([]);
    });

    it("sends 500 when the job store fails", async () => {
        // Arrange
        const app = createApp(makeConfig(new BrokenJobStore()));

        // Act
        const response = await request(app).get("/api/jobs");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error listing jobs" });
    });
});

describe("GET /api/jobs/:id", () => {
    it("sends 200 with one job", async () => {
        // Arrange
        const app = createApp(makeConfig(await storeWithJobs()));

        // Act
        const response = await request(app).get("/api/jobs/job-1");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body.jobId).toBe("job-1");
        expect(response.body.resultUrl).toBe("/results/job-1.csv");
    });

    it("sends 404 for a job that doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig(new FakeJobStore()));

        // Act
        const response = await request(app).get("/api/jobs/not-a-real-job");

        // Assert
        expect(response.status).toBe(404);
        expect(response.body).toEqual({ error: "Job ID not found" });
    });

    it("sends 500 when the job store fails", async () => {
        // Arrange
        const app = createApp(makeConfig(new BrokenJobStore()));

        // Act
        const response = await request(app).get("/api/jobs/job-1");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error fetching job" });
    });
});
