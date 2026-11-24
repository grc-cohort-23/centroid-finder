import express, { type Express } from "express";
import type { JobStore } from "./jobStore.js";
import type { runInBackground } from "./jarRunner.js";
import { processRoute } from "./processRoute.js";
import {statusRoute} from "./statusRoute.js";
import { jobsRoute } from "./jobsRoute.js";
import { videoDetailsRoute } from "./videoDetailsRoute.js";
import { frameRoute } from "./frameRoute.js";
import { uploadRoute } from "./uploadRoute.js";
import { healthRoute } from "./healthRoute.js";
import { videosRoute } from "./videosRoute.js";
import { thumbnailRoute } from "./thumbnailRoute.js";


// The folders the app needs. index.ts reads them from .env.
export type AppConfig ={
    videoDir: string;
    resultsDir: string;
        jarPath: string;
    store: JobStore;
    // only tests set this, so they can use a fake instead of really starting java
    run?: typeof runInBackground;
    // only tests set this, so they can try the "too big" error without a 2 GB file
    maxUploadBytes?: number;

};

// Builds the app and its routes. index.ts is what starts it.
export function createApp(config: AppConfig): Express{
    const app = express();

    app.use("/videos", express.static(config.videoDir));
    app.use("/results", express.static(config.resultsDir));
    app.use(processRoute(config));
    app.use(statusRoute(config));
    app.use(jobsRoute(config));
    app.use(videoDetailsRoute(config));
    app.use(frameRoute(config));
    app.use(uploadRoute(config));
    app.use(healthRoute());
    app.use(videosRoute(config));
    app.use(thumbnailRoute(config));

    // Test route
    app.get("/", (req, res) => {
        res.send("Hello from the server!");
    });

    return app;
}