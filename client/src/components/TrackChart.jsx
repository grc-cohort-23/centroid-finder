import { useMemo } from 'react';
import { formatTime } from '../lib/format.js';

const W = 1000;
const H = 160;

// Plots the centroid's x and y (as a share of the frame size) over time.
// Shaded bands mark frames where nothing was found. Click to seek.
export default function TrackChart({ rows, frameWidth, frameHeight, duration, currentTime, onSeek }) {
  const total = duration || (rows.length ? rows[rows.length - 1].t : 0) || 1;

  const { xPath, yPath, gaps } = useMemo(() => {
    const px = (t) => (t / total) * W;
    const series = (value, max) => {
      let d = '';
      let penDown = false;
      for (const row of rows) {
        if (!row.found) {
          penDown = false;
          continue;
        }
        const x = px(row.t).toFixed(1);
        const y = (H - (row[value] / max) * H).toFixed(1);
        d += `${penDown ? 'L' : 'M'}${x},${y}`;
        penDown = true;
      }
      return d;
    };

    const missing = [];
    let start = null;
    rows.forEach((row, i) => {
      if (!row.found && start === null) start = row.t;
      const endsRun = row.found || i === rows.length - 1;
      if (start !== null && endsRun) {
        const end = row.found ? row.t : total;
        missing.push({ x: px(start), w: Math.max(1, px(end) - px(start)) });
        start = null;
      }
    });

    return {
      xPath: series('x', frameWidth || 1),
      yPath: series('y', frameHeight || 1),
      gaps: missing,
    };
  }, [rows, total, frameWidth, frameHeight]);

  const playX = (Math.min(currentTime, total) / total) * W;

  function handleClick(e) {
    const rect = e.currentTarget.getBoundingClientRect();
    const share = Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width));
    onSeek(share * total);
  }

  return (
    <div className="chart">
      <div className="chart-legend">
        <span><i className="swatch x" /> x position</span>
        <span><i className="swatch y" /> y position</span>
        <span><i className="swatch gap" /> not found</span>
        <span className="muted chart-hint">Click the chart to jump to that moment</span>
      </div>
      <svg
        className="chart-svg"
        viewBox={`0 0 ${W} ${H}`}
        preserveAspectRatio="none"
        onClick={handleClick}
        role="img"
        aria-label="Centroid x and y position over time"
      >
        {gaps.map((g, i) => (
          <rect key={i} className="chart-gap" x={g.x} y="0" width={g.w} height={H} />
        ))}
        <line className="chart-grid" x1="0" x2={W} y1={H / 2} y2={H / 2} />
        <path className="chart-line x" d={xPath} />
        <path className="chart-line y" d={yPath} />
        <line className="chart-playhead" x1={playX} x2={playX} y1="0" y2={H} />
      </svg>
      <div className="chart-axis muted">
        <span>0:00</span>
        <span>{formatTime(total)}</span>
      </div>
    </div>
  );
}
