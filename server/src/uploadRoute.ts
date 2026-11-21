import fs from "fs/promises";
import { existsSync } from "fs";
import path from "path";
import { Router, type Request, type Response } from "express";
import multer, { MulterError, type FileFilterCallback } from "multer";
import { v4 as uuidv4 } from "uuid";
import type { AppConfig } from "./app.js";
import { VIDEO_EXTENSIONS, probeVideo, type VideoInfo } from "./videoInfo.js";

// Biggest video we accept: 2 GB (a few minutes of 4K, or a long 1080p video)
export const MAX_UPLOAD_BYTES = 2 * 1024 * 1024 * 1024;

// What browsers send as the type of .mp4 and .mov files
const VIDEO_MIME_TYPES = ["video/mp4", "video/quicktime"];

// An upload problem that already knows its HTTP status, like 409 for a name that's taken
class UploadError extends Error {
    status: number;
    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

// Makes the uploaded file's name safe to save:
// keep only the name (no folders), and swap odd characters for "_".
// undefined if nothing usable is left, or it would be a hidden file like ".mp4".
export function safeFilename(originalName: string): string | undefined {
    const name = path.basename(originalName.replaceAll("\\", "/")).replace(/[^A-Za-z0-9._ -]/g, "_");
    if (name === "" || name.startsWith(".")) return undefined;
    return name;
}

// POST /api/videos (multipart form, the file in the field "video")
// Saves an uploaded video in the videos folder and sends back its VideoInfo.
export function uploadRoute(config: AppConfig): Router {
    const router = Router();

    // multer reads the multipart form and writes the file to disk while it arrives,
    // so a 2 GB video never has to fit in memory
    const upload = multer({
        storage: multer.diskStorage({
            // a function (not just the folder's name), so multer doesn't try to make the folder when the app starts
            destination: (req, file, callback) => callback(null, config.videoDir),
            // Save under a temporary name first (".part" isn't a video, so GET /api/videos skips it).
            // We give it its real name only after the whole file has arrived.
            filename: (req, file, callback) => {
                callback(null, `${safeFilename(file.originalname)}.${uuidv4()}.part`);
            },
        }),
        limits: { fileSize: config.maxUploadBytes ?? MAX_UPLOAD_BYTES, files: 1 },
        // Runs before anything is saved, so we can refuse a file without writing it
        fileFilter: (req: Request, file: Express.Multer.File, callback: FileFilterCallback) => {
            const name = safeFilename(file.originalname);
            if (!name) {
                callback(new UploadError(400, "That file name can't be used"));
                return;
            }
            const extension = path.extname(name).toLowerCase();
            if (!VIDEO_EXTENSIONS.includes(extension) || !VIDEO_MIME_TYPES.includes(file.mimetype)) {
                callback(new UploadError(415, "Only .mp4 and .mov videos can be uploaded"));
                return;
            }
            if (existsSync(path.join(config.videoDir, name))) {
                callback(new UploadError(409, "A video with that name already exists"));
                return;
            }
            callback(null, true);
        },
    }).single("video");

    router.post("/api/videos", (req: Request, res: Response) => {
        upload(req, res, async (uploadError: unknown) => {
            if (uploadError) {
                sendUploadError(res, uploadError, config.maxUploadBytes ?? MAX_UPLOAD_BYTES);
                return;
            }
            if (!req.file) {
                res.status(400).json({ error: "No video sent. Put the file in the form field \"video\"." });
                return;
            }

            const tempPath = req.file.path;
            // fileFilter already checked this name, so it's never undefined here
            const filename = safeFilename(req.file.originalname) ?? req.file.filename;
            const finalPath = path.join(config.videoDir, filename);

            try {
                // link gives the file its real name, and fails (EEXIST) if that name is taken,
                // so two uploads with the same name at the same time can't overwrite each other
                await fs.link(tempPath, finalPath);
            } catch (error) {
                const taken = error instanceof Error && "code" in error && error.code === "EEXIST";
                if (!taken) console.error("Could not save upload:", error);
                res.status(taken ? 409 : 500).json({
                    error: taken ? "A video with that name already exists" : "Error saving video",
                });
                return;
            } finally {
                // remove the temporary name (the file stays under its real name if link worked)
                await fs.rm(tempPath, { force: true });
            }

            const video: VideoInfo = { filename, status: "not-processed", ...(await probeVideo(finalPath)) };
            // 201 = a new file was created
            res.status(201).json(video);
        });
    });

    return router;
}

// Turns a multer or UploadError error into a JSON error response
function sendUploadError(res: Response, error: unknown, maxBytes: number): void {
    if (error instanceof UploadError) {
        res.status(error.status).json({ error: error.message });
    } else if (error instanceof MulterError && error.code === "LIMIT_FILE_SIZE") {
        // 413 = too big
        const maxMb = Math.round(maxBytes / (1024 * 1024));
        res.status(413).json({ error: `Video is too big (the limit is ${maxMb} MB)` });
    } else if (error instanceof MulterError) {
        // like a file in a different field, or more than one file
        res.status(400).json({ error: `Bad upload: ${error.message}` });
    } else {
        console.error("Upload failed:", error);
        res.status(500).json({ error: "Error saving video" });
    }
}
