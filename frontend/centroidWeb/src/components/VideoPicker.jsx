import { useState, useEffect } from "react";
import { Alert, Button } from "@mui/material";
import { requestJson } from "../api";

/**
 * Fetches the list of available videos from `GET /api/videos` and renders
 * them as a selectable list, each with a "Preview" button and a "Playback"
 * link (opens `/videos/<filename>` in a new tab).
 *
 * States:
 * - loading – `videos` is null; shows "Loading videos…".
 * - error   – request failed; shows the error message in an Alert.
 * - empty   – server returned `[]`; shows an info Alert.
 * - list    – renders one row per filename.
 *
 * The request is aborted on unmount.
 *
 * @param {Object} props
 * @param {(filename: string) => void} props.onSelect - Called with the filename when "Preview" is clicked.
 * @returns {JSX.Element}
 */
export default function VideoPicker({onSelect}) {
    const [videos, setVideos] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        async function fetchVideos() {
            try {
                const data = await requestJson("/api/videos", { signal: controller.signal });
                if (!controller.signal.aborted) setVideos(data);
            } catch (err) {
                if (!controller.signal.aborted) setError(err.message);
            }
        }

        fetchVideos();
        return () => controller.abort();
    }, []);

    if (error) return <Alert severity="error">{error}</Alert>;
    if (!videos) return <p role="status">Loading videos…</p>;
    if (!videos.length) {
        return <Alert severity="info">No videos found. Add an MP4 or MOV to the server’s video directory.</Alert>;
    }

    return (
        <ul className="video-list">
            {videos.map((video) => (
                <li key={video}>
                    <span>{video}</span>
                    <Button color="inherit" onClick={() => onSelect(video)} aria-label={`Preview ${video}`}>
                        Preview
                    </Button>
                    <a href={`/videos/${encodeURIComponent(video)}`} target="_blank" rel="noreferrer">
                        Playback
                    </a>
                </li>
            ))}
        </ul>
    );
}
