// The parts of the server that do the actual work.
const { spawn } = require("child_process");

// ============================================================
// Thumbnail: first frame of a video as a JPEG, using ffmpeg
// ============================================================
/** Resolves with the JPEG bytes, or rejects if ffmpeg fails. */
function makeThumbnail(videoPath) {
  return new Promise((resolve, reject) => {
    // 1 frame, as a JPEG, written to ffmpeg's output (pipe:1) instead of a file
    const ffmpeg = spawn("ffmpeg", [
      "-loglevel", "error",
      "-i", videoPath,
      "-frames:v", "1",
      "-f", "image2pipe",
      "-vcodec", "mjpeg",
      "pipe:1",
    ]);

    const chunks = [];
    ffmpeg.stdout.on("data", (chunk) => chunks.push(chunk));
    ffmpeg.on("error", reject); // ffmpeg isn't installed
    ffmpeg.on("close", (code) => {
      if (code === 0 && chunks.length > 0) {
        resolve(Buffer.concat(chunks));
      } else {
        reject(new Error(`ffmpeg exited with code ${code}`));
      }
    });
  });
}

module.exports = { makeThumbnail };