// The parts of the server that do the actual work.
const fs = require("fs/promises");
const path = require("path");
const { spawn } = require("child_process");

// ============================================================
// JobStore: each job is one JSON file, jobs/<jobId>.json
// ============================================================
class JobStore {
  constructor(jobsDir) {
    this.jobsDir = jobsDir;
  }

  fileFor(jobId) {
    return path.join(this.jobsDir, `${jobId}.json`);
  }

  async save(jobId, job) {
    await fs.writeFile(this.fileFor(jobId), JSON.stringify(job, null, 2));
  }

  // Returns the job, or null if there is no job with that ID
  async find(jobId) {
    try {
      return JSON.parse(await fs.readFile(this.fileFor(jobId), "utf8"));
    } catch (err) {
      if (err.code === "ENOENT") return null;
      throw err;
    }
  }

  // Changes only some fields of a saved job (for example, its status)
  async update(jobId, changes) {
    const job = await this.find(jobId);
    await this.save(jobId, { ...job, ...changes });
  }
}

// ============================================================
// Processor runner: the only place that knows how to run the JAR
// ============================================================
/**
 * Runs: java -jar <jarPath> <videoPath> <resultPath> <targetColor> <threshold>
 * Returns right away; calls onFinish(error) later, when the JAR is done.
 * error is null if the JAR succeeded.
 */
function createProcessorRunner(jarPath) {
  return function runProcessor({ videoPath, resultPath, targetColor, threshold }, onFinish) {
    const child = spawn("java", ["-jar", jarPath, videoPath, resultPath, targetColor, String(threshold)], {
      detached: true,  // runs on its own, separate from the server
      stdio: "ignore", // don't connect its input/output to the server
    });

    // "exit" and "error" can both fire; only report once
    let finished = false;
    const finish = (error) => {
      if (finished) return;
      finished = true;
      onFinish(error);
    };

    child.on("exit", (code) => {
      finish(code === 0 ? null : new Error(`processor exited with code ${code}`));
    });
    // java couldn't start at all
    child.on("error", (err) => finish(err)); 

    // don't make the server wait for the JAR
    child.unref(); 
  };
}

// ============================================================
// Thumbnail: first frame of a video as a JPEG, using ffmpeg
// ============================================================
/** Resolves with the JPEG bytes, or rejects if ffmpeg fails. */
function makeThumbnail(videoPath) {
  return new Promise((resolve, reject) => {
    // 1 frame, as a JPEG, written to ffmpeg's output (pipe:1) instead of a file
    const ffmpeg = spawn("ffmpeg", [
      "-loglevel", "error",
      "-i", videoPath,
      "-frames:v", "1",
      "-f", "image2pipe",
      "-vcodec", "mjpeg",
      "pipe:1",
    ]);

    const chunks = [];
    ffmpeg.stdout.on("data", (chunk) => chunks.push(chunk));
    ffmpeg.on("error", reject); // ffmpeg isn't installed
    ffmpeg.on("close", (code) => {
      if (code === 0 && chunks.length > 0) {
        resolve(Buffer.concat(chunks));
      } else {
        reject(new Error(`ffmpeg exited with code ${code}`));
      }
    });
  });
}

module.exports = { JobStore, createProcessorRunner, makeThumbnail };