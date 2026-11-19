// Browser-side copy of the Java processor's detection math, used for the
// live preview. It mirrors:
//   EuclideanColorDistance   - distance in RGB space
//   DistanceImageBinarizer   - white when distance < threshold (strict)
//   DfsBinaryGroupFinder     - 4-connected groups, integer-division centroids
//   LargestGroupSelector     - biggest group wins, (-1, -1) when none

// Largest possible RGB distance: sqrt(3 * 255^2).
export const MAX_DISTANCE = Math.ceil(Math.sqrt(3) * 255);

export function hexToRgb(hex) {
  const clean = hex.replace(/^#/, '');
  if (!/^[0-9a-fA-F]{6}$/.test(clean)) {
    throw new Error(`Invalid hex color: ${hex}`);
  }
  const n = parseInt(clean, 16);
  return { r: (n >> 16) & 0xff, g: (n >> 8) & 0xff, b: n & 0xff };
}

export function rgbToHex({ r, g, b }) {
  return [r, g, b].map((v) => v.toString(16).padStart(2, '0')).join('');
}

export function colorDistance(a, b) {
  const dr = a.r - b.r;
  const dg = a.g - b.g;
  const db = a.b - b.b;
  return Math.sqrt(dr * dr + dg * dg + db * db);
}

// pixels: RGBA bytes (like ImageData.data). Returns a Uint8Array of 0/1.
export function binarize(pixels, width, height, target, threshold) {
  const mask = new Uint8Array(width * height);
  // Compare squared distances to avoid a sqrt per pixel. Because both sides
  // are non-negative, d < t  <=>  d^2 < t^2.
  const limit = threshold * threshold;
  for (let i = 0, p = 0; i < mask.length; i++, p += 4) {
    const dr = pixels[p] - target.r;
    const dg = pixels[p + 1] - target.g;
    const db = pixels[p + 2] - target.b;
    if (dr * dr + dg * dg + db * db < limit) mask[i] = 1;
  }
  return mask;
}

// Returns groups sorted like Java's Group.compareTo, descending:
// size, then centroid x, then centroid y.
export function findGroups(mask, width, height) {
  const visited = new Uint8Array(mask.length);
  const stack = new Int32Array(mask.length);
  const groups = [];
  let top = 0;

  // Adds an unvisited matching pixel to the DFS stack.
  const push = (n) => {
    if (mask[n] && !visited[n]) {
      visited[n] = 1;
      stack[top++] = n;
    }
  };

  for (let start = 0; start < mask.length; start++) {
    if (!mask[start] || visited[start]) continue;

    top = 0;
    push(start);
    let size = 0;
    let sumX = 0;
    let sumY = 0;

    while (top > 0) {
      const idx = stack[--top];
      const x = idx % width;
      const y = (idx - x) / width;
      size++;
      sumX += x;
      sumY += y;

      // down, up, right, left
      if (y + 1 < height) push(idx + width);
      if (y > 0) push(idx - width);
      if (x + 1 < width) push(idx + 1);
      if (x > 0) push(idx - 1);
    }

    groups.push({
      size,
      x: Math.floor(sumX / size),
      y: Math.floor(sumY / size),
    });
  }

  groups.sort((a, b) => b.size - a.size || b.x - a.x || b.y - a.y);
  return groups;
}

export function largestCentroid(groups) {
  if (groups.length === 0) return { x: -1, y: -1 };
  return { x: groups[0].x, y: groups[0].y };
}

// Runs the whole pipeline on one frame.
export function detect(pixels, width, height, target, threshold) {
  const mask = binarize(pixels, width, height, target, threshold);
  const groups = findGroups(mask, width, height);
  let matched = 0;
  for (let i = 0; i < mask.length; i++) matched += mask[i];
  return {
    mask,
    groups,
    centroid: largestCentroid(groups),
    matchedFraction: matched / mask.length,
  };
}

// Averages the colors in a (2r+1)x(2r+1) square around (x, y), clamped to
// the image. Picking one pixel is noisy on compressed video.
export function sampleColor(pixels, width, height, x, y, radius = 1) {
  let r = 0;
  let g = 0;
  let b = 0;
  let n = 0;
  for (let yy = Math.max(0, y - radius); yy <= Math.min(height - 1, y + radius); yy++) {
    for (let xx = Math.max(0, x - radius); xx <= Math.min(width - 1, x + radius); xx++) {
      const p = (yy * width + xx) * 4;
      r += pixels[p];
      g += pixels[p + 1];
      b += pixels[p + 2];
      n++;
    }
  }
  return { r: Math.round(r / n), g: Math.round(g / n), b: Math.round(b / n) };
}
