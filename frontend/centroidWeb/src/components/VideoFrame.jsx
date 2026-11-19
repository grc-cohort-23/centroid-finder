import {useEffect, useRef } from "react";

export default function VideoFrame({ filename }) {

    const videoRef = useRef(null);
    const canvasRef = useRef(null);


    useEffect(() => {
        const video = videoRef.current;
        const canvas = canvasRef.current;



        if(!filename || !video || !canvas) return;

        function drawFrame() {

            const ctx = canvas.getContext("2d");
            if (!ctx || !video.videoWidth || !video.videoHeight) return;
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;

            ctx.drawImage(video, 0, 0);

        }

       video.addEventListener("loadeddata", drawFrame);

        if (video.readyState >= 2) {
            drawFrame();
        } else {
            video.load();
        }

        return () => {
            video.removeEventListener("loadeddata", drawFrame);

        };
    }, [filename]);

    if (!filename) {
        return <p>Select a video to preview</p>;
    }
    return (
        <>
        <video
        ref={videoRef}
         src={`/videos/${encodeURIComponent(filename)}`}
         preload="auto"
         muted
         style={{ display: "none" }}
        />

        <canvas
        ref={canvasRef}
        style={{ maxWidth: "100%", height: "auto" }}
        />
        </>
    );
}