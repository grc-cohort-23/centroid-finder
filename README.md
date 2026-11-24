# Centroid Finder

Tracks a colored object through a video. Give it a video and a target color, and it finds the largest patch of that color in every frame and writes where its center (centroid) is over time, as a CSV of `seconds,x,y`.

The project has two parts:
- **`processor/`**: a Java program that does the video processing and builds `videoprocessor.jar`.
- **`server/`**: an Express + TypeScript server that implements the [Salamander API](https://github.com/auberonedu/salamander-api) by running the JAR, and keeps track of jobs in a Neon (PostgreSQL) database.

## How it works

For every frame of the video, the processor:
1. **Binarizes the frame.** Each pixel is compared to the target color using Euclidean distance in RGB. Pixels within the threshold become white (1), and everything else becomes black (0).
2. **Finds connected groups.** A depth-first search finds groups of white pixels that touch up, down, left, or right.
3. **Picks the largest group** and takes its centroid (the average x and y of its pixels).
4. **Writes one CSV row**: `seconds,x,y`. If no group is found, the row is `seconds,-1,-1`.

Frames are processed on several threads at once, but the rows are still written in frame order. Groups whose centroid is in the top 30 rows of the frame are ignored, because the salamander videos have a dark strip along the top edge (see `TOP_MARGIN` in `VideoSummaryApp`).

The server never does any of this itself. It starts the JAR in the background, saves the job in the database, and marks it done or failed when the JAR exits.

## Project structure

```
centroid-finder/
├── processor/          Java video processor (Maven project)
│   ├── src/main/       program code
│   ├── src/test/       JUnit 5 tests
│   ├── sampleInput/    a sample video and image
│   └── sampleOutput/   expected output for the sample image
├── server/             Express + TypeScript server
│   ├── src/            routes, job stores, and their tests
│   └── schema.sql      the database's jobs table
├── .env.example        the settings the server needs (copy it to .env)
├── plan.md             design of the video processor
└── serverPlan.md       design of the server
```

## Requirements

- Java 21 and Maven
- Node.js
- ffmpeg, which the server uses for thumbnails, frames, and video details. On a Mac: `brew install ffmpeg`

## Video processor

All of these commands are run from `processor/`.

### Build

```
mvn package
```

This runs all the tests, then builds `target/videoprocessor.jar`. The JAR is large because it includes FFmpeg for every operating system, so it isn't committed.

### Run

```
java -jar target/videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>
```

Example, tracking the red-orange balloon in the sample video:

```
java -jar target/videoprocessor.jar sampleInput/bright_balloon_sample_video.mp4 output.csv FF4500 100
```

Or run it with Maven, without building the JAR:

```
mvn compile exec:java -Dexec.args="sampleInput/bright_balloon_sample_video.mp4 output.csv FF4500 100"
```

| Argument | Meaning |
|---|---|
| `inputPath` | the video file to read |
| `outputCsv` | where to write the CSV |
| `targetColor` | the color to track, as 6 hex digits in RRGGBB format (example: `FF4500`) |
| `threshold` | how close a pixel's color must be to the target color to count, as a whole number 0 or higher |

### Single images

`ImageSummaryApp` runs the same steps on one image. It writes `binarized.png` (the black-and-white image) and `groups.csv` (one `size,x,y` row per group, largest first):

```
mvn compile
java -cp target/classes io.github.nadia5129.centroidfinder.ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

The output should match the files in `sampleOutput/`.

## Server

### Setup

1. **Build the JAR** (see [Video processor](#video-processor)). The server runs this file to process videos.
2. **Make your `.env` file** in the project root, then fill in the values:
   ```
   cp .env.example .env
   ```
   | Setting | Meaning |
   |---|---|
   | `DATABASE_URL` | the Neon connection string. Get it from the team privately, and never commit it. |
   | `PORT` | the port the server runs on (3000 by default) |
   | `VIDEOS_DIR` | the full path to the folder with the videos, like `.../centroid-finder/processor/sampleInput` |
   | `RESULTS_DIR` | the full path to the folder the result CSVs go in (it's made for you if it doesn't exist) |
   | `JAR_PATH` | the full path to `processor/target/videoprocessor.jar` |
3. **Install the packages** (from `server/`), once after cloning or pulling:
   ```
   npm install
   ```
4. **Optional: check the database connection** (from `server/`):
   ```
   npx tsx src/checkDb.ts
   ```
   The `jobs` table already exists in Neon. `server/schema.sql` has its `CREATE TABLE`, in case it ever needs to be made again (run it in Neon's SQL Editor).

### Run

From `server/`:

| Command | What it does |
|---|---|
| `npm run dev` | starts the server and restarts it when a file changes |
| `npm run build` then `npm start` | compiles the TypeScript into `dist/`, then runs it |

Then open `http://localhost:3000/api/health` to check it's up.

### Endpoints

From the Salamander API:

| Method and URL | What it does | Errors |
|---|---|---|
| `GET /api/videos` | lists the video file names in the videos folder | 500 |
| `GET /thumbnail/{filename}` | the video's first frame as a JPEG | 500 |
| `POST /process/{filename}?targetColor=ff0000&threshold=120` | starts processing a video, and sends back `{ "jobId": "..." }` with a 202 right away | 400, 500 |
| `GET /process/{jobId}/status` | `processing`, `done` (with a `result` link to the CSV), or `error` (with an `error` message) | 404, 500 |
| `GET /videos/{filename}` | the video file itself | 404 |
| `GET /results/{jobId}.csv` | a finished job's CSV (`seconds,x,y` rows) | 404 |

`POST /process` checks its input before starting a job, and sends a 400 if:
- `targetColor` isn't 6 hex digits (like `ff0000`)
- `threshold` isn't a whole number from 0 to 2147483647
- the file isn't a `.mp4` or `.mov`, or isn't in the videos folder

Extra endpoints for the frontend:

| Method and URL | What it does | Errors |
|---|---|---|
| `GET /api/videos/details` | each video with its status (`not-processed`, `processing`, `tracked`, or `error`), length, and size | 500 |
| `POST /api/videos` | uploads a video (form field `video`, up to 2 GB) | 400, 409 name taken, 413 too big, 415 not a video |
| `GET /frame/{filename}?t=2.5` | the frame at that many seconds, as a PNG | 400, 404, 500 |
| `GET /api/jobs` | every job, newest first | 500 |
| `GET /api/jobs/{jobId}` | one job | 404, 500 |
| `GET /api/health` | `{ "ok": true, "version": "..." }`, to check the server is up | none |

## Testing

| Part | Command | Notes |
|---|---|---|
| Video processor | `mvn test` (from `processor/`) | JUnit 5, using fakes instead of mocking libraries |
| Server | `npm test` (from `server/`) | Vitest. Uses a fake job store and a fake JAR runner, so it doesn't need the database or the JAR, but some tests use ffmpeg. |

GitHub Actions runs the Java tests on every pull request to `main`.

## Design docs

- [`plan.md`](plan.md): how the video processor is designed, and why
- [`serverPlan.md`](serverPlan.md): every endpoint, the design decisions (Express, TypeScript, Neon, ffmpeg), and open questions
- [`options.md`](options.md): the video libraries we compared, and why we chose JavaCV
- [`processor/docs/progress-parallel.md`](processor/docs/progress-parallel.md): how the parallel speed-up was built and checked

## Team

Paris, Nadia, and Jessica.

## License

[MIT](LICENSE)
