import { describe, it, expect } from "vitest";
import type { Pool } from "pg";
import { NeonJobStore } from "./neonJobStore.js";

// A pretend pg Pool. It remembers the SQL it was sent and answers with the rows we give it,
// so we can test NeonJobStore without the real Neon database.
class FakePool {
    queries: { sql: string; values: unknown[] | undefined }[] = [];
    rows: object[] = [];

    async query(sql: string, values?: unknown[]): Promise<{ rows: object[] }> {
        this.queries.push({ sql, values });
        return { rows: this.rows };
    }
}

// A pool whose database is unreachable, like when Neon is down: every query fails
class BrokenPool extends FakePool {
    override async query(): Promise<{ rows: object[] }> {
        throw new Error("database is down");
    }
}

// Our FakePool only has query(), which is all NeonJobStore uses, so we tell TypeScript to treat it as a Pool
function storeWith(pool: FakePool): NeonJobStore {
    return new NeonJobStore(pool as unknown as Pool);
}

const createdAt = new Date("2026-11-19T18:24:00.000Z");

// One row like the jobs table sends back
const doneRow = {
    id: "123e4567-e89b-12d3-a456-426614174000",
    filename: "intro.mp4",
    target_color: "ff0000",
    threshold: 120,
    status: "done",
    result: "/results/123e4567-e89b-12d3-a456-426614174000.csv",
    error: null,
    created_at: createdAt,
};

describe("NeonJobStore", () => {
    it("lists jobs newest first and turns rows into camelCase jobs", async () => {
        // Arrange
        const pool = new FakePool();
        pool.rows = [doneRow];
        const store = storeWith(pool);

        // Act
        const jobs = await store.listJobs();

        // Assert
        expect(pool.queries[0]?.sql).toContain("ORDER BY created_at DESC");
        expect(jobs).toEqual([
            {
                id: doneRow.id,
                filename: "intro.mp4",
                targetColor: "ff0000",
                threshold: 120,
                status: "done",
                result: doneRow.result,
                error: null,
                createdAt,
            },
        ]);
    });

    it("finds a job by id, including when it was created", async () => {
        // Arrange
        const pool = new FakePool();
        pool.rows = [doneRow];
        const store = storeWith(pool);

        // Act
        const job = await store.findJob(doneRow.id);

        // Assert
        expect(pool.queries[0]?.values).toEqual([doneRow.id]);
        expect(job?.createdAt).toEqual(createdAt);
        expect(job?.targetColor).toBe("ff0000");
    });

    it("returns undefined for an id that isn't a UUID, without asking the database", async () => {
        // Arrange
        const pool = new FakePool();
        const store = storeWith(pool);

        // Act
        const job = await store.findJob("abc");

        // Assert
        expect(job).toBeUndefined();
        expect(pool.queries).toEqual([]);
    });

    it("createJob inserts a new job with status 'processing'", async () => {
        // Arrange
        const pool = new FakePool();
        const store = storeWith(pool);

        // Act
        await store.createJob(doneRow.id, "intro.mp4", "ff0000", 120);

        // Assert
        expect(pool.queries).toHaveLength(1);
        expect(pool.queries[0]?.sql).toContain("INSERT INTO jobs");
        expect(pool.queries[0]?.sql).toContain("'processing'");
        expect(pool.queries[0]?.values).toEqual([doneRow.id, "intro.mp4", "ff0000", 120]);
    });

    it("markDone sets the status to 'done' and saves the result path", async () => {
        // Arrange
        const pool = new FakePool();
        const store = storeWith(pool);

        // Act
        await store.markDone(doneRow.id, doneRow.result);

        // Assert
        expect(pool.queries).toHaveLength(1);
        expect(pool.queries[0]?.sql).toContain("UPDATE jobs");
        expect(pool.queries[0]?.sql).toContain("status = 'done'");
        expect(pool.queries[0]?.values).toEqual([doneRow.id, doneRow.result]);
    });

    it("markError sets the status to 'error' and saves the message", async () => {
        // Arrange
        const pool = new FakePool();
        const store = storeWith(pool);

        // Act
        await store.markError(doneRow.id, "Processor exited with code 1");

        // Assert
        expect(pool.queries).toHaveLength(1);
        expect(pool.queries[0]?.sql).toContain("UPDATE jobs");
        expect(pool.queries[0]?.sql).toContain("status = 'error'");
        expect(pool.queries[0]?.values).toEqual([doneRow.id, "Processor exited with code 1"]);
    });

    it("markError sends the message as a value, never inside the SQL (no SQL injection)", async () => {
        // Arrange
        const pool = new FakePool();
        const store = storeWith(pool);
        const sneakyMessage = "can't read video'; DROP TABLE jobs; --";

        // Act
        await store.markError(doneRow.id, sneakyMessage);

        // Assert: the SQL is exactly the fixed text, so the message can only go in through the $2 placeholder
        expect(pool.queries[0]?.sql).toBe("UPDATE jobs SET status = 'error', error = $2 WHERE id = $1");
        expect(pool.queries[0]?.values).toEqual([doneRow.id, sneakyMessage]);
    });

    it("createJob passes a database error up instead of hiding it", async () => {
        // Arrange
        const store = storeWith(new BrokenPool());

        // Act + Assert
        await expect(store.createJob(doneRow.id, "intro.mp4", "ff0000", 120)).rejects.toThrow("database is down");
    });

    it("markDone passes a database error up instead of hiding it", async () => {
        // Arrange
        const store = storeWith(new BrokenPool());

        // Act + Assert
        await expect(store.markDone(doneRow.id, doneRow.result)).rejects.toThrow("database is down");
    });

    it("markError passes a database error up instead of hiding it", async () => {
        // Arrange
        const store = storeWith(new BrokenPool());

        // Act + Assert
        await expect(store.markError(doneRow.id, "Processor exited with code 1")).rejects.toThrow("database is down");
    });
});
