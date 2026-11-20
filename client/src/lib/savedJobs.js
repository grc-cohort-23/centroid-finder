// Remembers the last job started for each video in this browser, so the
// result is still there after a page reload. Storage can be unavailable
// (private mode, blocked site data), so every access is guarded.

const key = (filename) => `centroid:job:${filename}`;

export function loadSavedJob(filename) {
  try {
    const raw = localStorage.getItem(key(filename));
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveJob(filename, job) {
  try {
    localStorage.setItem(key(filename), JSON.stringify(job));
  } catch {
    // Not critical; the job still runs.
  }
}

export function clearSavedJob(filename) {
  try {
    localStorage.removeItem(key(filename));
  } catch {
    // ignore
  }
}
