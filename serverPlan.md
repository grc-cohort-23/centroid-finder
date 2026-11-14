## Server Plan

We are building an Express server in `server/` that serves salamander videos and runs our Java processor on them as background jobs. It is done when all four Salamander API endpoints behave exactly as the spec says, tested in Postman and with unit tests.

| Endpoint | What it does | Success response |
| --- | --- | --- |
| `GET /api/videos` | Lists the video files in the videos folder | 200, array of filenames |
| `GET /thumbnail/:filename` | Returns the first frame of a video | 200, JPEG |
| `POST /process/:filename?targetColor=&threshold=` | Starts a processing job in the background | 202, `{ jobId }` |
| `GET /process/:jobId/status` | Reports a job's status | 200, `processing`, `done` with `result`, or `error` |

Videos are also served at `/videos/<name>`, and result CSVs at `/results/<name>`.

## Notes on the Salamander API

<!-- One paragraph per endpoint, in your own words: what it takes in, what it returns -->

## Decisions

Job state goes in SQLite. Videos and result CSVs stay as plain files on disk.

| Decision | Choice | Why |
| --- | --- | --- |
| Job state stored in | SQLite (`better-sqlite3`), one jobs table | Jobs finish asynchronously and can finish at the same time. Each update touches only its own row and survives restarts. A JSON file is rewritten whole on every update, so simultaneous updates can be lost. |
| Videos stored in | Files in the videos folder | The spec lists the folder; a database copy would drift out of sync. |
| Thumbnails made with | `ffmpeg-static` npm package | Grabs the first frame in one command and installs with npm (ffmpeg is not installed system-wide). |
| Result file naming | `<filename>.csv`, e.g. `intro.mp4.csv` | Matches the spec's example. Downside: rerunning a video overwrites the old CSV. |
| How we know a job finished or failed | The Java process's exit code | Exit code 0 means done. A non-zero exit code or a failure to start means error, with Java's error output as the message. |

The jobs table keeps one row per job: job ID, video filename, target color, threshold, status (processing, done or error), result path, error message, and when it was created.

## Project setup

Before anyone writes a route, get the folders, settings and tools in place.

1. Create a videos folder and a results folder at the project root, and keep both out of git.
2. Add four settings to the server's `.env` file: where the videos live (`VIDEO_DIR`), where results go (`RESULTS_DIR`), where the Java jar is (`JAR_PATH`), and which port the server uses (`PORT`).
3. Install the extra packages: SQLite for jobs, ffmpeg for thumbnails, and Jest with Supertest for testing.
4. Build the Java processor into a jar and note its exact name and location.
5. Run the jar once by hand on a sample video to confirm it produces a CSV.

Planned server structure: an entry file that starts the app, one route file per endpoint group, a jobs module that talks to the database, and a runner module that starts Java and records the outcome.

## Architecture
![alt text](<Screenshot 2026-11-09 at 10.51.50 AM.png>)

Each route has one job. Only the runner talks to Java, and only the jobs module talks to SQLite, so a route never touches the database or the jar directly.

## Request flow: POST /process/:filename


## Build phases

Work through these in order; each phase ends with something you can check before moving on.

### Phase 1: Foundation

- [ ] Finish the project setup steps above
- [ ] Get the server starting on the configured port
- [ ] Make videos and result files viewable in the browser by URL

Done when a test video plays in the browser from the server.

### Phase 2: List videos

- [ ] Return the names of the files in the videos folder
- [ ] Return the spec's 500 error if the folder can't be read

Done when Postman shows the list, and pointing at a missing folder returns the error.

### Phase 3: Thumbnails

- [ ] Return the first frame of a requested video as a JPEG image
- [ ] Return the spec's 500 error when that fails

Done when Postman shows the image for a real video and the error for a missing one.

### Phase 4: Job storage

- [ ] Set up the SQLite database and the jobs table
- [ ] Support three actions: create a job, update its status, look a job up by ID
- [ ] Write unit tests for all three

Done when the tests pass.

### Phase 5: Start a job

- [ ] Check that a valid target color and threshold were sent; otherwise return 400
- [ ] Save a new job as processing and reply with its job ID immediately
- [ ] Run the Java processor in the background, then mark the job done or error when it finishes

Done when the request returns a job ID right away and the CSV appears in the results folder once Java finishes.

### Phase 6: Job status

- [ ] Return a job's current status, plus the result link or the error message
- [ ] Return 404 for an unknown job ID

Done when polling moves from processing to done and the result link opens the CSV.

### Phase 7: Polish

- [ ] Run every Postman check in the testing plan
- [ ] Add setup and run instructions to the README

## Testing plan

Unit tests cover the logic without Java or ffmpeg; Postman covers the real end-to-end behavior.

**Unit tests (Jest + Supertest)**

- Checking the target color and threshold: valid, missing and wrongly formatted values
- Creating, updating and looking up jobs, using a temporary test database
- Building the result file path from a video name
- Each route's responses, with the Java run faked so the tests stay fast

**Postman checks**

| Request | Expect |
| --- | --- |
| `GET /api/videos` | 200 with the filenames |
| `GET /thumbnail/<real video>` | 200, image shows |
| `GET /thumbnail/missing.mp4` | 500 with error JSON |
| `POST /process/<real video>?targetColor=ff0000&threshold=120` | 202 with `jobId` |
| `POST /process/<real video>` with no query | 400 |
| `GET /process/<jobId>/status` while running | `processing` |
| Same, after Java finishes | `done` with `result`; the CSV opens |
| `GET /process/not-a-real-id/status` | 404 |

## Open questions

- [ ] Name results `<filename>.csv` (spec) or `<filename>-<jobId>.csv` (no overwrites)?
- [ ] Return 404 instead of 500 when a video doesn't exist, or stick strictly to the spec?
- [ ] When the server restarts, should jobs still marked processing be set to error?
- [ ] Who owns each phase?
