const fs = require('fs');
const path = require('path');

// Fake Java so the tests don't run the real processor.
jest.mock('child_process', () => ({ spawn: jest.fn() }));
const { spawn } = require('child_process');
const { fakeProcess } = require('./fakeProcess');

const config = require('../config');
const jobs = require('../jobs');
const { runJob } = require('../runner');

let jobCount = 0;

// Creates a job in the database the way the POST route does, and returns it.
function newJob(filename = 'ensantina.mp4') {
  jobCount += 1;
  const job = { id: `runner-${jobCount}`, filename, targetColor: 'ff0000', threshold: 120 };
  jobs.createJob(job);
  return job;
}

// Starts the job against a fake Java process and returns that process.
function start(job) {
  const proc = fakeProcess();
  spawn.mockReturnValue(proc);
  runJob(job);
  return proc;
}

const csvPathFor = (filename) => path.join(config.RESULTS_DIR, `${filename}.csv`);

describe('runJob', () => {
  beforeEach(() => {
    spawn.mockReset();
    fs.writeFileSync(config.JAR_PATH, '');
  });

  test('runs the jar with the video, CSV path, color and threshold', () => {
    const job = newJob();
    start(job);

    expect(spawn).toHaveBeenCalledWith('java', [
      '-jar', config.JAR_PATH,
      path.join(config.VIDEO_DIR, 'ensantina.mp4'),
      csvPathFor('ensantina.mp4'),
      'ff0000',
      '120',
    ]);
  });

  test('names the result file after the video', () => {
    const job = newJob('my clip.mov');
    start(job);

    expect(spawn.mock.calls[0][1][3]).toBe(path.join(config.RESULTS_DIR, 'my clip.mov.csv'));
  });

  test('leaves the job processing until Java finishes', () => {
    const job = newJob();
    start(job);

    expect(jobs.getJob(job.id).status).toBe('processing');
  });

  test('marks the job done when Java succeeds and writes the CSV', () => {
    const job = newJob();
    const java = start(job);

    fs.writeFileSync(csvPathFor(job.filename), 'frame,x,y\n');
    java.emit('close', 0);

    expect(jobs.getJob(job.id)).toMatchObject({
      status: 'done',
      result: '/results/ensantina.mp4.csv',
      error: null,
    });
  });

  test('removes an old CSV so it cannot be mistaken for a new result', () => {
    const job = newJob();
    fs.writeFileSync(csvPathFor(job.filename), 'stale results\n');
    const java = start(job);

    expect(fs.existsSync(csvPathFor(job.filename))).toBe(false);

    // Java exits cleanly but never wrote a new CSV.
    java.emit('close', 0);

    expect(jobs.getJob(job.id).status).toBe('error');
  });

  test('marks an error when Java exits non-zero, even if a CSV was written', () => {
    const job = newJob();
    const java = start(job);

    fs.writeFileSync(csvPathFor(job.filename), 'frame,x,y\n');
    java.emit('close', 1);

    expect(jobs.getJob(job.id)).toMatchObject({
      status: 'error',
      error: 'Error processing video: processor exited with code 1',
    });
  });

  test('uses the last real line of Java\'s error output as the message', () => {
    const job = newJob();
    const java = start(job);

    java.stderr.emit('data', Buffer.from([
      '[swscaler @ 0x1] deprecated pixel format',
      'java.io.IOException: Could not open video',
      '    at JavaCvFrameSource.open(JavaCvFrameSource.java:20)',
      '    at VideoSummaryApp.main(VideoSummaryApp.java:12)',
      '',
    ].join('\n')));
    java.emit('close', 1);

    expect(jobs.getJob(job.id)).toMatchObject({
      status: 'error',
      error: 'Error processing video: java.io.IOException: Could not open video',
    });
  });

  test('falls back to the exit code when Java prints no error', () => {
    const job = newJob();
    const java = start(job);

    java.emit('close', 2);

    expect(jobs.getJob(job.id).error).toBe('Error processing video: processor exited with code 2');
  });

  test('marks an error when Java cannot be started', () => {
    const job = newJob();
    const java = start(job);

    java.emit('error', new Error('spawn java ENOENT'));

    expect(jobs.getJob(job.id)).toMatchObject({
      status: 'error',
      error: 'Error processing video: spawn java ENOENT',
    });
  });

  test('marks an error without starting Java when the jar is missing', () => {
    fs.rmSync(config.JAR_PATH);
    const job = newJob();

    runJob(job);

    expect(spawn).not.toHaveBeenCalled();
    expect(jobs.getJob(job.id).status).toBe('error');
    expect(jobs.getJob(job.id).error).toMatch(/processor jar not found/);
  });
});
