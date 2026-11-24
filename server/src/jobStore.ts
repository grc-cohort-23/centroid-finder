// The only 3 statuses a job can have (the same as the CHECK in schema.sql)
export type JobStatus = "processing" | "done" | "error";

// One job, like one row in the jobs table
export type Job = {
    id: string;
    filename: string;
    targetColor: string;
    threshold: number;
    status: JobStatus;
    result: string | null;   // path to the CSV, null until the job is done
    error: string | null;    // error message, null unless the job failed
    createdAt: Date;         // when the job was started (created_at in the table)
};

// Everything a job store can do.
// NeonJobStore uses the real database, FakeJobStore (for tests) uses a Map.
export interface JobStore {
    createJob(id: string, filename: string, targetColor: string, threshold: number): Promise<void>;
    markDone(id: string, resultPath: string): Promise<void>;
    markError(id: string, message: string): Promise<void>;
    findJob(id: string): Promise<Job | undefined>;
    // every job, newest first
    listJobs(): Promise<Job[]>;
}
