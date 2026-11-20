import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { fetchResultCsv, resultUrl, startJob, videoUrl } from '../api.js';
import useJob from '../hooks/useJob.js';
import {
  MAX_DISTANCE,
  colorDistance,
  detect,
  hexToRgb,
  rgbToHex,
  sampleColor,
} from '../lib/detection.js';
import { drawCrosshair, drawTrack, paintMask } from '../lib/draw.js';
import { formatPercent, formatTime } from '../lib/format.js';
import { clearSavedJob, loadSavedJob, saveJob } from '../lib/savedJobs.js';
import { parseTrackCsv, summarize } from '../lib/track.js';
import ColorField from './ColorField.jsx';
import TrackChart from './TrackChart.jsx';

const VIEW_MODES = [
  { id: 'original', label: 'Original', hint: 'The video as-is' },
  { id: 'overlay', label: 'Highlight', hint: 'Matching pixels glow, the rest is dimmed' },
  { id: 'mask', label: 'Mask', hint: 'Exactly what the processor sees: white = match' },
];

const FRAME_STEP = 1 / 30;
const STATS_INTERVAL_MS = 120;

export default function Workspace({ filename }) {
  const saved = useMemo(() => loadSavedJob(filename), [filename]);

  // Detection settings
  const [targetColor, setTargetColor] = useState(saved?.targetColor ?? 'ff0000');
  const [threshold, setThreshold] = useState(saved?.threshold ?? 60);

  // Tools and display
  const [eyedropper, setEyedropper] = useState(!saved);
  const [view, setView] = useState('overlay');
  const [showPreview, setShowPreview] = useState(true);
  const [showTrack, setShowTrack] = useState(true);

  // Video
  const [videoInfo, setVideoInfo] = useState(null);
  const [videoError, setVideoError] = useState(null);
  const [playing, setPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [stats, setStats] = useState(null);
  const [hover, setHover] = useState(null);

  // Job + results
  const [jobRun, setJobRun] = useState(saved);
  const [starting, setStarting] = useState(false);
  const [startError, setStartError] = useState(null);
  const job = useJob(jobRun?.jobId ?? null);
  const [track, setTrack] = useState(null);
  const [trackError, setTrackError] = useState(null);

  const videoRef = useRef(null);
  const overlayRef = useRef(null);
  const frameCanvasRef = useRef(null);
  const frameRef = useRef(null);
  const maskImageRef = useRef(null);
  const lastStatsRef = useRef(0);
  const settingsRef = useRef(null);

  settingsRef.current = {
    target: hexToRgb(targetColor),
    threshold,
    view,
    showPreview,
    showTrack,
    track,
  };

  // ---- Frame rendering -------------------------------------------------

  const render = useCallback((forceStats = false) => {
    const video = videoRef.current;
    const overlay = overlayRef.current;
    if (!video || !overlay || !video.videoWidth) return;

    const w = video.videoWidth;
    const h = video.videoHeight;

    // Grab the current frame's pixels from an offscreen canvas.
    if (!frameCanvasRef.current) frameCanvasRef.current = document.createElement('canvas');
    const fc = frameCanvasRef.current;
    if (fc.width !== w || fc.height !== h) {
      fc.width = w;
      fc.height = h;
    }
    const fctx = fc.getContext('2d', { willReadFrequently: true });
    fctx.drawImage(video, 0, 0, w, h);
    const frame = fctx.getImageData(0, 0, w, h);
    frameRef.current = frame;

    if (overlay.width !== w || overlay.height !== h) {
      overlay.width = w;
      overlay.height = h;
    }
    const ctx = overlay.getContext('2d');
    ctx.clearRect(0, 0, w, h);

    const s = settingsRef.current;
    const scale = Math.max(w, h) / 900;
    let result = null;

    if (s.showPreview || s.view !== 'original') {
      result = detect(frame.data, w, h, s.target, s.threshold);

      if (s.view !== 'original') {
        let img = maskImageRef.current;
        if (!img || img.width !== w || img.height !== h) {
          img = new ImageData(w, h);
          maskImageRef.current = img;
        }
        paintMask(img, result.mask, s.view);
        ctx.putImageData(img, 0, 0);
      }
    }

    if (s.showTrack && s.track) drawTrack(ctx, s.track, video.currentTime, scale);
    if (s.showPreview && result) drawCrosshair(ctx, result.centroid, scale);

    setCurrentTime(video.currentTime);

    const now = performance.now();
    if (result && (forceStats || now - lastStatsRef.current > STATS_INTERVAL_MS)) {
      lastStatsRef.current = now;
      setStats({
        matchedFraction: result.matchedFraction,
        groups: result.groups.length,
        largest: result.groups[0]?.size ?? 0,
        centroid: result.centroid,
        framePixels: w * h,
      });
    } else if (!result) {
      setStats(null);
    }
  }, []);

  // Redraw whenever a setting changes (works while paused, too).
  useEffect(() => {
    render(true);
  }, [targetColor, threshold, view, showPreview, showTrack, track, render]);

  // Video events + a per-frame loop while playing.
  useEffect(() => {
    const video = videoRef.current;
    if (!video) return undefined;

    const useVideoFrames = 'requestVideoFrameCallback' in HTMLVideoElement.prototype;
    let handle = null;
    let stopped = false;

    const cancel = () => {
      if (handle === null) return;
      if (useVideoFrames) video.cancelVideoFrameCallback(handle);
      else cancelAnimationFrame(handle);
      handle = null;
    };
    const tick = () => {
      handle = null;
      if (stopped) return;
      render();
      if (!video.paused && !video.ended) schedule();
    };
    const schedule = () => {
      if (handle !== null) return;
      handle = useVideoFrames ? video.requestVideoFrameCallback(tick) : requestAnimationFrame(tick);
    };

    const onPlay = () => {
      setPlaying(true);
      schedule();
    };
    const onPause = () => {
      setPlaying(false);
      cancel();
      render(true);
    };
    const onSeeked = () => render(true);
    const onLoaded = () => {
      setVideoInfo({ width: video.videoWidth, height: video.videoHeight, duration: video.duration });
      render(true);
    };
    const onError = () => setVideoError('This browser could not play the video. The processor can still run on it.');

    video.addEventListener('play', onPlay);
    video.addEventListener('pause', onPause);
    video.addEventListener('ended', onPause);
    video.addEventListener('seeked', onSeeked);
    video.addEventListener('loadeddata', onLoaded);
    video.addEventListener('error', onError);
    if (video.readyState >= 2) onLoaded();

    return () => {
      stopped = true;
      cancel();
      video.removeEventListener('play', onPlay);
      video.removeEventListener('pause', onPause);
      video.removeEventListener('ended', onPause);
      video.removeEventListener('seeked', onSeeked);
      video.removeEventListener('loadeddata', onLoaded);
      video.removeEventListener('error', onError);
    };
  }, [render]);

  // ---- Job lifecycle ---------------------------------------------------

  useEffect(() => {
    if (job.status !== 'done' || !job.result) return undefined;
    let cancelled = false;
    setTrackError(null);
    fetchResultCsv(job.result)
      .then((text) => !cancelled && setTrack(parseTrackCsv(text)))
      .catch((err) => !cancelled && setTrackError(err.message));
    return () => {
      cancelled = true;
    };
  }, [job.status, job.result]);

  // A remembered job the server no longer knows about (e.g. jobs.db was
  // deleted) is just forgotten.
  useEffect(() => {
    if (job.notFound) {
      clearSavedJob(filename);
      setJobRun(null);
    }
  }, [job.notFound, filename]);

  async function runDetection() {
    setStarting(true);
    setStartError(null);
    try {
      const jobId = await startJob(filename, targetColor, threshold);
      const run = { jobId, targetColor, threshold, startedAt: Date.now() };
      saveJob(filename, run);
      setTrack(null);
      setTrackError(null);
      setJobRun(run);
    } catch (err) {
      setStartError(err.message);
    } finally {
      setStarting(false);
    }
  }

  // ---- Interaction -----------------------------------------------------

  function togglePlay() {
    const video = videoRef.current;
    if (!video) return;
    if (video.paused || video.ended) video.play();
    else video.pause();
  }

  function seek(t) {
    const video = videoRef.current;
    if (!video || !Number.isFinite(video.duration)) return;
    video.currentTime = Math.min(Math.max(0, t), video.duration);
  }

  function step(direction) {
    const video = videoRef.current;
    if (!video) return;
    video.pause();
    seek(video.currentTime + direction * FRAME_STEP);
  }

  function pointerToPixel(e) {
    const overlay = overlayRef.current;
    const frame = frameRef.current;
    if (!overlay || !frame) return null;
    const rect = overlay.getBoundingClientRect();
    const fx = (e.clientX - rect.left) / rect.width;
    const fy = (e.clientY - rect.top) / rect.height;
    return {
      x: Math.min(frame.width - 1, Math.max(0, Math.floor(fx * frame.width))),
      y: Math.min(frame.height - 1, Math.max(0, Math.floor(fy * frame.height))),
      left: fx * 100,
      top: fy * 100,
      frame,
    };
  }

  function handlePointerMove(e) {
    const p = pointerToPixel(e);
    if (!p) return;
    const color = sampleColor(p.frame.data, p.frame.width, p.frame.height, p.x, p.y, 0);
    setHover({
      x: p.x,
      y: p.y,
      left: p.left,
      top: p.top,
      hex: rgbToHex(color),
      distance: colorDistance(color, settingsRef.current.target),
    });
  }

  function handleStageClick(e) {
    if (!eyedropper) {
      togglePlay();
      return;
    }
    const p = pointerToPixel(e);
    if (!p) return;
    const color = sampleColor(p.frame.data, p.frame.width, p.frame.height, p.x, p.y, 2);
    setTargetColor(rgbToHex(color));
  }

  // Keyboard shortcuts: space = play/pause, arrows = step, E = eyedropper.
  useEffect(() => {
    function onKey(e) {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) return;
      if (e.key === ' ') {
        e.preventDefault();
        togglePlay();
      } else if (e.key === 'ArrowLeft') {
        e.preventDefault();
        step(-1);
      } else if (e.key === 'ArrowRight') {
        e.preventDefault();
        step(1);
      } else if (e.key.toLowerCase() === 'e') {
        setEyedropper((v) => !v);
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  });

  // ---- Derived values --------------------------------------------------

  const settingsChanged = jobRun
    && (jobRun.targetColor !== targetColor || Number(jobRun.threshold) !== Number(threshold));
  const summary = track ? summarize(track) : null;
  const duration = videoInfo?.duration ?? 0;
  const hint = previewHint(stats);

  return (
    <div className="workspace">
      <section className="stage-col">
        <div
          className="stage"
          style={videoInfo ? { aspectRatio: `${videoInfo.width} / ${videoInfo.height}` } : undefined}
        >
          <video
            ref={videoRef}
            src={videoUrl(filename)}
            preload="auto"
            muted
            playsInline
          />
          <canvas
            ref={overlayRef}
            className={`overlay ${eyedropper ? 'picking' : ''}`}
            onClick={handleStageClick}
            onMouseMove={handlePointerMove}
            onMouseLeave={() => setHover(null)}
          />
          {hover && (
            <div
              className={`hover-tip ${hover.left > 70 ? 'flip-x' : ''} ${hover.top > 75 ? 'flip-y' : ''}`}
              style={{ left: `${hover.left}%`, top: `${hover.top}%` }}
            >
              <span className="tip-swatch" style={{ background: `#${hover.hex}` }} />
              <span className="mono">#{hover.hex}</span>
              <span className={hover.distance < threshold ? 'tip-match' : 'tip-miss'}>
                {Math.round(hover.distance)} {hover.distance < threshold ? '✓ match' : '✗'}
              </span>
              {eyedropper && <span className="tip-action">click to pick</span>}
            </div>
          )}
          {!videoInfo && !videoError && <div className="stage-message">Loading video…</div>}
          {videoError && <div className="stage-message error">{videoError}</div>}
        </div>

        <div className="transport">
          <button type="button" className="icon-btn" onClick={() => step(-1)} title="Previous frame (←)" aria-label="Previous frame">⏮</button>
          <button type="button" className="icon-btn primary" onClick={togglePlay} title="Play / pause (space)" aria-label={playing ? 'Pause' : 'Play'}>
            {playing ? '❚❚' : '▶'}
          </button>
          <button type="button" className="icon-btn" onClick={() => step(1)} title="Next frame (→)" aria-label="Next frame">⏭</button>
          <input
            className="scrubber"
            type="range"
            min="0"
            max={duration || 0}
            step="0.01"
            value={Math.min(currentTime, duration || 0)}
            onChange={(e) => seek(Number(e.target.value))}
            aria-label="Seek"
          />
          <span className="time mono">
            {formatTime(currentTime)} / {formatTime(duration)}
          </span>
        </div>

        {track && (
          <TrackChart
            rows={track}
            frameWidth={videoInfo?.width}
            frameHeight={videoInfo?.height}
            duration={duration}
            currentTime={currentTime}
            onSeek={seek}
          />
        )}
      </section>

      <aside className="panel">
        <div className="panel-section">
          <h2>1. Target color</h2>
          <div className="row">
            <button
              type="button"
              className={`btn tool ${eyedropper ? 'active' : ''}`}
              onClick={() => setEyedropper((v) => !v)}
              aria-pressed={eyedropper}
              title="Eyedropper (E)"
            >
              <span aria-hidden="true">⌖</span> Eyedropper
            </button>
            <ColorField value={targetColor} onChange={setTargetColor} />
          </div>
          <p className="muted small">
            {eyedropper
              ? 'Click the object in the video to sample its color.'
              : 'Turn on the eyedropper, or type a hex color.'}
          </p>
        </div>

        <div className="panel-section">
          <h2>2. Threshold</h2>
          <div className="row">
            <input
              className="slider"
              type="range"
              min="0"
              max={MAX_DISTANCE}
              value={threshold}
              onChange={(e) => setThreshold(Number(e.target.value))}
              style={{ '--fill': `${(threshold / MAX_DISTANCE) * 100}%` }}
              aria-label="Threshold"
            />
            <input
              className="num mono"
              type="number"
              min="0"
              max={MAX_DISTANCE}
              value={threshold}
              onChange={(e) => {
                const v = Math.round(Number(e.target.value));
                if (Number.isFinite(v)) setThreshold(Math.min(MAX_DISTANCE, Math.max(0, v)));
              }}
              aria-label="Threshold value"
            />
          </div>
          <p className="muted small">
            How far a pixel's color can be from the target and still count. Lower is stricter.
          </p>
        </div>

        <div className="panel-section">
          <h2>Preview</h2>
          <div className="segmented" role="radiogroup" aria-label="View mode">
            {VIEW_MODES.map((m) => (
              <button
                key={m.id}
                type="button"
                role="radio"
                aria-checked={view === m.id}
                className={view === m.id ? 'on' : ''}
                onClick={() => setView(m.id)}
                title={m.hint}
              >
                {m.label}
              </button>
            ))}
          </div>
          <label className="check">
            <input type="checkbox" checked={showPreview} onChange={(e) => setShowPreview(e.target.checked)} />
            <span><i className="key-dot preview" /> Live centroid (this frame)</span>
          </label>
          {track && (
            <label className="check">
              <input type="checkbox" checked={showTrack} onChange={(e) => setShowTrack(e.target.checked)} />
              <span><i className="key-dot track" /> Tracked path (from processor)</span>
            </label>
          )}

          {stats && (
            <dl className="stats">
              <div>
                <dt>Matching</dt>
                <dd>{formatPercent(stats.matchedFraction)}</dd>
              </div>
              <div>
                <dt>Blobs</dt>
                <dd>{stats.groups.toLocaleString()}</dd>
              </div>
              <div>
                <dt>Largest</dt>
                <dd>{stats.largest.toLocaleString()} px</dd>
              </div>
              <div>
                <dt>Centroid</dt>
                <dd className="mono">
                  {stats.centroid.x < 0 ? '—' : `${stats.centroid.x}, ${stats.centroid.y}`}
                </dd>
              </div>
            </dl>
          )}
          {hint && <p className={`hint ${hint.level}`}>{hint.text}</p>}
        </div>

        <div className="panel-section">
          <h2>3. Track the whole video</h2>
          <button
            type="button"
            className="btn primary block"
            onClick={runDetection}
            disabled={starting || job.status === 'processing'}
          >
            {job.status === 'processing' ? 'Processing…' : starting ? 'Starting…' : 'Run detection'}
          </button>
          <p className="muted small">
            Sends <span className="mono">#{targetColor}</span> and threshold{' '}
            <span className="mono">{threshold}</span> to the Java processor, which finds the largest blob in every frame.
          </p>
          {startError && <p className="hint bad" role="alert">{startError}</p>}

          {jobRun && (
            <JobCard
              job={job}
              run={jobRun}
              summary={summary}
              trackError={trackError}
              settingsChanged={settingsChanged}
              onRestore={() => {
                setTargetColor(jobRun.targetColor);
                setThreshold(Number(jobRun.threshold));
              }}
            />
          )}
        </div>

        <p className="muted small shortcuts">
          <kbd>Space</kbd> play/pause · <kbd>←</kbd>/<kbd>→</kbd> step frame · <kbd>E</kbd> eyedropper
        </p>
      </aside>
    </div>
  );
}

function previewHint(stats) {
  if (!stats) return null;
  if (stats.groups === 0) {
    return { level: 'bad', text: 'Nothing matches in this frame. Raise the threshold or pick the color again.' };
  }
  if (stats.matchedFraction > 0.25) {
    return { level: 'warn', text: 'Over a quarter of the frame matches. Lower the threshold so only the object lights up.' };
  }
  if (stats.groups > 50 && stats.largest < stats.framePixels * 0.001) {
    return { level: 'warn', text: 'Lots of tiny specks and no clear blob. Try sampling a more typical part of the object.' };
  }
  return { level: 'good', text: 'Looks trackable. Scrub through the video to check other frames, then run detection.' };
}

function JobCard({ job, run, summary, trackError, settingsChanged, onRestore }) {
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    if (job.status !== 'processing') return undefined;
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, [job.status]);

  const elapsed = run.startedAt ? Math.max(0, Math.round((now - run.startedAt) / 1000)) : null;

  return (
    <div className={`job-card ${job.status}`}>
      <div className="job-head">
        <span className={`status-pill ${job.status}`}>
          {job.status === 'processing' && <span className="spinner" aria-hidden="true" />}
          {job.status === 'processing' ? 'Processing' : job.status === 'done' ? 'Done' : 'Error'}
        </span>
        <span className="job-params">
          <span className="dot" style={{ background: `#${run.targetColor}` }} />
          <span className="mono">#{run.targetColor}</span> · {run.threshold}
        </span>
      </div>

      {job.status === 'processing' && (
        <p className="muted small">
          The processor is working through every frame{elapsed !== null ? ` (${elapsed}s so far)` : ''}. You can keep tuning while you wait.
        </p>
      )}

      {job.status === 'error' && <p className="hint bad">{job.error || 'Processing failed.'}</p>}

      {job.status === 'done' && (
        <>
          {trackError && <p className="hint bad">{trackError}</p>}
          {summary && (
            <dl className="stats">
              <div>
                <dt>Frames</dt>
                <dd>{summary.frames.toLocaleString()}</dd>
              </div>
              <div>
                <dt>Found in</dt>
                <dd>{formatPercent(summary.foundFraction, 0)}</dd>
              </div>
            </dl>
          )}
          <a className="btn block" href={resultUrl(job.result)} download>
            Download CSV
          </a>
        </>
      )}

      {settingsChanged && (
        <p className="small changed">
          Your settings changed since this run.{' '}
          <button type="button" className="link" onClick={onRestore}>Use the run's settings</button>
        </p>
      )}
    </div>
  );
}
