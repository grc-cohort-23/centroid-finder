import express from 'express';
import videosRouter from './routes/videos.js';

const app = express();

app.use(express.json());
app.use(videosRouter);


export default app;