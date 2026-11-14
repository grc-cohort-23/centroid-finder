import { findJob } from "../services/db.service.js";

export default function getJob(req, res) {
    const jobId = req.params.jobid;

    try {
        const {status, outputPath} = findJob(jobId);
        if (status === undefined) res.status(404).send({ error: "Error fetching job status" });
        else if (status === "processing") res.status(200).send({ status });
        else if (status === "done") res.status(200).send({ status, result: `/results/${jobId}.csv` })
        else if (status === "error") res.status(200).send({ status, error: "Error processing video: Unexpected ffmpeg error" });
    } catch (e) {
        res.status(500).send({ error: "Error fetching job status" });
    }
    return
}