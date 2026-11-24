import fs from "fs";
import path from "path";
import { describe, it, expect } from "vitest";
import request from "supertest";
import { createApp } from "./app.js";
import { FakeJobStore } from "./fakeJobStore.js";

describe("GET /api/health", () => {
    it("sends 200 with ok and the version from package.json", async () => {
        // Arrange
        const packageJson = JSON.parse(fs.readFileSync(path.join(import.meta.dirname, "..", "package.json"), "utf8"));
        const app = createApp({
            videoDir: "/videos",
            resultsDir: "/results-folder",
            jarPath: "/jars/videoprocessor.jar",
            store: new FakeJobStore(),
            run: () => {},
        });

        // Act
        const response = await request(app).get("/api/health");

        // Assert
        expect(response.status).toBe(200);
        expect(response.body).toEqual({ ok: true, version: packageJson.version });
    });
});
