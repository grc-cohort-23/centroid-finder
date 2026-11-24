import path from "path";
import { describe, it, expect } from "vitest";
import request from "supertest";
import { createApp, type AppConfig } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";
import { parseSeconds } from "./frameRoute.js";

// The real sample video (11 seconds long), so the tests can use real ffmpeg
const sampleVideos = path.join(import.meta.dirname, "..", "..", "processor", "sampleInput");

function makeConfig(): AppConfig {
    return {
        videoDir: sampleVideos,
        resultsDir: "/results-folder",
        jarPath: "/jars/videoprocessor.jar",
        store: new FakeJobStore(),
        run: () => {},
    };
}

// Every PNG file starts with these 8 bytes
const PNG_SIGNATURE = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);

describe("parseSeconds", () => {
    it("reads whole and decimal seconds", () => {
        expect(parseSeconds("0")).toBe(0);
        expect(parseSeconds("2.5")).toBe(2.5);
    });

    it("refuses missing, negative, and non-number values", () => {
        expect(parseSeconds(undefined)).toBeUndefined();
        expect(parseSeconds("")).toBeUndefined();
        expect(parseSeconds("-1")).toBeUndefined();
        expect(parseSeconds("abc")).toBeUndefined();
        expect(parseSeconds("Infinity")).toBeUndefined();
        expect(parseSeconds(["1", "2"])).toBeUndefined();
    });
});

describe("GET /frame/:filename", () => {
    it("sends 200 with a PNG of the frame at that time", async () => {
        // Arrange
        const app = createApp(makeConfig());

        // Act
        const response = await request(app).get("/frame/bright_balloon_sample_video.mp4?t=2.5");

        // Assert
        expect(response.status).toBe(200);
        expect(response.headers["content-type"]).toBe("image/png");
        expect(Buffer.from(response.body).subarray(0, 8)).toEqual(PNG_SIGNATURE);
    });

    it("sends different pictures for different times", async () => {
        // Arrange
        const app = createApp(makeConfig());

        // Act
        const start = await request(app).get("/frame/bright_balloon_sample_video.mp4?t=0");
        const later = await request(app).get("/frame/bright_balloon_sample_video.mp4?t=8");

        // Assert
        expect(Buffer.compare(start.body, later.body)).not.toBe(0);
    });

    it("sends 400 when t is missing or negative", async () => {
        // Arrange
        const app = createApp(makeConfig());

        // Act
        const missing = await request(app).get("/frame/bright_balloon_sample_video.mp4");
        const negative = await request(app).get("/frame/bright_balloon_sample_video.mp4?t=-3");

        // Assert
        expect(missing.status).toBe(400);
        expect(negative.status).toBe(400);
        expect(negative.body).toEqual({ error: "t must be a number of seconds, 0 or more" });
    });

    it("sends 404 when t is after the end of the video", async () => {
        // Arrange
        const app = createApp(makeConfig());

        // Act
        const response = await request(app).get("/frame/bright_balloon_sample_video.mp4?t=999");

        // Assert
        expect(response.status).toBe(404);
        expect(response.body).toEqual({ error: "No frame at that time" });
    });

    it("sends 500 when the video doesn't exist", async () => {
        // Arrange
        const app = createApp(makeConfig());

        // Act
        const response = await request(app).get("/frame/nope.mp4?t=1");

        // Assert
        expect(response.status).toBe(500);
        expect(response.body).toEqual({ error: "Error extracting frame" });
    });

    it("can't reach files outside the videos folder", async () => {
        // Arrange: %2F is "/", so this asks for ../../processor/sampleInput/... the long way round
        const app = createApp(makeConfig());

        // Act
        const response = await request(app).get("/frame/..%2F..%2Fpom.xml?t=0");

        // Assert: basename turned it into "pom.xml" inside the videos folder, which isn't there
        expect(response.status).toBe(500);
    });
});
