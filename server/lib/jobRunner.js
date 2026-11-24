import { spawn as nodeSpawn } from 'child_process';
import { access } from 'fs/promises';
import { updateJob } from './jobStore.js';

/**
 * Spawns the Java processor JAR as a background process and updates the job
 * record when it finishes or fails.
 *
 * Separated from routes/process.js so the spawn logic can be unit-tested
 * without HTTP infrastructure, and so the route stays focused on validation
 * and responding.
 *
 * @param {object}   opts
 * @param {string}   opts.jobId      - UUID for the job (used to update the store)
 * @param {string}   opts.jarPath    - absolute path to videoprocessor.jar
 * @param {string}   opts.videoPath  - absolute path to the input video file
 * @param {string}   opts.outputPath - absolute path where the JAR should write the CSV
 * @param {string}   opts.targetColor - 6-char hex color (RRGGBB)
 * @param {string}   opts.threshold  - integer threshold as a string
 * @param {Function} [opts.spawnFn]  - injectable spawn function; defaults to child_process.spawn.
 *                                     Accepts the same arguments as spawn and must return a
 *                                     ChildProcess-like object with stderr, on('error'), on('exit')
 *                                     and unref(). Pass a fake here in tests to avoid needing Java.
 * @returns {void} Returns as soon as the child is launched; the job record is updated later by the
 *                 child's 'error' and 'exit' handlers.
 * @throws If `spawnFn` throws synchronously. This is deliberately NOT async: the route calls
 *         runJob inside its try/catch and relies on a synchronous failure turning into a 500
 *         "Error starting job", exactly as it did when the spawn code lived in the route.
 *         (An async function would turn the same failure into an unhandled promise rejection.)
 */
export function runJob({
  jobId,
  jarPath,
  videoPath,
  outputPath,
  targetColor,
  threshold,
  spawnFn = nodeSpawn,
}) {
  let stderrOutput = '';
  const child = spawnFn(
    'java',
    ['-jar', jarPath, videoPath, outputPath, targetColor, threshold],
    { detached: true, stdio: ['ignore', 'ignore', 'pipe'] }
  );

  child.stderr.on('data', (chunk) => { stderrOutput += chunk.toString(); });

  child.on('error', async (err) => {
    // spawn itself failed — Java not on PATH, JAR missing, etc.
    await updateJob(jobId, { status: 'error', error: `Error processing video: ${err.message}` })
      .catch(console.error);
  });

  child.on('exit', async (code) => {
    try {
      // A job is done only when the JAR exits 0 AND the CSV was written.
      // B-E1 makes the exit codes trustworthy, so checking both is correct:
      // a zero exit with no CSV means the JAR succeeded but the output is missing.
      let hasCsv = true;
      await access(outputPath).catch(() => { hasCsv = false; });

      if (code === 0 && hasCsv) {
        await updateJob(jobId, { status: 'done', result: `/results/${jobId}.csv` });
      } else {
        const detail = stderrOutput.trim() || 'the processor did not produce a result';
        await updateJob(jobId, { status: 'error', error: `Error processing video: ${detail}` });
      }
    } catch (err) {
      console.error('Could not update job', jobId, err);
    }
  });

  // Detach so the child outlives the Node process if it restarts.
  child.unref();
}
