
import { useEffect, useState } from 'react';

import VideoList from '../components/VideoList';
import withRequestState from '../hoc/withRequestState';

// Create the enhanced component outside VideoChooser.
const VideoListWithRequestState = withRequestState(VideoList);

export default function VideoChooser() {
  const [vids, setVids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // console.trace('Current call stack:');
    const controller = new AbortController();

    async function fetchVideos() {
      setLoading(true);
      setError(null);

      try {
        const result = await fetch(
          `${import.meta.env.VITE_API_URL}/api/videos`,
          { signal: controller.signal }
        );

        if (!result.ok) {
          throw new Error('Failed to fetch videos');
        }

        const vidsObj = await result.json();

        if (!Array.isArray(vidsObj.availableVideos)) {
          throw new Error('Invalid video list response');
        }

        if (!controller.signal.aborted) {
          setVids(vidsObj.availableVideos);
        }

      } catch (err) {
        if (!controller.signal.aborted) {
          setError(err.message);
        }

      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    fetchVideos();

    return () => controller.abort();
  }, []);

  return (
    <section>
      <VideoListWithRequestState
        videos={vids}
        loading={loading}
        error={error}
      />
    </section>
  );
}
