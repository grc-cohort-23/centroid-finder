const express = require("express");
const path = require("node:path");

const projectRoot = path.resolve(__dirname, "..");

require("dotenv").config({
  path: path.join(projectRoot, ".env"),
});

const app = express();
const port = process.env.PORT || 3000;

app.use(require("./processingRoutes"));
app.use(require("./videoRoutes"));

if (require.main === module) {
  app.listen(port, () => {
    console.log(`Server listening on http://localhost:${port}`);
  });
}

module.exports = app;
