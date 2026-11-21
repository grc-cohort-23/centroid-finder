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
}

describe("GET /process/:jobId/status", () => {
    it("sends 200 with processing for a job that is still running", async () => {
        // Arrange
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).get("/process/job-1/status");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual({ status: "processing" });
    });

    it("sends 200 with done and the result path for a finished job", async () => {
        // Arrange
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);
        await store.markDone("job-1", "/results/job-1.csv");
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).get("/process/job-1/status");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual({ status: "done", result: "/results/job-1.csv" });
    });

    it("sends 200 with error and the message for a failed job", async () => {
        // Arrange
        const store = new FakeJobStore();
        await store.createJob("job-1", "intro.mp4", "ff0000", 120);
        await store.markError("job-1", "Error processing video: the JAR exited with code 1");
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).get("/process/job-1/status");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual({
            status: "error",
            error: "Error processing video: the JAR exited with code 1",
        });
    });

    it("sends 404 for a job that doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig(new FakeJobStore()));

        // Act
        const response = await request(app).get("/process/not-a-real-job/status");

        // Assert
        expect(response.status).toBe(404);
        expect(response.body).toEqual({ error: "Job ID not found" });
    });

    it("sends 500 when the job store fails", async () => {
        // Arrange
        const app = createApp(makeConfig(new BrokenJobStore()));

        // Act
        const response = await request(app).get("/process/job-1/status");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error fetching job status" });
    });
});
