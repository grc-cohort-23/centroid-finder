import { useState, useEffect } from "react";


export default function VideoPicker({onSelect}) {
    const [videos, setVideos] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchVideos() {
            try{
                const response = await fetch("/api/videos");

                if (!response.ok) {
                    throw new Error("Failed to fetch videos");

                }

                const data = await response.json();
                setVideos(data);

            } catch (err) {
                setError(err.message);
            }
        }
        fetchVideos();
    }, []);
    return (

        <ul>
            {error && <p>{error}</p>}

            {videos.map((video) => (
                <li key={video}>
                    {video}{" "}
                    <button onClick={() => onSelect(video)}>
                        Preview 
                        </button> 
                        {" "}
                        <a href ={`/videos/${encodeURIComponent(video)}`} target="_blank" rel="noreferrer">
                        Playback
                        </a>
                </li>
            ))}
        </ul>
        
    );

}
