import fs from "fs";
import os from "os";
import path from "path";
import { describe, it, expect, beforeAll, afterAll } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";
import type { Job } from "./jobStore.js";
import { videoStatus } from "./videoInfo.js";

// The real sample video, so we can check that ffprobe reads its size and length
const sampleVideo = path.join(import.meta.dirname, "..", "..", "processor", "sampleInput", "bright_balloon_sample_video.mp4");

// Settings for a test app that uses the given videos folder and job store
function makeConfig(videoDir: string, store: FakeJobStore): AppConfig {
    return {
        videoDir,
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store,
        run: () => {},
    };
}

// A job store that always fails, like when the database is down
class BrokenJobStore extends FakeJobStore {
    async listJobs(): Promise<Job[]> {
        throw new Error("database is down");
    }
}

describe("videoStatus", () => {
    it("uses the newest job for the video", async () => {
        // Arrange: listJobs gives the newest first, so the error job is the newest
        const store = new FakeJobStore();
        await store.createJob("old", "intro.mp4", "ff0000", 120);
        await store.markDone("old", "/results/old.csv");
        await store.createJob("new", "intro.mp4", "ff0000", 120);
        await store.markError("new", "Error processing video");

        // Act
        const status = videoStatus("intro.mp4", await store.listJobs());

        // Assert
        expect(status).toBe("error");
    });

    it("says not-processed for a video with no jobs", () => {
        // Arrange, Act, Assert
        expect(videoStatus("intro.mp4", [])).toBe("not-processed");
    });
});

describe("GET /api/videos/details", () => {
    let testFolder: string;

    // A temporary folder with 4 empty "videos", 1 real video, and a file that isn't a video
    beforeAll(() => {
        testFolder = fs.mkdtempSync(path.join(os.tmpdir(), "videos-"));
        for (const name of ["done.mp4", "running.mp4", "failed.mov", "new.mp4", "notes.txt"]) {
            fs.writeFileSync(path.join(testFolder, name), "");
        }
        fs.copyFileSync(sampleVideo, path.join(testFolder, "balloon.mp4"));
    });

    afterAll(() => {
        fs.rmSync(testFolder, { recursive: true });
    });

    it("sends 200 with each video's status from its newest job", async () => {
        // Arrange
        const store = new FakeJobStore();
        await store.createJob("job-1", "done.mp4", "ff0000", 120);
        await store.markDone("job-1", "/results/job-1.csv");
        await store.createJob("job-2", "running.mp4", "ff0000", 120);
        await store.createJob("job-3", "failed.mov", "ff0000", 120);
        await store.markError("job-3", "Error processing video");
        const app = createApp(makeConfig(testFolder, store));

        // Act
        const response = await request(app).get("/api/videos/details");

        // Assert (sorted, because the folder can list files in any order)
        expect(response.status).toBe(200);
        const statuses = Object.fromEntries(
            (response.body as { filename: string; status: string }[]).map((video) => [video.filename, video.status])
        );
        expect(statuses).toEqual({
            "balloon.mp4": "not-processed",
            "done.mp4": "tracked",
            "running.mp4": "processing",
            "failed.mov": "error",
            "new.mp4": "not-processed",
        });
    });

    it("adds the size and length for a real video, and leaves them out for a broken one", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder, new FakeJobStore()));

        // Act
        const response = await request(app).get("/api/videos/details");

        // Assert
        const videos = response.body as Record<string, unknown>[];
        const balloon = videos.find((video) => video.filename === "balloon.mp4");
        expect(balloon?.width).toBe(1920);
        expect(balloon?.height).toBe(1080);
        expect(balloon?.durationSeconds).toBeCloseTo(11, 0);
        const empty = videos.find((video) => video.filename === "new.mp4");
        expect(empty).toEqual({ filename: "new.mp4", status: "not-processed" });
    });

    it("sends 500 when the videos folder doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig("/no/such/folder", new FakeJobStore()));

        // Act
        const response = await request(app).get("/api/videos/details");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error reading video details" });
    });

    it("sends 500 when the job store fails", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder, new BrokenJobStore()));

        // Act
        const response = await request(app).get("/api/videos/details");

        // Assert
        expect(response.status).toBe(500);
    });

    it("doesn't change GET /api/videos, which still sends only names", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder, new FakeJobStore()));

        // Act
        const response = await request(app).get("/api/videos");

        // Assert
        expect(response.body.sort()).toEqual(["balloon.mp4", "done.mp4", "failed.mov", "new.mp4", "running.mp4"]);
    });
});
