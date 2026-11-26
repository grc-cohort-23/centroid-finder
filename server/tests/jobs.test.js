const jobs = require('../jobs');

const sampleJob = (id) => ({
  id,
  filename: 'intro.mp4',
  targetColor: 'ff0000',
  threshold: 120,
});

describe('jobs module', () => {
  test('createJob saves a new job as processing', () => {
    jobs.createJob(sampleJob('job-1'));

    expect(jobs.getJob('job-1')).toMatchObject({
      id: 'job-1',
      filename: 'intro.mp4',
      targetColor: 'ff0000',
      threshold: 120,
      status: 'processing',
      result: null,
      error: null,
    });
  });

  test('createJob records when the job was created', () => {
    jobs.createJob(sampleJob('job-2'));

    expect(jobs.getJob('job-2').createdAt).toEqual(expect.any(String));
  });

  test('updateJob marks a job done with its result', () => {
    jobs.createJob(sampleJob('job-3'));
    jobs.updateJob('job-3', { status: 'done', result: '/results/intro.mp4.csv' });

    expect(jobs.getJob('job-3')).toMatchObject({
      status: 'done',
      result: '/results/intro.mp4.csv',
      error: null,
    });
  });

  test('updateJob marks a job as an error with its message', () => {
    jobs.createJob(sampleJob('job-4'));
    jobs.updateJob('job-4', { status: 'error', error: 'Error processing video: boom' });

    expect(jobs.getJob('job-4')).toMatchObject({
      status: 'error',
      result: null,
      error: 'Error processing video: boom',
    });
  });

  test('updateJob only changes the job it is given', () => {
    jobs.createJob(sampleJob('job-5'));
    jobs.createJob(sampleJob('job-6'));
    jobs.updateJob('job-5', { status: 'done', result: '/results/intro.mp4.csv' });

    expect(jobs.getJob('job-6').status).toBe('processing');
  });

  test('getJob returns null for an unknown ID', () => {
    expect(jobs.getJob('does-not-exist')).toBeNull();
  });
});
