/**
 * Unit tests for createProcessorRunner in src/services.js.
 *
 * Instead of real Java, each test writes a tiny fake "java" shell script
 * that prints to stderr and exits with a chosen code. The runner is told
 * to use that script, so these tests are fast and don't need Java installed.
 */
const fs = require("fs");
const os = require("os");
const path = require("path");
const { createProcessorRunner } = require("../src/services");

let tempDir;
let logPath;

beforeEach(() => {
  tempDir = fs.mkdtempSync(path.join(os.tmpdir(), "runner-test-"));
  logPath = path.join(tempDir, "job-1.log");
});

afterEach(() => {
  fs.rmSync(tempDir, { recursive: true, force: true });
});

// Writes a fake "java" program that runs the given shell lines
function fakeJava(lines) {
  const script = path.join(tempDir, "fake-java.sh");
  fs.writeFileSync(script, `#!/bin/sh\n${lines}\n`, { mode: 0o755 });
  return script;
}

const JOB = {
  videoPath: "/videos/ensantina.mp4",
  resultPath: "/results/job-1.csv",
  targetColor: "5C0005",
  threshold: "80",
};

// Runs the processor and resolves with the error it finished with (or null)
function run(command) {
  const runProcessor = createProcessorRunner("/fake/videoprocessor.jar", command);
  return new Promise((resolve) => runProcessor({ ...JOB, logPath }, resolve));
}

test("finishes with no error when the JAR exits with 0", async () => {
  const error = await run(fakeJava("exit 0"));

  expect(error).toBeNull();
});

test("passes the JAR path and job settings as arguments", async () => {
  // The fake prints its arguments to stderr, which goes to the log file
  await run(fakeJava('echo "$@" >&2\nexit 0'));

  expect(fs.readFileSync(logPath, "utf8").trim()).toBe(
    "-jar /fake/videoprocessor.jar /videos/ensantina.mp4 /results/job-1.csv 5C0005 80"
  );
});

test("saves everything the JAR writes to stderr in the log file", async () => {
  await run(fakeJava('echo "WARNING: some JVM notice" >&2\necho "Invalid threshold: 999" >&2\nexit 1'));

  expect(fs.readFileSync(logPath, "utf8")).toBe("WARNING: some JVM notice\nInvalid threshold: 999\n");
});

test("uses the last line the JAR printed as the error reason", async () => {
  const error = await run(fakeJava('echo "WARNING: some JVM notice" >&2\necho "Invalid threshold: 999" >&2\nexit 1'));

  expect(error.message).toBe("Invalid threshold: 999 (exit code 1)");
});

test("ignores blank lines at the end of the log", async () => {
  const error = await run(fakeJava('echo "Input video not found: x.mp4" >&2\necho "" >&2\necho "   " >&2\nexit 1'));

  expect(error.message).toBe("Input video not found: x.mp4 (exit code 1)");
});

test("falls back to the exit code when the JAR printed nothing", async () => {
  const error = await run(fakeJava("exit 3"));

  expect(error.message).toBe("processor exited with code 3");
});

test("finishes with an error, only once, when java can't be started", async () => {
  const runProcessor = createProcessorRunner("/fake/videoprocessor.jar", path.join(tempDir, "no-such-java"));
  const calls = [];

  runProcessor({ ...JOB, logPath }, (error) => calls.push(error));
  // Give both the "error" and "exit" events time to fire
  await new Promise((resolve) => setTimeout(resolve, 200));

  expect(calls).toHaveLength(1);
  expect(calls[0].code).toBe("ENOENT");
});

test("finishes with an error when the log file can't be created", async () => {
  logPath = path.join(tempDir, "missing-folder", "job-1.log");

  const error = await run(fakeJava("exit 0"));

  expect(error.code).toBe("ENOENT");
});
