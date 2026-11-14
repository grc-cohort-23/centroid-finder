const fs = require('fs');
const app = require('./app');
const config = require('./config');

// Make sure the output folder exists before any job tries to write to it.
fs.mkdirSync(config.RESULTS_DIR, { recursive: true });

app.listen(config.PORT, (err) => {
  // Express 5 passes startup errors (like the port already being in use) here.
  if (err) {
    console.error(`Could not start server on port ${config.PORT}: ${err.message}`);
    process.exit(1);
  }
  console.log(`Server running at http://localhost:${config.PORT}`);
  console.log(`  videos:  ${config.VIDEO_DIR}`);
  console.log(`  results: ${config.RESULTS_DIR}`);
});
