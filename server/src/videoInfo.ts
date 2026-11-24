import { execFile } from "child_process";
import type { Job } from "./jobStore.js";

// One video the way the frontend wants it (the same as VideoInfo in the frontend's types.ts)
export type VideoStatus = "not-processed" | "processing" | "tracked" | "error";
export type VideoInfo = {
    filename: string;
    status: VideoStatus;
    durationSeconds?: number;
    width?: number;
    height?: number;
};

// The video types we list and accept
export const VIDEO_EXTENSIONS = [".mp4", ".mov"];

// true for intro.mp4 or DEMO.MOV, false for notes.txt
export function isVideoFile(name: string): boolean {
    const lower = name.toLowerCase();
    return VIDEO_EXTENSIONS.some((extension) => lower.endsWith(extension));
}

// Works out a video's status from its newest job.
// jobs must be newest first (like listJobs gives them), so the first match is the newest.
export function videoStatus(filename: string, jobs: Job[]): VideoStatus {
    const newest = jobs.find((job) => job.filename === filename);
    if (!newest) return "not-processed";
    if (newest.status === "done") return "tracked";
    return newest.status; // "processing" or "error"
}

// What ffprobe prints with -of json (only the parts we ask for)
type ProbeOutput = {
    streams?: { width?: number; height?: number }[];
    format?: { duration?: string };
};

// The size and length of a video, if ffprobe can read them
export type VideoProbe = Pick<VideoInfo, "durationSeconds" | "width" | "height">;

// Asks ffprobe for a video's width, height and length.
// It never fails: if ffprobe is missing or the file isn't a real video, we get {} and the
// frontend just doesn't show those details.
export function probeVideo(videoPath: string): Promise<VideoProbe> {
    // execFile runs ffprobe directly (no shell), so a strange filename can't run other commands
    const args = [
        "-v", "error",
        "-select_streams", "v:0",                          // only the first video stream
        "-show_entries", "stream=width,height:format=duration",
        "-of", "json",
        videoPath,
    ];

    return new Promise((resolve) => {
        execFile("ffprobe", args, (error, stdout) => {
            if (error) {
                resolve({});
                return;
            }
            try {
                const output = JSON.parse(stdout) as ProbeOutput;
                const probe: VideoProbe = {};
                const stream = output.streams?.[0];
                const duration = Number(output.format?.duration);
                if (Number.isFinite(duration)) probe.durationSeconds = duration;
                if (stream?.width) probe.width = stream.width;
                if (stream?.height) probe.height = stream.height;
                resolve(probe);
            } catch {
                // ffprobe printed something that isn't JSON
                resolve({});
            }
        });
    });
}
