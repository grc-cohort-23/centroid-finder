# centroid-finder

A video-processing pipeline that tracks a colored object (originally a salamander) through a video. A **Java processor** finds the centroid of the largest group of pixels near a target color in sampled video frames and writes the positions to a CSV file. An **Express server** exposes that over HTTP and tracks each job's state. The companion React app, [centroid-finder-frontend](https://github.com/sabinMas/centroid-finder-frontend), is the UI: it lists videos, previews a frame live while you pick a color and threshold, and starts a processing job through this server's API.

## Course AI rules (read first)

This is graded coursework, so these rules apply to every commit, human or AI:

- AI is **not allowed** to generate the implementations of the classes. AI may help write test cases, but a person must be actively involved in writing them.
- **Do not mix human and AI work in one commit.**
- **Every AI-assisted commit message must start with `AI Used`.**
- The original assignment text, including these rules in full, is kept in [docs/part1-instructions.md](docs/part1-instructions.md).

## How it works

When you `POST` a video filename to `/process/:filename`, the server creates a job record and immediately starts the Java processor as a background process (`java -jar videoprocessor.jar`). The processor samples one frame every 0.5 seconds, turns each frame into a black-and-white image (pixels within the threshold distance of the target color become white), groups the connected white pixels, and writes the centroid of the largest group to a CSV file in `RESULTS_DIR`. The server tracks the job in `JOBS_DIR` and updates its status when the process exits. Poll `/process/:jobId/status` until you get `"done"` and a `result` path, or `"error"` with a reason.

## Prerequisites

- **Java 21** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **Node 20 LTS or newer** (`node -v`)
- **ffmpeg on your `PATH`** (`ffmpeg -version`). The server uses it to make thumbnails (`GET /thumbnail/:filename`). The processor JAR bundles its own video decoding through JavaCV, so ffmpeg is not needed to process a video.

## Getting started

### 1. Build the processor JAR

```sh
cd processor
mvn -DskipTests package
cd ..
```

Output: `processor/target/videoprocessor.jar`

### 2. Configure the server

```sh
cp .env.example .env
# edit .env if needed. Every variable has a default
```

### 3. Install dependencies and start the server

```sh
cd server
npm ci
node index.js
```

