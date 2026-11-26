// Builds the Express app from its parts.
// Everything it needs is passed in, so tests can pass in fakes.
const express = require("express");
const createRoutes = require("./routes");

function createApp({ videosDir, resultsDir, jobStore, runProcessor, makeThumbnail }) {
  const app = express();

  // Public folders: /videos/ensantina.mp4 and /results/ensantina.mp4.csv
  app.use("/videos", express.static(videosDir));
  app.use("/results", express.static(resultsDir));

  app.get("/", (req, res) => res.send("Salamander server is running"));

  app.use(createRoutes({ videosDir, resultsDir, jobStore, runProcessor, makeThumbnail }));

  return app;
}

module.exports = createApp;