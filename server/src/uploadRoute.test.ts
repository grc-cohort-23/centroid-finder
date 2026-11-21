import fs from "fs";
import os from "os";
import path from "path";
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";
import { safeFilename } from "./uploadRoute.js";

// The real sample video, so we can upload a real file
const sampleVideo = path.join(import.meta.dirname, "..", "..", "processor", "sampleInput", "bright_balloon_sample_video.mp4");

function makeConfig(videoDir: string, maxUploadBytes?: number): AppConfig {
    const config: AppConfig = {
        videoDir,
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store: new FakeJobStore(),
        run: () => {},
    };
    if (maxUploadBytes !== undefined) config.maxUploadBytes = maxUploadBytes;
    return config;
}

describe("safeFilename", () => {
    it("keeps a normal name", () => {
        expect(safeFilename("my video 1.mp4")).toBe("my video 1.mp4");
    });

    it("drops folders and odd characters", () => {
        expect(safeFilename("../../etc/evil.mp4")).toBe("evil.mp4");
        expect(safeFilename("C:\\Users\\me\\clip.mov")).toBe("clip.mov");
        expect(safeFilename("sala;mander$.mp4")).toBe("sala_mander_.mp4");
    });

    it("refuses empty and hidden names", () => {
        expect(safeFilename("")).toBeUndefined();
        expect(safeFilename(".mp4")).toBeUndefined();
    });
});

describe("POST /api/videos", () => {
    let testFolder: string;

    // A new empty videos folder for every test
    beforeEach(() => {
        testFolder = fs.mkdtempSync(path.join(os.tmpdir(), "uploads-"));
    });

    afterEach(() => {
        fs.rmSync(testFolder, { recursive: true });
    });

    it("sends 201 with the VideoInfo and saves the video", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act: attach() sends a multipart form, like a browser's file upload
        const response = await request(app)
            .post("/api/videos")
            .attach("video", sampleVideo, { filename: "balloon.mp4", contentType: "video/mp4" });

        // Assert
        expect(response.status).toBe(201);
        expect(response.body).toMatchObject({ filename: "balloon.mp4", status: "not-processed", width: 1920, height: 1080 });
        expect(fs.statSync(path.join(testFolder, "balloon.mp4")).size).toBe(fs.statSync(sampleVideo).size);
        // only the video is left, no temporary .part file
        expect(fs.readdirSync(testFolder)).toEqual(["balloon.mp4"]);
    });

    it("shows the uploaded video in GET /api/videos", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        await request(app).post("/api/videos").attach("video", Buffer.from("tiny"), { filename: "clip.mov", contentType: "video/quicktime" });
        const response = await request(app).get("/api/videos");

        // Assert
        expect(response.body).toEqual(["clip.mov"]);
    });

    it("sends 409 when a video with that name already exists", async () => {
        // Arrange
        fs.writeFileSync(path.join(testFolder, "balloon.mp4"), "the old video");
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("video", Buffer.from("new video"), { filename: "balloon.mp4", contentType: "video/mp4" });

        // Assert: the old video wasn't replaced
        expect(response.status).toBe(409);
        expect(response.body).toEqual({ error: "A video with that name already exists" });
        expect(fs.readFileSync(path.join(testFolder, "balloon.mp4"), "utf8")).toBe("the old video");
    });

    it("sends 415 for a file that isn't .mp4 or .mov", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("video", Buffer.from("hello"), { filename: "notes.txt", contentType: "text/plain" });

        // Assert: nothing was saved
        expect(response.status).toBe(415);
        expect(response.body).toEqual({ error: "Only .mp4 and .mov videos can be uploaded" });
        expect(fs.readdirSync(testFolder)).toEqual([]);
    });

    it("sends 415 when the name says .mp4 but the type doesn't", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("video", Buffer.from("hello"), { filename: "fake.mp4", contentType: "text/html" });

        // Assert
        expect(response.status).toBe(415);
    });

    it("sends 413 when the video is bigger than the limit, and keeps nothing", async () => {
        // Arrange: a 10-byte limit, so we don't need a 2 GB file
        const app = createApp(makeConfig(testFolder, 10));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("video", Buffer.alloc(100), { filename: "big.mp4", contentType: "video/mp4" });

        // Assert
        expect(response.status).toBe(413);
        expect(response.body.error).toContain("too big");
        expect(fs.readdirSync(testFolder)).toEqual([]);
    });

    it("sends 400 when no file is sent", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app).post("/api/videos").field("name", "no file here");

        // Assert
        expect(response.status).toBe(400);
        expect(response.body.error).toContain("No video sent");
    });

    it("sends 400 when the file is in the wrong form field", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("file", Buffer.from("hello"), { filename: "clip.mp4", contentType: "video/mp4" });

        // Assert
        expect(response.status).toBe(400);
    });

    it("saves a name with folders in it inside the videos folder only", async () => {
        // Arrange
        const app = createApp(makeConfig(testFolder));

        // Act
        const response = await request(app)
            .post("/api/videos")
            .attach("video", Buffer.from("hello"), { filename: "../../escape.mp4", contentType: "video/mp4" });

        // Assert
        expect(response.status).toBe(201);
        expect(response.body.filename).toBe("escape.mp4");
        expect(fs.readdirSync(testFolder)).toEqual(["escape.mp4"]);
    });
});
