// Thin wrappers around the Salamander API served by the Express server.

const enc = encodeURIComponent;

async function readError(res, fallback) {
  try {
    const body = await res.json();
    return body.error || fallback;
  } catch {
    return fallback;
  }
}

export async function listVideos() {
  const res = await fetch('/api/videos');
  if (!res.ok) throw new Error(await readError(res, 'Could not load videos'));
  return res.json();
}

export const thumbnailUrl = (filename) => `/thumbnail/${enc(filename)}`;
export const videoUrl = (filename) => `/videos/${enc(filename)}`;

export async function startJob(filename, targetColor, threshold) {
  const query = new URLSearchParams({ targetColor, threshold: String(threshold) });
  const res = await fetch(`/process/${enc(filename)}?${query}`, { method: 'POST' });
  if (res.status !== 202) throw new Error(await readError(res, 'Could not start job'));
  const { jobId } = await res.json();
  return jobId;
}

export async function getJobStatus(jobId) {
  const res = await fetch(`/process/${enc(jobId)}/status`);
  if (!res.ok) throw new Error(await readError(res, 'Could not fetch job status'));
  return res.json();
}

// `result` is the server's path, e.g. "/results/intro.mp4.csv".
export const resultUrl = (result) => encodeURI(result);

export async function fetchResultCsv(result) {
  const res = await fetch(resultUrl(result));
  if (!res.ok) throw new Error('Could not download result CSV');
  return res.text();
}
