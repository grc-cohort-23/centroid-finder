const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs/promises");
const os = require("node:os");
const path = require("node:path");
const { once } = require("node:events");

let server;
let baseUrl;
let directory;
const originalVideosDirectory = process.env.VIDEOS_DIR;

before(async () => {
  directory = await fs.mkdtemp(
    path.join(os.tmpdir(), "video-list-tests-")
  );

  await fs.writeFile(path.join(directory, "zebra.mp4"), "");
  await fs.writeFile(path.join(directory, "alpha.MOV"), "");
  await fs.writeFile(path.join(directory, "notes.txt"), "");
  await fs.mkdir(path.join(directory, "folder.mp4"));

  process.env.VIDEOS_DIR = directory;

  const app = require("./index");
  server = app.listen(0);
  await once(server, "listening");

  baseUrl = `http://localhost:${server.address().port}`;
});

after(async () => {
  if (server) {
    await new Promise((resolve) => server.close(resolve));
  }

  if (directory) {
    await fs.rm(directory, { recursive: true, force: true });
  }

  if (originalVideosDirectory === undefined) {
    delete process.env.VIDEOS_DIR;
  } else {
    process.env.VIDEOS_DIR = originalVideosDirectory;
  }
});

test("lists video files alphabetically and skips other entries", async () => {
  const response = await fetch(`${baseUrl}/api/videos`);

  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), [
    "alpha.MOV",
    "zebra.mp4",
  ]);
});