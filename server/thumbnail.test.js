const test = require("node:test");
const assert = require("node:assert/strict");
const app = require("./index");

test("thumbnail route returns a JPEG for an existing video", async () => {
  const server = app.listen(0);

  try {
    const port = server.address().port;
    const response = await fetch(
      `http://localhost:${port}/thumbnail/sample.mp4`
    );

    assert.equal(response.status, 200);
    assert.match(response.headers.get("content-type"), /^image\/jpeg/);

    const image = Buffer.from(await response.arrayBuffer());

    // JPEG files begin with FF D8.
    assert.equal(image[0], 0xff);
    assert.equal(image[1], 0xd8);
  } finally {
    server.close();
  }
});

test("thumbnail route returns 404 for a missing video", async () => {
  const server = app.listen(0);

  try {
    const port = server.address().port;
    const response = await fetch(
      `http://localhost:${port}/thumbnail/nonexistent.mp4`
    );

    assert.equal(response.status, 404);

    const body = await response.json();
    assert.equal(body.error, "Video not found");
  } finally {
    server.close();
  }
});
