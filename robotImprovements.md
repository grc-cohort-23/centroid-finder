# AI-Suggested Improvements

Covers the server (`centroid-finder/`: Express server + Java processor) and the front end
(`centroid-finder-frontend/`: React app). Each item is tagged **[Server]**, **[Frontend]** or **[Both]**.
The AI read the code but did not run it, so confirm each item before changing anything.
Items marked **(high)** looked most valuable.

## API contract the front end depends on (read before changing the server)
Server changes must keep these, or the front end breaks:
- `POST /process/:filename` returns **202** with `{ jobId }`; failures return `{ error }` in the JSON body.
- `GET /process/:jobId/status` returns `{ status: "processing" | "done" | "error", result?, error? }`.
- An unknown job returns **404** with an error message containing "not found". The front end checks that
  text (`useJob.js`), so changing the wording would break it (see Error handling).
- `result` is a path like `/results/<name>.csv`; the front end uses whatever the server returns.
- CSV rows are `seconds,x,y`, no header, and `-1,-1` means "nothing found" (`track.js`).
- `GET /api/videos`, `POST /api/videos` (field `video`, 201 with `{ filename }`), `GET /thumbnail/:filename`,
  `GET /videos/:file`, `GET /results/:file`.
- The upload extension list is duplicated in the front end (`UploadDialog.jsx`).
- In development the front end only works because Vite proxies everything to port 3000 (one origin).

# Required

## Refactoring code
- **(high) [Frontend]** `Workspace.jsx` is 661 lines and does everything. Split it into pieces: the frame
  render loop (a hook), the keyboard shortcuts (a hook), the job card (`JobCard` and `previewHint` already
  live at the bottom of the file), and the settings panel. Move constants out too.
- **[Frontend]** `Workspace` re-renders on every video frame (`setCurrentTime`), so the whole panel
  re-renders with it. After splitting, keep fast-changing state in the small components that need it.
- **[Frontend]** `PreviewPair.jsx` and `Workspace.jsx` repeat the "detect, paint mask, draw dot" logic; share it.
- **[Frontend]** Move tests next to what they test consistently (`humanImprovements.md` mentions this).
- **[Server]** Move the repeated "filename must not contain a path" check (process and thumbnail routes) into
  shared validation middleware.
- **[Server]** Centralize the job status strings (`processing`, `done`, `error`).
- **[Server]** Make `jobs.js` open the database in an init function instead of on `require`.
- **[Server]** Remove dead code: unused `path` import in `app.js`; check whether `JavaCvFrameSource.frameRate()` is used.
- **[Server]** `LargestGroupSelector` re-scans a list the group finder already sorted; simplify.

## Adding tests
- **(high) [Both]** CI only runs the Java tests, and the front-end repo has no CI at all. Add a server job
  (`npm test`) and a front-end job (`npm test`), and run on push as well as PRs.
- **[Frontend]** Only `detection.js` and `track.js` have unit tests. Add tests for `api.js` (error
  parsing, 202 handling), `useJob.js` (retry, not-found, stop on done), `savedJobs.js` (storage unavailable),
  `format.js`, and `draw.js`. Vitest runs in the `node` environment, so component tests would need
  jsdom and Testing Library; decide whether that is worth the setup.
- **[Frontend]** Cypress covers the happy paths; add cases for a failed job, a job that never finishes, an
  upload rejected by the server (409, 413), and a thumbnail that fails to load.
- **[Server]** Java classes with no tests: `VideoSummaryApp`, `JavaCvFrameSource`, `ImageSummaryApp`, `Group`, `Coordinate`.
- **[Server]** `DfsBinaryGroupFinder` edge cases: ragged rows, 1x1 image, all-white image, very large frame (overflow).
- **[Server]** `CsvWriter` output format. Concurrent jobs on one video, job timeout, recovery after a restart, path-traversal attempts.
- **[Both]** The front end reimplements the Java detection in `detection.js` so the preview matches the
  processor. Add a parity test: run the same image through both and compare groups and centroids. This is
  the best protection against the two drifting apart (the overflow bug below is one such case).
