# Robot Improvements

Suggested improvements from AI reviews of the code (including GitHub Copilot), trimmed to the ones we think are best. These are directions to look into, not exact fixes.

## Bugs and error handling

1. **`POST /process` doesn't check its input.**
   - `threshold` goes through `Number()`, so `"abc"` becomes `NaN` and `"12.5"` becomes a decimal. Either one makes the database insert fail, so the client gets a 500 when it should get a 400.   
   - `targetColor` isn't checked against the 6-hex-digit format.
   - **Direction:** check both values in the route, using the same rules as `ArgumentParser`.
   - Also check that the video exists before creating a job. Right now a missing file only shows up later as "JAR exited with code 1".
   - **"Exists" isn't enough.** A folder also exists (`/process/..`), and `processRoute.ts` never checks the extension, so `notes.txt` would start a JAR.
     - **Direction:** use `fs.stat` + `isFile()`, and reuse `isVideoFile` from `videoInfo.ts`. Send a 400, since the spec only lists 202 / 400 / 500 for this route.
2. **Jobs can get stuck on `processing`.** If the server restarts while a JAR is running, nothing ever calls `finishJob`.
   - **Direction:** mark old `processing` jobs as errors when the server starts, or add a timeout.
3. **The JAR's real error message is lost.** `stdio: "ignore"` throws away its stderr, so a failed job only says "exited with code 1".
   - **Direction:** capture stderr into a log file, or save the last line of it into the job's `error`.
   - **Careful: `job.error` is sent to the client.** `statusRoute.ts` returns it as-is, so raw stderr (stack traces, server paths) would reach the frontend.
     - **Direction:** log the full stderr on the server, and save only a short, safe message in the job.
4. **`onExit` can run twice.** Sometimes both the `exit` and `error` events fire. Only the first call should update the job.
5. **Java: write errors are silent.** `PrintWriter` in `CSVOutputWriter` never throws, so a full disk can still end with exit code 0 and a broken CSV.
   - **Direction:** call `checkError()` before closing, and exit with 1 if it returns true.
