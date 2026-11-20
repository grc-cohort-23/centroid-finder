// Canvas drawing for the overlay that sits on top of the video. All
// coordinates are in video pixels; `scale` keeps markers a sensible size
// whether the video is 480p or 4K.

import { rowIndexAt } from './track.js';

export const COLORS = {
  highlight: [0, 229, 255], // matched pixels in Highlight view
  preview: '#ffd400', // live-preview crosshair
  track: '255, 79, 139', // tracked path (rgb, alpha added per use)
};

const TRAIL_SECONDS = 1.5;

// Paints the binarized mask into `img` (an ImageData the size of the frame).
export function paintMask(img, mask, mode) {
  const d = img.data;
  const [hr, hg, hb] = COLORS.highlight;
  for (let i = 0, p = 0; i < mask.length; i++, p += 4) {
    if (mode === 'mask') {
      const v = mask[i] ? 255 : 0;
      d[p] = v;
      d[p + 1] = v;
      d[p + 2] = v;
      d[p + 3] = 255;
    } else if (mask[i]) {
      d[p] = hr;
      d[p + 1] = hg;
      d[p + 2] = hb;
      d[p + 3] = 190;
    } else {
      // Dim everything that doesn't match so the matches stand out.
      d[p] = 0;
      d[p + 1] = 0;
      d[p + 2] = 0;
      d[p + 3] = 120;
    }
  }
}

export function drawCrosshair(ctx, { x, y }, scale, color = COLORS.preview) {
  if (x < 0 || y < 0) return;
  const r = 11 * scale;
  const shape = () => {
    ctx.beginPath();
    ctx.arc(x, y, r, 0, Math.PI * 2);
    for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      ctx.moveTo(x + dx * r * 0.45, y + dy * r * 0.45);
      ctx.lineTo(x + dx * r * 1.8, y + dy * r * 1.8);
    }
  };
  ctx.save();
  ctx.lineCap = 'round';
  ctx.strokeStyle = 'rgba(0,0,0,0.65)';
  ctx.lineWidth = 4.5 * scale;
  shape();
  ctx.stroke();
  ctx.strokeStyle = color;
  ctx.lineWidth = 2 * scale;
  shape();
  ctx.stroke();
  ctx.restore();
}

function strokeRows(ctx, rows, from, to) {
  ctx.beginPath();
  let penDown = false;
  for (let i = from; i < to; i++) {
    const row = rows[i];
    if (!row.found) {
      penDown = false;
    } else if (!penDown) {
      ctx.moveTo(row.x, row.y);
      penDown = true;
    } else {
      ctx.lineTo(row.x, row.y);
    }
  }
  ctx.stroke();
}

// Draws the whole tracked path faintly, the last moments brightly, and a
// dot where the object is at time `t`.
export function drawTrack(ctx, rows, t, scale) {
  if (!rows.length) return;
  ctx.save();
  ctx.lineJoin = 'round';
  ctx.lineCap = 'round';

  ctx.strokeStyle = `rgba(${COLORS.track}, 0.35)`;
  ctx.lineWidth = 2 * scale;
  strokeRows(ctx, rows, 0, rows.length);

  const now = rowIndexAt(rows, t);
  if (now >= 0) {
    const trailStart = Math.max(0, rowIndexAt(rows, t - TRAIL_SECONDS));
    ctx.strokeStyle = `rgba(${COLORS.track}, 0.95)`;
    ctx.lineWidth = 3.5 * scale;
    strokeRows(ctx, rows, trailStart, now + 1);

    const row = rows[now];
    if (row.found) {
      ctx.beginPath();
      ctx.arc(row.x, row.y, 7 * scale, 0, Math.PI * 2);
      ctx.fillStyle = `rgb(${COLORS.track})`;
      ctx.fill();
      ctx.lineWidth = 2.5 * scale;
      ctx.strokeStyle = '#fff';
      ctx.stroke();
    }
  }
  ctx.restore();
}
