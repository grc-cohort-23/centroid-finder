import { after, before, test } from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs/promises";
import os from "node:os";
import path from "node:path";
import app from "../app.js";

let server;
let baseUrl;
let tempDir;
let originalVideoDir;

before(async () => {
  originalVideoDir = process.env.VIDEO_DIR;
  tempDir = await fs.mkdtemp(path.join(os.tmpdir(), "sal salamander-test-"));
  process.env.VIDEO_DIR = tempDir;

  server = app.listen(0);
  await new Promise((resolve) => server.once("listening", resolve));

  const { port } = server.address();
  baseUrl = `http://127.0.0.1:${port}`;
});

after(async () => {
  await new Promise((resolve, reject) => {
    server.close((error) => (error ? reject(error) : resolve()));
  });

  await fs.rm(tempDir, { recursive: true, force: true });

  if (originalVideoDir === undefined) {
    delete process.env.VIDEO_DIR;
  } else {
    process.env.VIDEO_DIR = originalVideoDir;
  }
});

test("GET / returns the home page", async () => {
  const response = await fetch(`${baseUrl}/`);

  assert.equal(response.status, 200);
  assert.equal(await response.text(), "Hello from home page");
});

test("GET /health returns the server health page", async () => {
  const response = await fetch(`${baseUrl}/health`);
  const body = await response.text();

  assert.equal(response.status, 200);
  assert.match(body, /Server is live!/);
});

test("GET /api/videos returns an empty list when the video directory is empty", async () => {
  const response = await fetch(`${baseUrl}/api/videos`);
  const body = await response.json();

  assert.equal(response.status, 200);
  assert.equal(body.success, true);
  assert.deepEqual(body.availableVideos, []);
});

test("POST /process/:filename rejects missing processing parameters", async () => {
  const response = await fetch(`${baseUrl}/process/example.mp4`, {
    method: "POST",
  });
  const body = await response.json();

  assert.equal(response.status, 400);
  assert.equal(body.success, false);
});