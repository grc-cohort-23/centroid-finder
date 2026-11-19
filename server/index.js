// Starts the server: reads the settings, connects the real parts, and listens.
const fs = require("fs");
const config = require("./src/config");
const createApp = require("./src/app");
const { JobStore, createProcessorRunner, makeThumbnail } = require("./src/services");

// Make sure the results folder exists
fs.mkdirSync(config.resultsDir, { recursive: true });
fs.mkdirSync(config.resultsDir, { recursive: true });
fs.mkdirSync(config.jobsDir, { recursive: true });

const app = createApp({
  videosDir: config.videosDir,
  resultsDir: config.resultsDir,
  jobStore: new JobStore(config.jobsDir),
  runProcessor: createProcessorRunner(config.jarPath),
  makeThumbnail,
});

app.listen(config.port, (err) => {
    if (err) {
    console.error(`Could not start server on port ${config.port}: ${err.message}`);
    process.exit(1);
  }
  console.log(`Server listening on http://localhost:${config.port}`);
  console.log(`Videos folder:  ${config.videosDir}`);
  console.log(`Results folder: ${config.resultsDir}`);
});