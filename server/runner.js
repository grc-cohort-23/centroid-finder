const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const config = require('./config');
const jobs = require('./jobs');

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

  let errorOutput = '';
  java.stderr.on('data', (chunk) => { errorOutput += chunk; });

  // 'error' fires if Java couldn't be started (e.g. java isn't installed).
  java.on('error', (err) => markError(err.message));

  java.on('close', (code) => {
    // The processor exits non-zero on failure. The CSV check guards against
    // a run that exited cleanly but never wrote its output.
    if (code === 0 && fs.existsSync(csvPath)) {
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