- **[Server]** One end-to-end test with a tiny real video and the real JAR (everything is mocked today).

## Improving error handling
- **(high) [Server]** The Java app exits with code 0 on failure, so the server scrapes stdout for a success
  message. Exit non-zero on failure, then simplify the success check in `runner.js`.
- **(high) [Both]** Jobs left as "processing" after a server restart stay that way forever, and the front end
  polls them forever too (it only gives up after 3 *failed* checks, never on a job that keeps saying
  "processing"). Server: mark leftover jobs errored on startup and add a timeout for the Java process.
  Front end: add a maximum wait with a "this is taking too long" message.
- **[Frontend]** `useJob.js` decides a job is missing with `/not found/i.test(err.message)`. That ties
  behavior to the server's wording. Have `api.js` throw an error that carries the HTTP status and check for 404.
- **[Frontend]** `togglePlay` calls `video.play()` without handling the rejected promise (browsers reject it
  in some situations). Handle it so errors do not go unreported.
- **[Frontend]** `loadSavedJob` trusts whatever is in `localStorage`. Validate the saved shape so a corrupted
  entry cannot break the page.
- **[Frontend]** Check the file size in the upload dialog before sending, so a 600 MB file fails instantly
  instead of after uploading.
- **[Server]** Bad or missing filenames return 500 in the process and thumbnail routes; 400/404 is correct.
  This is safe for the front end as long as the JSON `{ error }` body stays (it only checks for 202 and
  reads `body.error`).
- **[Server]** Add a 404 handler and a final Express error-handling middleware.
- **[Server]** Handle a `null` return from `converter.convert` in `JavaCvFrameSource` (corrupt or unsupported video).
- **[Server]** Kill running Java processes and mark their jobs errored when the server shuts down.
- **[Server]** DFS input validation does not match its Javadoc: ragged rows throw `ArrayIndexOutOfBounds`
  instead of `IllegalArgumentException`, and values other than 0/1 are accepted.

## Writing documentation
- **(high) [Server]** Replace the assignment text in the root `README.md` with: what the project is,
  prerequisites (JDK, Maven), how to build the JAR, `.env` variables, how to start the server, how to run tests.
  Link to the front-end repo, since the app needs both.
- **[Frontend]** The README references `docs/videos-page.png` and `docs/preview-page.png`, but the `docs/`
  folder is empty, so the images are broken. Add the screenshots or remove the links.
- **[Frontend]** Add comments to the files with none, such as `Workspace.jsx`, `draw.js`, `NavBar.jsx` and
  `TrackChart.jsx`, covering why they are built the way they are (for example why `render` reads from a
  ref instead of state).
