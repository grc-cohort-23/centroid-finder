import { useEffect, useState } from 'react';
import { listVideos, thumbnailUrl } from '../api.js';
import { loadSavedJob } from '../lib/savedJobs.js';

export default function VideoLibrary({ onSelect }) {
  const [videos, setVideos] = useState(null);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setError(null);
    listVideos()
      .then((list) => !cancelled && setVideos(list))
      .catch((err) => !cancelled && setError(err.message));
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  return (
    <section className="library">
      <div className="library-head">
        <div>
          <h1>Videos</h1>
          <p className="muted">
            Pick a video, click the object you want to follow, and tune the threshold until only it lights up.
          </p>
        </div>
        <button type="button" className="btn" onClick={() => setReloadKey((k) => k + 1)}>
          Refresh
        </button>
      </div>

      {error && (
        <div className="notice error" role="alert">
          <strong>{error}.</strong> Is the server running on port 3000, and does the <code>videos/</code> folder exist?
        </div>
      )}

      {!error && videos === null && <p className="muted">Loading videos…</p>}

      {videos && videos.length === 0 && (
        <div className="notice">
          No videos yet. Drop <code>.mp4</code>, <code>.mov</code>, <code>.avi</code>, <code>.mkv</code> or{' '}
          <code>.webm</code> files into the <code>videos/</code> folder at the project root, then refresh.
        </div>
      )}

      {videos && videos.length > 0 && (
        <ul className="video-grid">
          {videos.map((name) => {
            const saved = loadSavedJob(name);
            return (
              <li key={name}>
                <button type="button" className="video-card" onClick={() => onSelect(name)}>
                  <div className="thumb">
                    <img src={thumbnailUrl(name)} alt="" loading="lazy" />
                    {saved && (
                      <span className="badge" title={`Last run: #${saved.targetColor}, threshold ${saved.threshold}`}>
                        <span className="dot" style={{ background: `#${saved.targetColor}` }} />
                        Tracked
                      </span>
                    )}
                  </div>
                  <span className="video-name">{name}</span>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
