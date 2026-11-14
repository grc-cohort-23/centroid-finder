const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const config = require('./config');
const jobs = require('./jobs');

// The Java app prints this line only when it finished successfully.
const SUCCESS_MESSAGE = 'Centroid tracking saved to';

// Starts the Java processor for a job and returns immediately.
// When Java exits, the job is marked 'done' or 'error'.
function runJob({ id, filename, targetColor, threshold }) {
  const videoPath = path.join(config.VIDEO_DIR, filename);
  const csvName = `${filename}.csv`;
  const csvPath = path.join(config.RESULTS_DIR, csvName);

  const markError = (message) => {
    console.error(`Job ${id} failed: ${message}`);
    jobs.updateJob(id, { status: 'error', error: `Error processing video: ${message}` });
  };

  if (!fs.existsSync(config.JAR_PATH)) {
    return markError(`processor jar not found at ${config.JAR_PATH}`);
  }

  // Remove an old result for this video so a stale CSV can't be mistaken for a new one.
  fs.mkdirSync(config.RESULTS_DIR, { recursive: true });
  fs.rmSync(csvPath, { force: true });

  const java = spawn('java', [
    '-jar', config.JAR_PATH,
    videoPath, csvPath, targetColor, String(threshold),
  ]);

  let output = '';
  let errorOutput = '';
  java.stdout.on('data', (chunk) => { output += chunk; });
  java.stderr.on('data', (chunk) => { errorOutput += chunk; });

  // 'error' fires if Java couldn't be started (e.g. java isn't installed).
  java.on('error', (err) => markError(err.message));

  java.on('close', (code) => {
    // Our Java app exits with code 0 even when it fails, so we also require
    // its success message and the CSV file before calling the job done.
    const succeeded = code === 0
      && output.includes(SUCCESS_MESSAGE)
      && fs.existsSync(csvPath);

    if (succeeded) {
      console.log(`Job ${id} done: ${csvName}`);
      jobs.updateJob(id, { status: 'done', result: `/results/${csvName}` });
    } else {
      markError(lastErrorLine(errorOutput) || `processor exited with code ${code}`);
    }
  });
}

// Java/ffmpeg print lots of noise to stderr; the most useful message is
// usually the last line that isn't a stack-trace line.
function lastErrorLine(text) {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith('at ') && !line.startsWith('[swscaler'))
    .pop();
}

module.exports = { runJob };
