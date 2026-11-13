import ffmpegPath from "ffmpeg-static";
import { spawn } from "node:child_process";
import fs from "node:fs/promises";
import path from "node:path";


export const home = (req, res)=>{
    res.send("Hello from home page");
    
};

export const serverHealth = (req, res)=>{
    res.send("<h1>Server is live!</h1> <br> <strong>current available endpoints:</strong> <br> /health <br> /thumbnail/:filename <br> /api/videos <br> /process/:filename <br> /process/:jobId/status");
    console.log("server is live Hooray!");
};

/**
 * @description handles the retrieval of a thumbnail for a video file by calling FFMpeg and returning the thumbnail
 * @param {*} req 
 * @param {*} res 
 * @returns {image/jpeg} The generated thumbnail image.
 */
export const thumbnail = async (req, res) => {
    const { filename } = req.params;
    const videoDir = process.env.VIDEO_DIR;

    if (!videoDir) {
        return res.status(500).json({
            error: "Video directory not found"
        });
    }

    // Reject unsafe filenames and unsupported extensions. <-- smart
    if (
        typeof filename !== "string" ||
        !/^[a-zA-Z0-9][a-zA-Z0-9_.-]*\.mp4$/i.test(filename) ||
        filename.includes("..")
    ) {
        return res.status(400).json({
            error: "Invalid video filename"
        });
    }

    try {
        // Verify the video exists in video folder using consistant env value for directory
        const entries = await fs.readdir(videoDir, {
            withFileTypes: true
        });

        const valid = entries.some(entry =>
            entry.isFile() && entry.name === filename
        );

        if (!valid) {
            return res.status(404).json({
                error: "Video not found"
            });
        }

        const videoPath = path.resolve(videoDir, filename);

        // Generate and retrieve the image bytes.
        const image = await extractThumbnail(videoPath);

        // Return the JPEG image to the client.
        return res.status(200)
            .type("image/jpeg")
            .send(image); //<---this makes the page a picture of the thumbnail, mayb just save and not "send"

    } catch (error) {
        console.error("Thumbnail generation failed:", error);

        return res.status(500).json({
            error: "Unable to generate thumbnail"
        });
    }
};

/**
 * @description Uses FFmpeg to extract the first frame of a video. 
 * @param {string} videoPath  path to the video.
 * @returns {Promise<Buffer>} JPEG image data.
 */
function extractThumbnail(videoPath) {

    return new Promise((resolve, reject) => {

        const outputPath = path.resolve(process.env.CSV_OUTPUT_DIR, "thumbnail.JPEG");

        // child process for ffmpeg to extract a single frame and save to same folder as csv currently
        const child = spawn(ffmpegPath, [
            "-y",
            "-i", videoPath,
            "-frames:v", "1",
            outputPath
        ]);

        child.on("error", reject);

        child.on("close", async (code) => {
            if (code !== 0) {
                return reject(new Error(`FFmpeg exited with code ${code}`));
            }

            try {
                const image = await fs.readFile(outputPath);
                resolve(image);
            } catch (error) {
                reject(error);
            }
        });

    });
}

