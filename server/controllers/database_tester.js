// This file is designed to test the database.

import { v4 as generateUUID } from 'uuid';
import { addJob, clearJobs, findAllJobs, findJob, updateJobStatus } from "../services/db.service.js";

/**
 * Returns all the jobs in the database. 
 * 
 * 200 - Success
 * 500 - Failure
 */
export function getDatabase(req, res) {
    try {
        const allJobs = findAllJobs();
        res.status(200).json({ count: allJobs.length, jobs: allJobs });
    } catch (e) {
        console.error(e);
        res.status(500).send({ error: "Database error" });
    }
}

/**
 * Adds a job with the specific id to the database.
 * 201 - Success
 * 500 - Failure
 */
export function addToDatabase(req, res) {
    const id = req.query.id;
    const jobMetadata = {
        id,
        threshold: 1, 
        targetColor: "FFFFFF",
        inputPath: "tmp/path"
    }

    try {
        addJob(jobMetadata)
        return res.status(201).send({ jobId: jobMetadata.id });
    } catch (e) {
        console.error(e);
        return res.status(500).send({ error: "Database error" });
    }
}

/**
 * Updates a job with a new status
 * 400 - jobId or status was not provided in query
 * 404 - job does not exist
 * 201 - updated job with new status
 * 
 * There is NO check on whether the status is valid.
 */
export function updateDatabase(req, res) {
    const jobId = req.query.jobId;
    const newStatus = req.query.status;
    if (jobId == null || newStatus == null) return res.status(400).send({ message: "Missing jobId or new status"})

    try {
        if (findJob(jobId) === undefined) return res.status(404).send({ message: "Job not found" });
        updateJobStatus(jobId, newStatus);
        return res.status(201).send({ message: `Updated ${jobId}`, newStatus });
    } catch (e) {
        console.error(`Database Error: ${e}`);
    }

    return res.status(500);
}

/**
 * Resets the database.
 * 200 - Success
 * 500 - Failure
 */
export function resetDatabase(req, res) {
    try {
        clearJobs()
        return res.status(200).send({ message: "Database reset" });
    } catch (e) {
        console.error(e);
        return res.status(500).send({ error: "Database error" });
    }
}