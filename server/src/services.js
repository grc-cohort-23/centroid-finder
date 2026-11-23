// The parts of the server that do the actual work.
const fs = require("fs/promises");
const { openSync, closeSync } = require("fs");
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

  // Where the JAR's error output (stderr) for this job is saved
  logFileFor(jobId) {
    return path.join(this.jobsDir, `${jobId}.log`);
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

  /*
   * Run when the server starts. A job still marked "processing" was cut off
   * by the last shutdown: the server that was waiting for its JAR is gone,
   * so nothing would ever mark it done. Marks each one as an error with the
   * given message and returns their IDs.
   */
  async failUnfinished(errorMessage) {
    const files = await fs.readdir(this.jobsDir);
    const changed = [];

    for (const file of files) {
      if (path.extname(file) !== ".json") continue; // skip the .log files

      const jobId = path.basename(file, ".json");
      let job;
      try {
        job = await this.find(jobId);
      } catch (err) {
        // one broken file shouldn't stop the server from starting
        console.warn(`Skipping job file ${file}: ${err.message}`);
        continue;
      }

      if (job.status === "processing") {
        await this.update(jobId, { status: "error", error: errorMessage });
        changed.push(jobId);
      }
    }
    return changed;
  }
}

// ============================================================
// Processor runner: the only place that knows how to run the JAR
// ============================================================
/**
 * Runs: java -jar <jarPath> <videoPath> <resultPath> <targetColor> <threshold>
 * Everything the JAR prints to stderr is saved in logPath.
 * Returns right away; calls onFinish(error) later, when the JAR is done.
 * error is null if the JAR succeeded. If it failed, error.message is the
 * last line the JAR printed (its reason), or the exit code if it printed nothing.
 *
 * command is "java" in real use; tests pass a fake script instead.
 */
function createProcessorRunner(jarPath, command = "java") {
  return function runProcessor({ videoPath, resultPath, targetColor, threshold, logPath }, onFinish) {
    // "exit" and "error" can both fire; only report once
    let finished = false;
    const finish = (error) => {
      if (finished) return;
      finished = true;
      onFinish(error);
    };

    let logFile;
    let child;
    try {
      // The JAR writes its stderr straight into this file, so the reason
      // for a failure is kept even though the JAR runs on its own
      logFile = openSync(logPath, "w");
      child = spawn(command, ["-jar", jarPath, videoPath, resultPath, targetColor, String(threshold)], {
        detached: true,                          // runs on its own, separate from the server
        stdio: ["ignore", "ignore", logFile],    // no input, ignore output, errors -> log file
      });
    } catch (err) {
      // the log file couldn't be created
      finish(err);
      return;
    } finally {
      // the JAR has its own copy of the file now, so the server can close its copy
      if (logFile !== undefined) closeSync(logFile);
    }

    child.on("exit", async (code) => {
      if (code === 0) {
        finish(null);
        return;
      }
      const reason = await lastLineOf(logPath);
      finish(new Error(reason ? `${reason} (exit code ${code})` : `processor exited with code ${code}`));
    });
    // java couldn't start at all
    child.on("error", (err) => finish(err));

    // don't make the server wait for the JAR
    child.unref();
  };
}

// Returns the last non-blank line of a file, or null if there isn't one
async function lastLineOf(file) {
  let text;
  try {
    text = await fs.readFile(file, "utf8");
  } catch {
    // no log to read: the caller falls back to the exit code
    return null;
  }
  const lines = text.split("\n").map((line) => line.trim()).filter((line) => line !== "");
  return lines.length > 0 ? lines[lines.length - 1] : null;
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