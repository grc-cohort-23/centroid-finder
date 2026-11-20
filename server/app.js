const express = require('express');
const path = require('path');
const config = require('./config');
const videosRouter = require('./routes/videos');
const thumbnailRouter = require('./routes/thumbnail');
const processRouter = require('./routes/process');

const app = express();

// Serve the raw videos and the result CSVs as static files.
app.use('/videos', express.static(config.VIDEO_DIR));
app.use('/results', express.static(config.RESULTS_DIR));

// API routes
app.use(videosRouter);
app.use(thumbnailRouter);
app.use(processRouter);

// The built React front end (cd client && npm run build). In development,
// the Vite dev server serves it instead and proxies API calls here.
app.use(express.static(path.join(__dirname, '..', 'client', 'dist')));

module.exports = app;
