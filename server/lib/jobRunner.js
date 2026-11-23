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
 */
export async function runJob({ jobId, jarPath, videoPath, outputPath, targetColor, threshold, spawnFn }) {
  // Default to the real spawn so production callers don't have to pass it.
  const spawn = spawnFn ?? (await import('child_process')).spawn;

  let stderrOutput = '';
  const child = spawn(
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
