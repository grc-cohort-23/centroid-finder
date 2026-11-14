import Database from 'better-sqlite3';

// Referenced to database file
const db = new Database("jobs.db");

// Ensure database is created.
const createStatement = db.prepare(`CREATE TABLE IF NOT EXISTS jobs (
        id TEXT PRIMARY KEY,
        status TEXT,
        threshold INT,
        target_color TEXT,
        input_path TEXT
    )`
);
createStatement.run();

// Prepare statements
const getStatusStatement = db.prepare("SELECT status FROM jobs WHERE id = ?");
const getJobStatement = db.prepare("SELECT * FROM jobs WHERE id = ?");
const selectAllStatement = db.prepare("SELECT * FROM jobs");
const addJobStatement = db.prepare("INSERT INTO jobs (id, threshold, target_color, input_path, status) VALUES (?, ?, ?, ?, ?)")
const clearStatement = db.prepare("DROP TABLE IF EXISTS jobs");
const updateStatusStatement = db.prepare("UPDATE jobs SET status = ? WHERE id = ?");

// Exported functions

/**
 * Finds the status of a job from its UUID.
 * Returns undefined if no job found.
 * 
 * @param id the UUID of the job. 
 * @returns the status of the job
 */
export function findJobStatus(id) {
    return getStatusStatement.get(id);
}

/**
 * Finds a job from its UUID.
 * Returns undefined if no job found.
 * 
 * @param id the UUID of the job. 
 * @returns the job metadata.
 */
export function findJob(id) {
    return getJobStatement.get(id);
}

/**
 * Returns an array of all jobs in the database 
 * 
 * @returns all jobs in db.
 */
export function findAllJobs() {
    return selectAllStatement.all();
}

/**
 * Adds a new job to the database from its UUID and parameters.
 * 
 * @param jobMetadata the metadata of the new job 
 * @returns 
 */
export function addJob({ id, threshold, targetColor, inputPath, status }) {
    return addJobStatement.run(id, threshold, targetColor, inputPath, status);
}

/**
 * Drops the jobs table and replaces it.
 */
export function clearJobs() {
    clearStatement.run();
    createStatement.run();
}

/**
 * Updates the given id's status
 * 
 * @param id  The id to update
 * @param status The status to update
 * @returns 
 */
export function updateJobStatus(id, status) {
    return updateStatusStatement.run(status, id);
}