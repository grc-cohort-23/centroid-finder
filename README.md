# centroid-finder

Centroid Finder tracks colored objects in video. You give it a video, a target
color and a threshold. It binarizes each frame (pixels close to the target color
become 1, everything else 0), finds the connected groups of 1s, and reports the
centroid of the largest group per frame. The original use case is tracking
salamanders.

The project has three parts:

| Part | Folder / repo | What it does |
| --- | --- | --- |
| Processor | [`processor/`](processor/) | Java app that does the image and video analysis and packages into one runnable JAR. |
| Server | [`server/`](server/) | Node.js (Express) API that stores uploaded videos, runs the JAR as a job, and serves results. |
| Front end | `centroid-finder-frontend` (separate repo, **link: TODO**) | React app for uploading videos, previewing the mask and starting jobs. |

The web app needs the server **and** the front end running together.

## Prerequisites

- JDK 21 or newer (the processor compiles with `maven.compiler.release` 21)
- Maven 3.x
- Node.js and npm (for the server)

## Build the JAR

From the repo root:

```
cd processor
mvn package
```

This runs the tests and produces `processor/target/videoprocessor.jar`, a
self-contained JAR (main class `VideoSummaryApp`). Use `mvn package -DskipTests`
to skip the tests.

## Configure the server

Create a `.env` file in the **repo root** (not inside `server/`). It is
git-ignored. All variables are optional; paths are relative to the repo root.

| Variable | Default | Meaning |
| --- | --- | --- |
| `PORT` | `3000` | Port the server listens on. |
| `VIDEO_DIR` | `videos` | Folder holding uploaded videos. |
| `RESULTS_DIR` | `results` | Folder where job output is written. |
| `JAR_PATH` | `processor/target/videoprocessor.jar` | The JAR built above. |
| `DB_PATH` | `jobs.db` | SQLite file that tracks jobs. |
| `MAX_UPLOAD_MB` | `500` | Largest video accepted on upload, in MB. |

Example `.env`:

```
PORT=3000
VIDEO_DIR=videos
RESULTS_DIR=results
```

## Start the server

```
cd server
npm install
npm start
```

The server then listens on `http://localhost:3000` (or your `PORT`). Use
`npm run dev` to restart automatically when files change. Make sure the JAR has
been built first, since the server runs it for each job.

Endpoints:

- `GET /api/videos` lists uploaded videos
- `POST /api/videos` uploads a video
- `GET /thumbnail/:filename` returns a video's thumbnail
- `POST /process/:filename` starts a processing job
- `GET /process/:jobId/status` checks a job's status

## Run the tests

```
# Processor (Java, JUnit)
cd processor
mvn test

# Server (Node, Jest)
cd server
npm test
```

## Running the processor on its own

You can also run the processor directly on an image, without the server. From
`processor/`:

```
mvn package
java -cp target/videoprocessor.jar ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

This writes `binarized.png` and `groups.csv`, which should match the files in
`processor/sampleOutput/`. The arguments are the input image, the target color
as hex RGB, and the color-distance threshold.

## Course notes

This project began as a class assignment. The original assignment instructions
and AI-use acknowledgements are kept in [ASSIGNMENT.md](ASSIGNMENT.md).
