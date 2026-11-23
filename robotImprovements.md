# Robot Improvements

Suggested improvements from an AI review of the code. These are directions to look into, not exact fixes.

## Bugs and error handling

1. **`POST /process` doesn't check its input.**
   - `threshold` goes through `Number()`, so `"abc"` becomes `NaN` and `"12.5"` becomes a decimal. Either one makes the database insert fail, so the client gets a 500 when it should get a 400.   
   - `targetColor` isn't checked against the 6-hex-digit format.
   - **Direction:** check both values in the route, using the same rules as `ArgumentParser`.
   - Also check that the video exists before creating a job. Right now a missing file only shows up later as "JAR exited with code 1".
2. **Jobs can get stuck on `processing`.** If the server restarts while a JAR is running, nothing ever calls `finishJob`.
   - **Direction:** mark old `processing` jobs as errors when the server starts, or add a timeout.
3. **The JAR's real error message is lost.** `stdio: "ignore"` throws away its stderr, so a failed job only says "exited with code 1".
   - **Direction:** capture stderr into a log file, or save the last line of it into the job's `error`.
4. **`onExit` can run twice.** Sometimes both the `exit` and `error` events fire. Only the first call should update the job.
5. **Java: write errors are silent.** `PrintWriter` in `CSVOutputWriter` never throws, so a full disk can still end with exit code 0 and a broken CSV.
   - **Direction:** call `checkError()` before closing, and exit with 1 if it returns true.
6. **Java: `ImageSummaryApp` catches plain `Exception` in 3 places.** Catch the specific exceptions instead.
7. **The server can crash or hang.**
   - There's no `pool.on("error")` listener, so a dropped Neon connection can crash the process.
   - There's no Express error handler.
   - There's no graceful shutdown (`pool.end()`).
8. **Half-written CSVs are already public.** The CSV is written straight into `/results`, so it can be downloaded while the JAR is still running, or after it fails.
   - **Direction:** write to a temporary name and rename it when it's done. `uploadRoute` already does this with `.part` files.

## Security

1. **Nothing is protected.** There's no login, rate limit or CORS setup. Anyone who can reach the server can upload 2 GB files, start unlimited JAR jobs, or run ffmpeg over and over.
   - For a class project it's fine to write this down as a known risk.
   - The most realistic fixes are a limit on jobs running at once and a smaller upload limit.
2. **`path.basename("..")` returns `".."`.** So `/thumbnail/..` and `/process/..` point at the parent folder. ffmpeg and the JAR fail on a folder, so it doesn't do much harm.
   - **Direction:** use one shared "safe video filename" check for every route, like `uploadRoute`'s `safeFilename`.
3. **Error messages:** raw ffmpeg and database errors go to the server logs, not to the client. Keep it that way.
4. **Secrets:** keep `DATABASE_URL` only in `.env`, and check that `.env.example` has no real values in it.

## Refactoring

- **Repeated code:**
  - `/thumbnail` and `/frame` both build ffmpeg commands.
  - Every route repeats the same "basename + join with `videoDir`" step.
  - Moving each into one helper means one place to fix bugs like the `..` one.
- **`app.ts`:** `/thumbnail` and `/api/videos` are still written directly in `app.ts`. Every other endpoint is its own `xRoute(config)` file, so move these two out to match.
- **Java interfaces:**
  - a `CentroidTracker` interface for `ImageCentroidTracker`
  - a `ResultWriter` interface for `CSVOutputWriter`
- **Hard-coded value:** `TOP_MARGIN = 30` in `VideoSummaryApp` only makes sense for the salamander video. Make it an optional argument, or at least document it in the README.
- **Leftover files:** delete or document `JavaCVExperiment.java`, `processor/extractedFrame.png`, and the `salamander-*.csv` files at the project root.

## Tests

- **Highest priority:** tests for whatever validation is added to `POST /process`, like a bad threshold, a bad color, or a missing video.
- **Next:**
  - `NeonJobStore`'s write functions against a fake pool
  - the "`onExit` runs twice" case
  - `finishJob` when the store throws
- **Lightly tested:** `videoInfo.ts`, `ImageCentroidTracker` (2 tests), and `Group.compareTo` (no direct tests).
- **CI:** it only runs `mvn test`. Add a step for the server's Vitest tests.

## Performance

- **No limit on jobs at once.** Each JAR already uses every CPU core, so two or three at the same time slow each other down.
  - **Direction:** a simple queue, or a cap on running jobs.
- **Repeated ffmpeg work.** Thumbnails and ffprobe results are made again on every request. Cache them on disk.
- **`listJobs` returns every row.** Add a `LIMIT`, and an index on `created_at`.
- **The JAR is about 675 MB.** `javacv-platform` includes every operating system. A version with only the platforms we need would be much smaller.

## Documentation

- **Server:** add JSDoc to the exported functions, especially in `startJob.ts`, `jarRunner.ts` and the job store.
- **Java:** add Javadoc to the classes and methods that don't have it.
- **README:** the server section needs its setup steps (`.env`, `npm install`, ffmpeg, `mvn package`) and the list of endpoints.
- **Schema:** explain in a comment why `finished_at` isn't stored yet. `jobsRoute.ts` mentions it.

## Top 3 priorities

1. Validate the input in `POST /process`.
2. Stop jobs from getting stuck on `processing`.
3. Keep the JAR's error message.
