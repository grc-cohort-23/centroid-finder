import fs from "fs";
import os from "os";
import path from "path";
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";

// A real temporary videos folder, made fresh for every test, so the route can check that a video really exists
let videoDir: string;

// Settings for a test app: the temp videos folder, Nadia's fake job store, and a runner that does nothing
function makeConfig(store: FakeJobStore): AppConfig {
    return {
        videoDir,
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store,
        run: () => {},
    };
}

describe("POST /process/:filename", () => {
    beforeEach(() => {
        videoDir = fs.mkdtempSync(path.join(os.tmpdir(), "process-videos-"));
        // empty files are enough, because the route only checks that they exist (the fake runner never reads them)
        fs.writeFileSync(path.join(videoDir, "intro.mp4"), "");
        fs.writeFileSync(path.join(videoDir, "secret.mp4"), "");
        fs.writeFileSync(path.join(videoDir, "notes.txt"), "");
        // a folder whose name looks like a video, to check the route wants a real file
        fs.mkdirSync(path.join(videoDir, "folder.mp4"));
    });

    afterEach(() => {
        fs.rmSync(videoDir, { recursive: true });
    });

    it("sends 202 with a jobId and saves the job as processing", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act: supertest sends a pretend request to our app (no real server needed)
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000&threshold=120");

        // Assert
        expect(response.status).toBe(202);
        const job = await store.findJob(response.body.jobId);
        expect(job?.status).toBe("processing");
    });

    it("sends 400 when targetColor is missing", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?threshold=120");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body).toEqual({ error: "Missing targetColor or threshold query parameter." });
    });

    it("sends 400 when threshold is missing", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body).toEqual({ error: "Missing targetColor or threshold query parameter." });
    });

    it("sends 500 when the job can't be started", async () => {
        // Arrange: a job store that always fails, like when Neon is down
        class BrokenJobStore extends FakeJobStore {
            async createJob(): Promise<void> {
                throw new Error("database is down");
            }
        }
        const app = createApp(makeConfig(new BrokenJobStore()));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000&threshold=120");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error starting job" });
    });

    it("only uses the file name, so ../ can't reach files outside the videos folder", async () => {
        // Arrange: a fake runner that remembers the arguments it was given
        const store = new FakeJobStore();
        let ranArgs: string[] = [];
        const config = makeConfig(store);
        config.run = (command: string, args: string[]) => {
            ranArgs = args;
        };
        const app = createApp(config);

        // Act: %2F is an encoded "/", so this filename is "../../secret.mp4"
        const response = await request(app).post("/process/..%2F..%2Fsecret.mp4?targetColor=ff0000&threshold=120");

        // Assert: the JAR was given a video inside the videos folder, and the job saved just the name
        expect(response.status).toBe(202);
        expect(ranArgs[2]).toBe(path.join(videoDir, "secret.mp4"));
        const job = await store.findJob(response.body.jobId);
        expect(job?.filename).toBe("secret.mp4");
    });

    it("sends 400 when targetColor isn't hex", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=red&threshold=120");

        // Assert: rejected before any job was made
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/targetColor/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when targetColor isn't exactly 6 digits", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff00&threshold=120");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/targetColor/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when threshold isn't a number", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000&threshold=abc");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/threshold/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when threshold is a decimal", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000&threshold=12.5");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/threshold/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when threshold is negative", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/intro.mp4?targetColor=ff0000&threshold=-5");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/threshold/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when the file isn't a video", async () => {
        // Arrange: notes.txt is really in the folder, but it isn't .mp4 or .mov
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/notes.txt?targetColor=ff0000&threshold=120");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/video/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when the video doesn't exist", async () => {
        // Arrange
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/missing.mp4?targetColor=ff0000&threshold=120");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/video/);
        expect(store.jobs.size).toBe(0);
    });

    it("sends 400 when the name is a folder, not a video file", async () => {
        // Arrange: folder.mp4 is a folder, so only an "is it a real file?" check can catch it
        const store = new FakeJobStore();
        const app = createApp(makeConfig(store));

        // Act
        const response = await request(app).post("/process/folder.mp4?targetColor=ff0000&threshold=120");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toMatch(/video/);
        expect(store.jobs.size).toBe(0);
    });
});
