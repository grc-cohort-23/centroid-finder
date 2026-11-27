const { test, before, after } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs/promises");
const { once } = require("node:events");
const app = require("./index");

let server;
let baseUrl;

before(async () => {
  server = app.listen(0);
  await once(server, "listening");
  baseUrl = `http://localhost:${server.address().port}`;
});

after(async () => {
  if (server) {
    await new Promise((resolve, reject) => {
      server.close((error) => {
        if (error) reject(error);
        else resolve();
      });
    });
  }
});

for (const code of ["ENOENT", "ENOTDIR"]) {
  test(`returns 404 when checking the video fails with ${code}`, async (t) => {
    t.mock.method(fs, "stat", async () => {
      throw Object.assign(new Error("File not found"), { code });
    });

    const response = await fetch(`${baseUrl}/thumbnail/sample.mp4`);

    assert.equal(response.status, 404);
    assert.deepEqual(await response.json(), {
      error: "Video not found",
    });
  });
}

test("returns 500 when checking the video fails with EACCES", async (t) => {
  t.mock.method(fs, "stat", async () => {
    throw Object.assign(new Error("Permission denied"), {
      code: "EACCES",
    });
  });
  t.mock.method(console, "error", () => {});

  const response = await fetch(`${baseUrl}/thumbnail/sample.mp4`);

  assert.equal(response.status, 500);
  assert.deepEqual(await response.json(), {
    error: "Error reading video file",
  });
});