- **[Both]** Document the API contract above in one place (the server's `serverPlan.md` is a good home) and link it from both READMEs.
- **[Both]** Document known differences between the browser preview and the Java processor (video decoding
  can give slightly different colors).
- **[Server]** Document the CSV format, including what `-1,-1` means. Add Javadoc to `VideoProcessor`, `JavaCvFrameSource`, and `CsvWriter`.
- **[Both]** Update `humanImprovements.md` so it says which repo each item belongs to.

# Optional

## Improving performance
- **(high) [Frontend]** While a video plays, every frame copies the full-resolution image out of the canvas
  and runs the whole detection (binarize plus DFS) in the main thread. On 1080p that is about 2 million
  pixels per frame and will drop frames. Options: run detection on a downscaled copy while playing and at
  full size when paused, or move it to a Web Worker. Measure first.
- **[Frontend]** `PreviewPair` re-runs detection on every slider tick; debounce it.
- **[Frontend]** Playback uses a fixed `1/30` second frame step regardless of the video's real frame rate.
- **[Server]** Thumbnails spawn ffmpeg on every request; add caching headers or cache the file.
- **[Server]** Cap the stdout/stderr buffering in `runner.js`, which grows without limit.
- **[Server]** Per-frame allocation in Java (a `Coordinate` object per pixel in the DFS) could be reduced, but only after measuring.

## Hardening security
- **[Server]** Cap the number of concurrent Java jobs so one client cannot exhaust memory.
- **[Server]** Add a `.env.example`. (`.env` is already untracked in both repos, so nothing needs deleting.)
- **[Both]** Run `npm audit` in both repos, and commit the pending `server/package-lock.json` change.
- **[Both]** Deployment gap: the Vite proxy only works in development. The server no longer serves the built
  front end (its `.gitignore` still mentions `client/dist`). If you ever deploy, either serve `dist/` from
  Express (same origin) or add CORS, and note that the video must be served with CORS headers or the canvas
  becomes tainted and `getImageData` fails. Decide whether deployment is in scope at all.
- **[Server]** Decide deliberately whether `/videos` and `/results` should be served publicly.

## Bug fixes
- **(high) [Server]** Result CSVs collide: `runner.js` names the result `<filename>.csv`, so two jobs on the
  same video overwrite each other, and a new job deletes the previous result. Name results per job. The
  front end is safe, since it uses the `result` path the server returns. It also fixes stale CSVs being
  cached by the browser under the same URL.
- **(high) [Server]** Possible integer overflow in `DfsBinaryGroupFinder`: coordinate sums are `int`, so a
  very large frame could give a wrong centroid. The JavaScript copy uses doubles and would not overflow,
  so the two could disagree. Prove it with a test, then use `long`.
- **[Frontend]** Keyboard shortcuts in `Workspace` only skip text inputs. Pressing Space on a focused button
  both clicks it and toggles playback, and the arrow keys on the focused sliders both move the slider and
  step the video. Ignore keys when focus is on any interactive control. (Also, that `useEffect` has no
  dependency list, so it re-registers the listener on every render.)
- **[Frontend]** "App still receives data when the video is not playing" (from `humanImprovements.md`).
  I could not pin down the cause from reading. Reproduce it with the browser Network tab and React DevTools.
  Candidates: `useJob` polling every second while a job is processing (expected, but unbounded), the video
  element's `preload="auto"` continuing to download, and the hover tooltip still sampling pixels while paused.
- **[Server]** The videos folder is not created at startup (only results is), so uploads can fail on a fresh clone.
- **[Server]** `CsvWriter` writes a raw `double`, which can print like `1.0E-4`.

## Other
- **[Both]** The front end shows an indeterminate progress bar because the server reports no progress. A
  `progress` field on the status response (frames done / total) would improve it, but it is a feature, not a fix.
- **[Both]** There is no way to cancel a running job. Same caveat.
- **[Both]** Remove committed binaries that do not need to be tracked from the server repo: `processor/binarized.png`,
  `processor/groups.csv`, `thumb.jpg`, the screenshot. Decide whether both the root and `server/` `package-lock.json` are needed.

# Suggestions I rejected (and why)
- **Authentication, rate limiting, helmet.** Out of scope for a locally run class project. A concurrency cap covers the real risk.
- **Checking uploaded file contents, not just extensions.** Too large for the benefit here.
- **Consolidating the three Java entry points.** `ImageSummaryApp` is original assignment code, so changing it is risky for little gain.
- **Removing `sqrt` per pixel.** The front-end `binarize` already compares squared distances, and the Java version
  would need the same change in step to keep parity. Only worth it if profiling shows it matters.
- **Frame skipping.** It would change the CSV the front end plots.
- **Removing the JUnit jar from the repo.** The README's compile command depends on it.
- **Rewriting the front end in TypeScript or adding a state library.** Far too large for the benefit; splitting `Workspace.jsx` is enough.
