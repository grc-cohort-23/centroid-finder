import { Router } from 'express';
import processVideo from '../controllers/processVideo.js';
import getJob from '../controllers/getJob.js';
import getVideos from '../controllers/getVideos.js';
import getThumbnail from '../controllers/getThumbnail.js';

const appRouter = Router();

appRouter.get('/status', (req, res) => {
    res.json({ message: "Running" });
});

appRouter.get("/api/videos", getVideos); 
appRouter.post("/process/:filename", processVideo);
appRouter.get("/process/:jobid/status", getJob);
appRouter.get("/thumbnail/:filename", getThumbnail); 

export default appRouter;