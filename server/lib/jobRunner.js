import { access } from 'fs/promises';
import { updateJob } from './jobStore.js';

/**
 * Spawns the Java processor as a detached background process and updates the
 * job record when it finishes.
 *
 * The function returns as soon as the child process is launched — it does not
 * wait for the JAR to finish. Job state is written to disk via `updateJob`
 * inside the `exit` and `error` event handlers.
 *
 * The optional `spawnFn` parameter exists solely for unit testing: tests pass
 * in an EventEmitter-based fake child process so the suite never needs to
 * invoke Java. Production callers omit it and get the real `child_process.spawn`.
 *
 * @param {object}    opts
 * @param {string}    opts.jobId       - UUID of the job created by the route handler
 * @param {string}    opts.jarPath     - Absolute path to videoprocessor.jar
 * @param {string}    opts.videoPath   - Absolute path to the input video file
 * @param {string}    opts.outputPath  - Absolute path where the JAR should write its CSV
 * @param {string}    opts.targetColor - 6-digit hex color string (e.g. "FFA200")
 * @param {string}    opts.threshold   - Numeric threshold as a string
 * @param {Function}  [opts.spawnFn]   - Dependency-injected spawn for testing; defaults
 *                                       to Node's built-in `child_process.spawn`
 * @returns {Promise<void>} Resolves once the child process has been launched and unref'd
 */
export async function runJob({ jobId, jarPath, videoPath, outputPath, targetColor, threshold, spawnFn }) {
  const spawn = spawnFn ?? (await import('child_process')).spawn;
  let stderrOutput = '';
  const child = spawn('java', ['-jar', jarPath, videoPath, outputPath, targetColor, threshold],
    { detached: true, stdio: ['ignore', 'ignore', 'pipe'] });

  child.stderr.on('data', (chunk) => { stderrOutput += chunk.toString(); });

  child.on('error', async (err) => {
    await updateJob(jobId, { status: 'error', error: `Error processing video: ${err.message}` }).catch(console.error);
  });

  child.on('exit', async (code) => {
    try {
      let hasCsv = true;
      await access(outputPath).catch(() => { hasCsv = false; });
      if (code === 0 && hasCsv) {
        await updateJob(jobId, { status: 'done', result: `/results/${jobId}.csv` });
      } else {
        const detail = stderrOutput.trim() || 'the processor did not produce a result';
        await updateJob(jobId, { status: 'error', error: `Error processing video: ${detail}` });
      }
    } catch (err) { console.error('Could not update job', jobId, err); }
  });

  child.unref();
}
