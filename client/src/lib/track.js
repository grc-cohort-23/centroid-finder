// Helpers for the processor's result CSV: one "seconds,x,y" row per frame,
// no header. x = y = -1 means nothing was found in that frame.

export function parseTrackCsv(text) {
  const rows = [];
  for (const line of text.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const [t, x, y] = trimmed.split(',').map(Number);
    if ([t, x, y].some(Number.isNaN)) continue;
    rows.push({ t, x, y, found: x >= 0 && y >= 0 });
  }
  return rows;
}

// Index of the last row whose time is <= t (binary search). -1 if t is
// before the first row.
export function rowIndexAt(rows, t) {
  let lo = 0;
  let hi = rows.length - 1;
  let best = -1;
  while (lo <= hi) {
    const mid = (lo + hi) >> 1;
    if (rows[mid].t <= t) {
      best = mid;
      lo = mid + 1;
    } else {
      hi = mid - 1;
    }
  }
  return best;
}

export function summarize(rows) {
  const found = rows.filter((r) => r.found).length;
  return {
    frames: rows.length,
    found,
    foundFraction: rows.length ? found / rows.length : 0,
    duration: rows.length ? rows[rows.length - 1].t : 0,
  };
}
