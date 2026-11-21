import fs from "fs";
import os from "os";
import path from "path";
import { describe, it, expect, beforeAll, afterAll } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";

// The real sample video, so the thumbnail test can use real ffmpeg
const sampleVideos = path.join(import.meta.dirname, "..", "..", "processor", "sampleInput");

// Settings for a test app that uses the given videos folder
function makeConfig(videoDir: string): AppConfig {
    return {
        videoDir,
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store: new FakeJobStore(),
        run: () => {},
    };
}

describe("GET /api/videos", () => {
    let testFolder: string;

    // Make a temporary folder with 2 videos and 1 file that isn't a video
    beforeAll(() => {
        testFolder = fs.mkdtempSync(path.join(os.tmpdir(), "videos-"));
        fs.writeFileSync(path.join(testFolder, "intro.mp4"), "");
        fs.writeFileSync(path.join(testFolder, "demo.MOV"), "");
        fs.writeFileSync(path.join(testFolder, "notes.txt"), "");
    });

    // Delete the temporary folder when the tests are done
    afterAll(() => {
        fs.rmSync(testFolder, { recursive: true });
    });

    it("sends 200 with only the video file names", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app).get("/api/videos");

        // Assert (sorted, because the folder can list files in any order)
        expect(response.status).toBe(200);
        expect(response.body.sort()).toEqual(["demo.MOV", "intro.mp4"]);
    });

    it("sends 500 when the videos folder doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig("/no/such/folder"));

        // Act
        const response = await request(app).get("/api/videos");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error reading video directory" });
    });
});

describe("GET /thumbnail/:filename", () => {
    it("sends 200 with a JPEG of the first frame", async () => {
        // Arrange
        const app = createApp(makeConfig(sampleVideos));

        // Act
        const response = await request(app).get("/thumbnail/bright_balloon_sample_video.mp4");

        // Assert
        expect(response.status).toBe(200);
        expect(response.headers["content-type"]).toBe("image/jpeg");
        expect(response.body.length).toBeGreaterThan(0);
    });

    it("sends 500 when the video doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig(sampleVideos));

        // Act
        const response = await request(app).get("/thumbnail/nope.mp4");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error generating thumbnail" });
    });
});