The server listens on `http://localhost:3000` by default. There is no `npm start` script yet, so use `node index.js`. To use the UI, start the [frontend](https://github.com/sabinMas/centroid-finder-frontend) too. It runs on `http://localhost:5180` and forwards API calls to port 3000.

## Configuration

All options are read from `.env` in the repo root (see `.env.example`). Every variable has a default.

| Variable | Default (no `.env`) | `.env.example` value | Description |
|---|---|---|---|
| `PORT` | `3000` | `3000` | Port the server listens on. |
| `VIDEOS_DIR` | `videos/` | `processor/sampleInput` | Directory of input videos. Also served statically at `/videos/`. |
| `RESULTS_DIR` | `results/` | `results` | Directory where the processor writes `.csv` output. Served at `/results/`. |
| `JOBS_DIR` | `jobs/` | `jobs` | Directory where per-job JSON state files are stored. |
| `JAR_PATH` | `processor/target/videoprocessor.jar` | `processor/target/videoprocessor.jar` | Path to the processor JAR that the server runs. |

Relative paths are resolved from the repo root. The server creates `RESULTS_DIR` and `JOBS_DIR` on start-up.

## API

| Method | Path | Success | Errors |
|---|---|---|---|
| `GET` | `/api/videos` | `200` JSON array of filenames (`.mp4 .mov .avi .mkv .webm` only) | `500` |
| `GET` | `/thumbnail/:filename` | `200` `image/jpeg` of the first frame | `400` bad filename, `404` not found, `500` ffmpeg failed |
| `POST` | `/process/:filename?targetColor=RRGGBB&threshold=N` | `202` `{ "jobId": "<uuid>" }` | `400` bad or missing parameters, `404` video not found, `500` could not start |
| `GET` | `/process/:jobId/status` | `200` job state (see below) | `404` unknown job, `500` |
| `GET` | `/results/<jobId>.csv` | `200` the CSV file (static) | `404` |

Every error response is JSON of the form `{ "error": "<message>" }`.

Example responses:

```jsonc
// GET /api/videos
["salamander1.mp4", "salamander2.mov"]

// POST /process/salamander1.mp4?targetColor=FFA200&threshold=164   (202)
{ "jobId": "3f2b7c0e-8a0b-4a53-9d54-6f6b6b2d6c11" }

// POST with a bad color                                              (400)
{ "error": "Invalid filename, targetColor (RRGGBB hex) or threshold (whole number)." }

// POST for a video that does not exist                               (404)
{ "error": "Video not found" }

// GET /process/<jobId>/status, while the processor runs
{ "status": "processing" }

// ...when it finished successfully
{ "status": "done", "result": "/results/3f2b7c0e-8a0b-4a53-9d54-6f6b6b2d6c11.csv" }

// ...when it failed (the text is the processor's stderr)
{ "status": "error", "error": "Error processing video: Error opening video: videos/missing.mp4 (Failed to open video: videos/missing.mp4)" }

// GET /process/not-a-real-id/status                                  (404)
{ "error": "Job ID not found" }
```

The routes, query parameters, status codes and JSON shapes above are a contract with the frontend. Don't change them without changing the frontend too.

## The processor (Java)

Command line (the argument order is fixed, because the server relies on it):

```sh
java -jar processor/target/videoprocessor.jar <input_video> <output_csv> <hex_target_color> <threshold>
# example
java -jar processor/target/videoprocessor.jar processor/sampleInput/ball.mp4 out.csv FF0000 100
```

**CSV format.** One `timestamp,x,y` row per sampled frame: the timestamp is in seconds from the start of the video with 2 decimals, and `x,y` is the centroid of the largest matching group in pixels. If no pixel matches in a frame, the row is `-1,-1`:

```csv
0.00,320,240
0.50,331,244
1.00,-1,-1
```

**Exit codes.** The server treats any non-zero code as a failed job and stores whatever the processor wrote to stderr as the job's error text.

| Code | Meaning |
|---|---|
| `0` | Success, the CSV was written |
| `1` | Wrong number of arguments |
| `2` | The threshold is not an integer |
| `3` | The target color is not a 6-digit `RRGGBB` hex string |
| `4` | The video could not be opened (missing file, or not a video) |
| `5` | Decoding the video's frames failed part way through |
| `6` | The output CSV could not be written |

`ImageSummaryApp` is the older single-image version of the pipeline from Part 1 (`java -cp ... ImageSummaryApp <image> <hex_target_color> <threshold>`). Its output files, `binarized.png` and `groups.csv`, are in `processor/sampleOutput/`.

## Running the tests

```sh
# Java processor (JUnit 5 via Maven)
cd processor && mvn -B test

# Node server (Node's built-in node:test runner, no extra framework)
cd server && npm ci && npm test
```

GitHub Actions runs both suites in parallel on every pull request to `main` and on pushes to `main`. See [`.github/workflows/run-tests.yml`](.github/workflows/run-tests.yml). `processor/sampleInput/ball.mp4` is a tiny test video that is checked in on purpose (other videos are gitignored) and is used by the end-to-end test.

## Project structure

```
centroid-finder/
├── processor/                       # Java 21 Maven project (default package)
│   ├── pom.xml                      #   builds target/videoprocessor.jar
│   ├── src/main/java/               #   VideoSummaryApp, ImageSummaryApp, and the pipeline classes
│   ├── src/test/java/               #   JUnit tests, including VideoSummaryAppEndToEndTest
│   ├── sampleInput/                 #   ball.mp4 (test fixture), squares.jpg
│   └── sampleOutput/                #   expected output for squares.jpg
├── server/                          # Express API (Node, ES modules)
│   ├── index.js                     #   start-up: creates the results/jobs dirs, listens
│   ├── app.js                       #   builds the Express app (used by index.js and tests)
│   ├── config.js                    #   reads .env and resolves paths
│   ├── routes/
│   │   ├── videos.js                #   GET /api/videos
│   │   ├── thumbnail.js             #   GET /thumbnail/:filename
│   │   └── process.js               #   POST /process/:filename, GET /process/:jobId/status
│   ├── lib/
│   │   ├── jobStore.js              #   read and write job JSON files
│   │   ├── jobRunner.js             #   starts java -jar and tracks how it ends
│   │   └── validate.js              #   input validators
│   └── test/
│       ├── routes.test.js           #   HTTP tests (supertest)
│       ├── validate.test.js
│       └── jobRunner.test.js        #   uses a fake child process, no Java needed
├── .github/workflows/run-tests.yml  # CI
├── docs/part1-instructions.md       # the original Part 1 assignment text
├── .env.example
├── videos/  results/  jobs/         # runtime folders (gitignored)
└── *.md                             # plan, notes and improvement write-ups
```

`server/lib/jobRunner.js` and its test arrive with the job-runner refactor (B-R3). This README describes the project once that has merged.

## Docs and planning

- [TASKS.md](TASKS.md): the Wave 5 task breakdown and who owns which files
- [priorityImprovements.md](priorityImprovements.md): the improvements we chose and the backlog
- [robotImprovements.md](robotImprovements.md) and [humanImprovements.md](humanImprovements.md): the Wave 3 and Wave 2 improvement reviews
- [plan.md](plan.md), [serverPlan.md](serverPlan.md), [notes.md](notes.md): earlier planning notes
- [docs/part1-instructions.md](docs/part1-instructions.md): the original Part 1 assignment
