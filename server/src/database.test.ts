import { describe, it, expect, vi, afterEach } from "vitest";
import { createPool } from "./database.js";

describe("createPool", () => {
    // Put console.error back to normal after each test
    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("doesn't crash when a database connection drops", async () => {
        // Arrange: a pool with a pretend address (making a pool doesn't connect yet),
        // and console.error silenced so the test output stays clean
        const pool = createPool("postgresql://user:password@localhost:5432/test");
        const logged = vi.spyOn(console, "error").mockImplementation(() => {});

        // Act + Assert: without the listener, emitting "error" would throw
        expect(() => pool.emit("error", new Error("connection dropped"))).not.toThrow();
        expect(logged).toHaveBeenCalledWith("Database connection error:", "connection dropped");

        await pool.end();
    });
});
