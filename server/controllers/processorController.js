import crypto from "crypto";
import fs from "fs/promises";
import { spawn } from "node:child_process";
import path from "node:path";
import { parseProcessingParams } from "../utils/processingParams.js";

/**
 * @description In-memory store for video processing jobs.
 *
 * Each job is identified by a unique UUID and contains information
 * about its processing status and generated CSV output. 
 *
 * 
 * Structure: (Typsescript would be nice here)
 * Map<jobId, {
 *     filename: string,
 *     status: "processing" | "completed" | "failed",
 *     outputCsv: string | null,
 * }>
 *
 * Jobs are added when processing begins and updated when the
 * Java process completes or fails.
 *
 * Note: This store is temporary. All job records are lost
 * when the Express server restarts.
 */
export const jobStore = new Map();

/**
 * @description handles the processing of a video file by calling a Java JAR and returning a job ID for tracking
 * 
 * @param {*} req 
 * @param {*} res 
 * @returns 
 */
export const processVideo = async (req, res) =>{

  const validation = parseProcessingParams({
        filename: req.params.filename,
        outputCsv: req.query.outputCsv,
        targetColor: req.query.targetColor,
        threshold: req.query.threshold
    });

    if (!validation.ok) {
        return res.status(400).json({
            success: false,
            message: validation.message
        });
    }

    const {
        videoFile,
        outputCsv,
        targetColor,
        threshold
    } = validation.params;


    const jobUUID = crypto.randomUUID();

    //debug logging
    console.log(`file provided: ${videoFile} | jar: ${process.env.JAR_PATH} | output: ${process.env.CSV_OUTPUT_DIR}/${outputCsv} | target color: ${targetColor} | threshold: ${threshold} | Job-ID: ${jobUUID} `);

    try {
        const valid = await getSingleVideo(process.env.VIDEO_DIR, videoFile);

        if(!valid){
            console.log(`Video not found:`, videoFile.trim());
            return res.status(500).json({success: false, message: `invalid video file check extensions or spelling`, videoFile});  
        }

    } catch (error) {
        if (error.code === "ENOTDIR") {
            console.error("Path is not a directory: ", process.env.VIDEO_DIR);
        } else if (error.code === "ENOENT") {
            console.error("Path does not exist: ", process.env.VIDEO_DIR);
        }else{
            console.log(error);
        }
        return res.status(500).json({success: false, message: `ERROR: Error reading video directory`});
    }

    const csvPath = path.resolve(process.env.CSV_OUTPUT_DIR ,  jobUUID + outputCsv);
    await fs.mkdir(process.env.CSV_OUTPUT_DIR, { recursive: true });

    //add a job process to the jobstore map
    jobStore.set(jobUUID, {
        filename: videoFile,
        status: "processing",
        outputCsv: null,
    });
    const job = jobStore.get(jobUUID);

    //start jar with a child process
    const child = spawn("java", [
        "-jar",
        process.env.JAR_PATH,
        process.env.VIDEO_DIR + "/" + videoFile,
        csvPath,
        targetColor,
        String(threshold)
    ]);
    

    // Java could not start or encountered a process error.
    child.on("error", (error) => {
        console.error(`Job ${jobUUID} error:`, error);

        job.status = "failed";
        job.error = error.message;
    });

    // Java has finished, and its output streams have closed.
    child.on("close", async (code) => {

        // Avoid overwriting an earlier process error.
        if (job.status === "failed") return;

        if (code !== 0) {
            job.status = "failed";
            job.error = `Java exited with code ${code}`;
            return;
        }

        // Verify Java JAR worked
        try {
            const stats = await fs.stat(csvPath);

            if (!stats.isFile()) {
                throw new Error("CSV output is not a file");
            }

            job.status = "completed";
            job.outputCsv =  jobUUID + outputCsv;

        } catch (error) {
            job.status = "failed";
            job.error = "Expected CSV output was not produced";
            console.error(error);
        }

        console.log(`Job ${jobUUID}: ${job.status}`);
    });

        return res.status(202).json({
        success: true,
        jobId: jobUUID,
        status: "processing",
        message: `successfully started JAR process! 
                    file provided: ${videoFile} | 
                    jar: ${process.env.JAR_PATH} | 
                    output: ${process.env.CSV_OUTPUT_DIR}/${outputCsv} | 
                    target color: ${targetColor} | threshold: ${threshold} |
                    Job-ID: ${jobUUID} `
        });
}

async function getSingleVideo(videoDir, videoFile) {

    const entries = await fs.readdir(videoDir, { withFileTypes: true });


    return entries.some((entry) => {
        return(
        // console.log(entry.name + " | " + videoFile),
        entry.name.trim() === videoFile);
    });
}



/**
 * @description handles the status of a video processing job by checking the job ID and returning the current status
 * @param {*} req 
 * @param {*} res 
 * @returns 
 */
export const status = (req, res) =>{

    if(!req.params.jobId){
        return res.status(400).json({success: false, message: "id not provided"});
    }
    const jobId = req.params.jobId;

    if(!jobStore.has(jobId)){
        return res.status(400).json({success: false, message: `jobStore does not contain a value for id: ${jobId}`});
    }

    const job = jobStore.get(jobId);
    
    switch (job.status) {
        case "processing":
            return res.status(202).json({
            success: true,
            jobId,
            status: "processing"
            });

        case "completed":
            return res.status(200).json({
            success: true,
            jobId,
            status: "completed",
            outputCsv: job.outputCsv,
            csvUrl: `/outputs/${encodeURIComponent(job.outputCsv)}`
            });

        case "failed":
            return res.status(200).json({
            success: false,
            jobId,
            status: "failed",
            error: job.error || "Video processing failed"
            });

        default:
            return res.status(500).json({
            success: false,
            message: "Unknown job status"
            });
    }
}
