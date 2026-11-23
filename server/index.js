// Starts the server: reads the settings, connects the real parts, and listens.
const fs = require("fs");
const config = require("./src/config");
const createApp = require("./src/app");
const { JobStore, createProcessorRunner, makeThumbnail } = require("./src/services");

async function start() {
  // Make sure the results and jobs folders exist
  fs.mkdirSync(config.resultsDir, { recursive: true });
  fs.mkdirSync(config.jobsDir, { recursive: true });

  const jobStore = new JobStore(config.jobsDir);

  // Jobs still "processing" from before this start can never finish, so mark them failed
  const unfinished = await jobStore.failUnfinished("Error processing video: server restarted");
  if (unfinished.length > 0) {
    console.log(`Marked ${unfinished.length} unfinished job(s) as error: ${unfinished.join(", ")}`);
  }

  const app = createApp({
    videosDir: config.videosDir,
    resultsDir: config.resultsDir,
    jobStore,
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
}

start().catch((err) => {
  console.error(`Could not start server: ${err.message}`);
  process.exit(1);
});
