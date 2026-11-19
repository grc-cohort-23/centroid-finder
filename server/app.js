import express from 'express';
import { config } from './config.js';
import videosRouter from './routes/videos.js';
import thumbnailRouter from './routes/thumbnail.js';
import processRouter from './routes/process.js';

const app = express();

app.use(express.json());
app.use('/videos', express.static(config.videosDir));
app.use('/results', express.static(config.resultsDir));

app.use(videosRouter);
app.use(thumbnailRouter);
app.use(processRouter);

export default app;