6. **Java: `ImageSummaryApp` hides its errors.**
   - It catches plain `Exception` in 3 places. Catch the specific exceptions instead.
   - `ImageIO.read` returns `null` (it doesn't throw) for a file that isn't an image, so the program crashes with a `NullPointerException` a few lines later, when it uses the image.
   - Bad arguments print a message and `return`, so the program still exits with code 0, as if it worked. The usage message also goes to `System.out` instead of `System.err`.
   - **Direction:** check for `null` after `ImageIO.read`, and exit with 1 on bad input like `VideoSummaryApp` does.
7. **The server can crash or hang.**
   - There's no `pool.on("error")` listener, so a dropped Neon connection can crash the process.
   - There's no Express error handler.
   - There's no graceful shutdown (`pool.end()`).
8. **Half-written CSVs are already public.** The CSV is written straight into `/results`, so it can be downloaded while the JAR is still running, or after it fails.
   - **Direction:** write to a temporary name and rename it when it's done. `uploadRoute` already does this with `.part` files.
9. **`index.ts` doesn't check `DATABASE_URL`.** It stops if `VIDEOS_DIR`, `RESULTS_DIR` or `JAR_PATH` is missing, but not `DATABASE_URL`.
   - Without it, `pg` quietly tries a database on `localhost`, so the server starts fine and then every job request fails with a 500.
   - **Direction:** add `DATABASE_URL` to the same "must be set in .env" check.
10. **ffmpeg and ffprobe have no time limit.** `execFile` in `app.ts` (`/thumbnail`), `frameRoute.ts` and `videoInfo.ts` has no `timeout`, so a broken video can hang the request forever.
    - **Direction:** add a `timeout` option (like 10 seconds) to each `execFile` call, and send the route's normal 500 error when it runs out.

## Security

1. **Nothing is protected.** There's no login, rate limit or CORS setup. Anyone who can reach the server can upload 2 GB files, start unlimited JAR jobs, or run ffmpeg over and over.
   - For a class project it's fine to write this down as a known risk.
   - The most realistic fixes are a limit on jobs running at once and a smaller upload limit.
2. **`path.basename("..")` returns `".."`.** So `/thumbnail/..` and `/process/..` point at the parent folder. ffmpeg and the JAR fail on a folder, so it doesn't do much harm.
   - **Direction:** use one shared "safe video filename" check for every route, like `uploadRoute`'s `safeFilename`.

## Refactoring

- **Repeated code:**
  - `/thumbnail` and `/frame` both build ffmpeg commands.
  - `/thumbnail`, `/frame` and `/process` each repeat the same "basename + join with `videoDir`" step.
  - Moving each into one helper means one place to fix bugs like the `..` one.
- **`app.ts`:** `/thumbnail` and `/api/videos` are still written directly in `app.ts`. Every other endpoint is its own `xRoute(config)` file, so move these two out to match.
- **Java interfaces:**
  - a `CentroidTracker` interface for `ImageCentroidTracker`
  - a `ResultWriter` interface for `CSVOutputWriter`
- **Hard-coded value:** `TOP_MARGIN = 30` in `VideoSummaryApp` only makes sense for the salamander video. Make it an optional argument, or at least document it in the README.
- **Leftover files and dead code:** nothing uses `JavaCVExperiment.java` or `processor/extractedFrame.png`. The experiment imports `ImageIO` and `File` but never uses them, and points at a hard-coded `sampleInput/ensantina.mp4`. `pom.xml` still has two commented-out `<mainClass>` lines for `ImageSummaryApp` and `JavaCVExperiment`.
  - **Direction:** remove them, or say in a comment why they stay.
- **`npm run build` compiles test files.** `server/tsconfig.json` has no `include`/`exclude`, so every `*.test.ts` file and `fakeJobStore.ts` end up in `dist/`.
  - **Direction:** look at an `exclude` for test files (or a separate tsconfig for tests).
- **`.gitignore` only ignores `processor/*.csv`.** A CSV made from the project root shows up as a new file that could get committed by accident.
  - **Direction:** look at ignoring output CSVs in the root too, without hiding `sampleOutput/groups.csv`.

## Tests

- **Highest priority:** tests for whatever validation is added to `POST /process`, like a bad threshold, a bad color, a missing video, or a folder name.
- **Next:**
  - `NeonJobStore`'s write functions against a fake pool
  - the "`onExit` runs twice" case
  - `finishJob` when the store throws
- **Lightly tested:** `videoInfo.ts`, `ImageCentroidTracker` (2 tests), and `Group.compareTo` (no direct tests).
- **`ImageCentroidTrackerTest` doesn't match the test style.** Its names (`returnsLargestGroupCentroid`, `returnsNegativeCoordinatesWhenNoGroupsFound`) aren't `method_condition_expectedResult`, and it has no Arrange / Act / Assert comments. It's the only Java test file with none (the server tests use `it("...")` sentences instead).
  - **Direction:** rename them (like `findCentroid_noGroups_returnsNegativeOnes`) and add the comments.
- **CI (`.github/workflows/run-tests.yml`) only runs `mvn test`.**
  - Add a step for the server's Vitest tests. The `/thumbnail`, `/frame`, upload and details tests use real ffmpeg/ffprobe, so it would also need to install ffmpeg.
  - It only runs on pull requests, not on pushes to `main`.
  - `actions/checkout@v3` and `actions/setup-java@v3` are old versions, and it uses Java 23 while `pom.xml` targets Java 21.
  - **Direction:** look at newer action versions, matching the Java version, and an ffmpeg install step.

## Performance

- **No limit on jobs at once.** Each JAR already uses every CPU core, so two or three at the same time slow each other down.
  - **Direction:** a simple queue, or a cap on running jobs.
- **Repeated ffmpeg work.** Thumbnails and ffprobe results are made again on every request. Cache them on disk.
- **`listJobs` returns every row.** Add a `LIMIT`, and an index on `created_at`.
- **`GET /api/videos/details` starts every ffprobe at once.** `videoDetailsRoute.ts` uses `Promise.all` over every video, so a folder with 100 videos starts 100 ffprobe programs at the same time.
  - **Direction:** look at running a few at a time (this also goes well with caching the results).

## Documentation

- **Server:** add JSDoc to the exported functions, especially in `startJob.ts`, `jarRunner.ts` and the job store.
- **README server section is out of date.**
  - Line 114 is just the word "server", and line 115 says "This branch adds a server…" even though it's merged into `main`.
  - It's missing the setup steps (`.env`, `npm install`, ffmpeg, `mvn package`), how to run the server (`npm run dev`, `npm start`) and its tests (`npm test`), and the list of endpoints.
  - **Direction:** clean up those two lines and add the missing steps and endpoints.
- **Schema:** explain in a comment why `finished_at` isn't stored yet. `jobsRoute.ts` mentions it.
- **`ImageCentroidTracker` has hidden rules.** It has no Javadoc, so two things aren't written down:
  - it returns `(-1, -1)` when there are no groups, which the CSV depends on
  - `groups.get(0)` is only the largest group because the group finder sorts biggest first
  - **Direction:** add a short Javadoc that says both.
- **`BinaryGroupFinder`'s Javadoc contradicts itself.** It says the sort is "size first, then x, then y", then says ties are broken "first by descending y value, then descending x value". `Group.compareTo` really checks x before y.
  - **Direction:** fix the comment so it matches `Group.compareTo`.

## Top 3 priorities

1. Validate the input in `POST /process`.
2. Stop jobs from getting stuck on `processing`.
3. Keep the JAR's error message.
