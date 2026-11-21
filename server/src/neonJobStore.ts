import type { Pool } from "pg";
import { validate } from "uuid";
import type { Job, JobStatus, JobStore } from "./jobStore.js";

// What one row from the jobs table looks like.
// The database uses snake_case names, like target_color.
type JobRow = {
    id: string;
    filename: string;
    target_color: string;
    threshold: number;
    status: JobStatus;
    result: string | null;
    error: string | null;
    created_at: Date;   // pg turns TIMESTAMPTZ into a JavaScript Date
};

// The columns we read, in the same order as JobRow
const JOB_COLUMNS = "id, filename, target_color, threshold, status, result, error, created_at";

// turn the database's snake_case row into our camelCase Job
function rowToJob(row: JobRow): Job {
    return {
        id: row.id,
        filename: row.filename,
        targetColor: row.target_color,
        threshold: row.threshold,
        status: row.status,
        result: row.result,
        error: row.error,
        createdAt: row.created_at,
    };
}
// The real job store. It saves jobs in our Neon database using pg.
export class NeonJobStore implements JobStore {
    private pool: Pool;

    // The pool is made once (from DATABASE_URL) and passed in here
    constructor(pool: Pool) {
        this.pool = pool;
    }

    async createJob(id: string, filename: string, targetColor: string, threshold: number): Promise<void> {
        // $1, $2... are placeholders. pg fills them in safely (no SQL injection)
        await this.pool.query(
            "INSERT INTO jobs (id, filename, target_color, threshold, status) VALUES ($1, $2, $3, $4, 'processing')",
            [id, filename, targetColor, threshold]
        );
    }

    async markDone(id: string, resultPath: string): Promise<void> {
        await this.pool.query(
            "UPDATE jobs SET status = 'done', result = $2 WHERE id = $1",
            [id, resultPath]
        );
    }

    async markError(id: string, message: string): Promise<void> {
        await this.pool.query(
            "UPDATE jobs SET status = 'error', error = $2 WHERE id = $1",
            [id, message]
        );
    }

    async findJob(id: string): Promise<Job | undefined> {
        // The id column is a UUID, so the database throws an error for something like "abc".
        // A made-up id should be a 404, not a 500, so we stop here instead.
        if (!validate(id)) {
            return undefined;
        }

        const result = await this.pool.query<JobRow>(
            `SELECT ${JOB_COLUMNS} FROM jobs WHERE id = $1`,
            [id]
        );

        // rows is empty if there's no job with this id
        const row = result.rows[0];
        if (!row) {
            return undefined;
        }

        return rowToJob(row);
    }

    async listJobs(): Promise<Job[]> {
        // DESC = newest first
        const result = await this.pool.query<JobRow>(`SELECT ${JOB_COLUMNS} FROM jobs ORDER BY created_at DESC`);
        return result.rows.map(rowToJob);
    }
}
