# Robot Improvements (Wave 3: AI Improvements)

This is the AI pass over the `centroid-finder` backend: the Java processor (`processor/`, `pom.xml`), the Express server (`server/`), the CI workflow, and the root-level files. It builds on our Wave 2 notes in [humanImprovements.md](humanImprovements.md). It follows the prompt from the assignment:

> Please look through this code and identify improvements I could make. Focus on things like refactoring code, fixing bugs, adding tests, improving performance, improving error handling, hardening security, and writing documentation. Please do not write the exact code fixes. Instead give me overall direction on what I should be focusing on.

The sections use the same category order as the assignment's Wave 2 questions: required categories first, then the optional ones. That makes it easy to pick the top two per category for Wave 4. The frontend has its own `robotImprovements.md` in `centroid-finder-frontend`.

Everything below is direction, not finished code. Each item says what to look at, why it matters, and what "done" would look like. Remember the assignment's advice: be skeptical, and keep only what you agree with.

---

## How this pass was done

These are the Wave 2 questions, asked again with the code actually running:

1. **Is it readable?** Could a new teammate follow it without asking us?
2. **Is it dead?** Is anything left over from earlier waves, such as experiments, notes, or committed output?
3. **Is it doing too much?** Would splitting a class or method make it easier to test?
4. **Where would interfaces help?** (The assignment asks this one specifically.)
5. **What happens when it fails?** Bad arguments, missing files, a broken video, a crashed Java process, a full disk.
6. **Is it tested where it can break?** Does a fresh clone pass the tests? Does CI actually run them?
7. **Is it fast enough on real footage?** Our tests use tiny images. The salamander video is full HD.
8. **Could someone misuse it?** Anything that reaches the server from a URL ends up in file paths and a spawned process.

**What we ran:**
- `npm test` in `server/`: 36 of 36 pass.
- `npm audit`: 0 vulnerabilities.
- `mvn test` in `processor/`: 40 tests, **2 errors**. Both `FfmpegVideoFrameGrabberTest` tests fail with "Could not open input sampleInput/ball.mp4", because `ball.mp4` isn't in the repo any more (see Bugs).
- The GitHub Actions workflow, read rather than run, can't pass as written (see Tests).

### Color key (same as Wave 2)

- ⚫ **Black** = fine as is
- 🟣 **Purple** = backlog item
- 🔵 **Blue** = should be updated now
- 🔴 **Red** = delete

---

## Top priorities

If we only get to a few things, these give the most value for the effort:

1. 🔵 **Fix CI so it actually runs our tests** (Tests). Right now the workflow points at folders that don't exist.
2. 🔵 **Make a fresh clone pass `mvn test`** (Bugs and Tests). Restore a small test video or build one inside the test.
3. 🔵 **Make the Java app exit with a non-zero code on failure** (Error handling). The server relies on the exit code.
4. 🔵 **Replace `catch (Exception e)` and `throws Exception` with specific types** (Error handling). The assignment calls this out by name.
5. 🔵 **Stop converting every video frame when only some are sampled, and stop allocating objects per pixel** (Performance).
6. 🔴 **Remove or archive `Experiment.java` and the root-level leftovers** (Refactoring, dead code).
7. 🔵 **Fix the contradictory sort-order JavaDoc** (Documentation).

---

## 1. Refactoring code

### Package structure (our Wave 2 plan, with one change)

Wave 2 planned to split `src/main/java` into folders for interfaces, records and processes. That's a good instinct, because 20 classes in the default package is hard to navigate. One thing to be careful about: in Java, a folder only counts once the files declare a matching `package`, and the default package can't be imported from a named one. So this is an "all at once" change: every class moves into a package together, and the tests follow.

**Direction:** consider grouping by *what the code is about* instead of *what kind of Java file it is*, because that usually keeps related things together. For example, use one package for the image pipeline (binarizer, color distance, group finder, Group, Coordinate), one for video (frame grabber, sampling finder, FrameData, VideoResult, writer), and one for the command-line apps. Either layout is fine. Pick one and apply it everywhere. Remember to update the `mainClass` values in `pom.xml` and the JAR path the server uses.

### Remove dead and leftover code

- 🔴 `Experiment.java` duplicates `VideoSummaryApp`. It samples every 15th frame instead of every 0.5s, takes arguments in a different order, and uses `throws Exception`. It was useful during the JavaCV spike and isn't used now. `pom.xml` still points `exec-maven-plugin` at it. Archive it in a branch, or delete it, and point `exec` at `VideoSummaryApp`.
- 🔴 `output.csv` at the root is a committed result from `Experiment.java`, even though `.gitignore` now ignores `/output.csv`. If it's worth keeping as a validation fixture, move it next to the other sample outputs with a clear name.
- 🔵 `notes.md`, `options.md`, `plan.md`, and `serverPlan.md` sit at the root. They're useful history, but they clutter the repo (the Wave 2 "monolithic parent directory" note). Move them into a `docs/` folder and link them from the README.
- `EuclideanColorDistance` imports `java.awt.color.*` and never uses it.
- `DfsBinaryGroupFinder.findConnectedGroups` has a block of leftover planning comments ("after validating the image we then need to find groups...") after the work is already done. Trim them to comments that explain *why*.

### Put helpers where they belong

- `DfsBinaryGroupFinder.isValidImage` is a public static validator that `DistanceImageBinarizer` also calls. That makes the binarizer depend on one specific group-finder implementation. **Direction:** move binary-image validation into its own small utility, or onto the interface's contract, and give it a name that says it throws, such as "validate" or "require".
- The hex-to-int color parsing happens in three places (`ImageSummaryApp`, `VideoSummaryApp.parseTargetColor`, and `Experiment`). **Direction:** one shared parsing helper that also validates (see Error handling).
- `ImageSummaryApp` and `VideoSummaryApp` both do argument parsing, validation and usage messages by hand. **Direction:** a small argument-parsing class would make both `main` methods short and make the parsing unit testable without calling `main`. This matches the assignment's own example PR, "extracted new ArgumentParser class from the main method".

### Where an interface would help (an assignment question)

- **Output:** `VideoResultWriter` is a concrete class that always writes to a file. A "result writer" interface would let tests capture rows in memory, and would let a future version stream rows as they're produced.
- **Image to binary to groups is already well separated** behind `ImageBinarizer`, `ColorDistanceFinder` and `BinaryGroupFinder`. That's good work from Part 1. Keep it.

### Server

- `routes/process.js` does validation, file checks, job creation, process spawning and exit handling in one route handler. **Direction:** move "run the processor for this job" into its own module under `lib/` (a job runner). Then the route only validates and responds, and the runner can be tested with a fake spawn.
- `index.js` to `server.js` (from Wave 2) is fine. Update the README, `package.json`'s `main`, and the frontend README at the same time.
- Route naming: the API mixes `/api/videos` with unprefixed `/thumbnail`, `/process`, `/results`, and `/videos` (static). Starting a job and checking a job both live under `/process/...`, even though one takes a filename and the other a job id. **Direction:** agree with the frontend on one prefix and resource-style names, and change both sides in the same PR window. The frontend only touches URLs in `src/api/`, so its side is small.

---

## 2. Adding tests

### Make the tests we have actually run

- 🔵 **CI is broken.** `.github/workflows/run-tests.yml` was copied from the Part 1 template. It runs `javac` on `src/*.java` with `lib/junit-platform-console-standalone-6.0.3.jar`, but the code now lives in `processor/src/main/java` and there is no `lib/` folder. So every PR to `main` either fails or tests nothing. **Direction:** have CI run `mvn test` inside `processor/` and `npm ci` plus `npm test` inside `server/`, on the same Java version as the `pom.xml` (21; the workflow uses 23). Keep the "at least 15 tests" check if you like it. FFmpeg must be installed on the runner for the thumbnail route.
- 🔵 **A fresh clone fails 2 tests.** `FfmpegVideoFrameGrabberTest` reads `sampleInput/ball.mp4`, which was removed in the "remove stale ball.mp4" commit. The frontend README and this repo's `.gitignore` comment still say it's included. **Direction:** either commit a tiny fixture video on purpose (a second or two at low resolution, a few KB), or have the test generate one before it runs. Also have the test resolve the path relative to the project, not the current working directory.

### Highest-priority gaps

- **`VideoSummaryApp` end to end:** run `main` on a tiny fixture video and compare the CSV with a known-good output. That's the one test that proves the JAR the server spawns actually works.
- **`VideoResultWriter`:** no tests. Cover an empty list, the row format, and an unwritable path.
- **`ImageSummaryApp`:** no tests. At minimum, run it on `sampleInput/squares.jpg` and compare with `sampleOutput/`. That's the Part 1 validation step turned into an automatic test.
- **`Group.compareTo`:** the tie-break order is only tested indirectly. Add direct tests, because the docs disagree about it (see Documentation).
- **Edge cases that try to break things** (our Wave 2 "write more edge cases" item):
  - a 1x1 image,
  - a threshold of 0 (nothing matches, because the check is strictly less than),
  - a very large threshold (everything matches),
  - one group that covers the whole frame at full HD or larger (see the overflow bug),
  - a target color with fewer or more than 6 hex digits,
  - a negative threshold,
  - a video with no frames,
  - an interval of 0.
- **Server: the process lifecycle.** The current route tests stop at "processing or error". **Direction:** with the job runner extracted (Refactoring), test that a successful exit marks the job `done` with a result path, a non-zero exit marks `error` with stderr, and a missing JAR marks `error`. Use a fake spawn so no Java is needed.
- **Server: `jobStore`:** test concurrent updates, a missing job, and a corrupt JSON file.
- **Server: the thumbnail route's success path:** currently only the 400 and 404 paths are tested. It needs a tiny fixture video (the same one as above).

### Test hygiene

- `routes.test.js` writes an empty `test.mp4` and then posts a real process request. That can spawn a real `java -jar` in the background during tests. Make sure tests use temporary folders for videos, results and jobs, and clean them up, so test runs don't leave files in the repo.

---

## 3. Improving error handling

### The exit-code contract between Java and the server

`VideoSummaryApp` uses `return` after printing an error, so the process **exits with code 0 even when it failed**. The server only treats a job as done when the code is 0 *and* a CSV exists. That covers most cases today, but only by luck. If a partial or old CSV exists at that path, a failed run would look like a success. **Direction:** exit with a non-zero code on every failure path (bad arguments, can't open the video, error while reading, can't write the CSV). Ideally use a different code per kind of failure, and document the codes so the server can map them to friendly messages.

### Specific exceptions instead of `Exception` (called out in the assignment)

- `FfmpegVideoFrameGrabber` catches `Exception` around `start()` and `grabImage()`, and declares `close() throws Exception`. JavaCV throws its own `FrameGrabber.Exception`, so catch that. `close()` should declare only what it really throws.
- `VideoSummaryApp` has a broad `catch (Exception e)` for "error reading video" and another around writing. Narrow them to the I/O and frame-grabber exceptions you expect, and let real programming errors surface.
- `ImageSummaryApp` has three `catch (Exception e)` blocks around `ImageIO.read` and write and the CSV writer. These should be `IOException`.
- `Experiment.main` declares `throws Exception` (another reason to remove it).
- `nextFrame()` wraps failures in a plain `RuntimeException`. Consider a small custom unchecked exception (for example, a "frame read" exception) so callers can catch it specifically.

### Input validation

- `parseTargetColor` accepts anything `Integer.parseInt(..., 16)` accepts: `"FFF"`, `"1FFFFFF"`, `"-1"` and `"+FF"` all pass. The server validates first, but the JAR is also a command-line tool. **Direction:** require exactly 6 hex digits in Java too.
- `parseThreshold` accepts negative numbers. Decide on a valid range and reject anything outside it.
- `ImageIO.read` returns `null` (rather than throwing) for files it can't decode, which later causes a `NullPointerException` in the binarizer. Check for it and print a clear message.
- Constructors (`DistanceImageBinarizer`, `BinarizingImageGroupFinder`, `SamplingVideoGroupFinder`) accept `null` collaborators and a zero or negative interval. Fail fast in the constructor with a clear message instead of failing later deep inside a loop.

### Surfacing errors on the server

- **Jobs that never finish.** If the server restarts while a job is running, the detached Java process keeps going, but nobody records its exit. The job file stays `processing` forever, and the frontend polls forever. **Direction:** on startup, mark any leftover `processing` jobs as `error` (or check whether their CSV exists). Consider a maximum runtime per job.
- **`updateJob` can lose updates.** It reads, merges and writes with no lock. The `error` and `exit` events can both fire for one child. Make the update order explicit, so a later event can't overwrite the first result.
- **Raw stderr reaches the user.** The full Java stderr, including stack traces with file paths, is stored in the job and returned to the browser. **Direction:** log the full stderr on the server and return a short, friendly message (this also helps Security).
- **`console.error` everywhere.** There's no consistent logging. A tiny logger, or at least consistent prefixes with the job id, would make debugging much easier.
- **The thumbnail route** turns every ffmpeg failure into the same 500. Distinguish "ffmpeg isn't installed" (a setup problem) from "this file isn't a valid video" (a user problem).
- Add an Express error-handling middleware so unexpected errors return JSON in the same `{ error }` shape as everything else.

---

## 4. Writing documentation

### JavaDoc (Wave 2 "check the JavaDocs")

- 🔵 **The sort-order docs contradict each other.** `BinaryGroupFinder` says sorting is "size first, then x, then y", and then in the next sentence says ties are broken "first by descending y value, then descending x value". `Group.compareTo` actually does size, then x, then y. Fix the interface doc, and add a test that pins the real order.
- `DfsBinaryGroupFinder` is named "Dfs", but it uses a queue (`addLast` and `removeFirst`), which is breadth-first search. Either rename it or add a note explaining why the name stayed (the interface contract doesn't care which one is used). Expect to be asked about this in the presentation.
- Missing class-level JavaDoc: `DfsBinaryGroupFinder`, `EuclideanColorDistance`, `BinaryGroupFinder`, `ImageGroupFinder` (whose `@param image` has no description), and `Experiment`.
- `ImageBinarizer` writes the colors as `x000000` and `xFFFFFF`. Use `0x000000` and `0xFFFFFF`.
- `SamplingVideoGroupFinder`: the docs say sampling "stays close to even intervals even when frame timestamps have gaps". After a big gap, though, it samples several frames in a row until the target catches up, and a test (`gapBetweenTimestamps_processesEveryFrameUntilCaughtUp`) locks that in. Explain this behavior honestly in the docs, or decide it's not what you want (see Bugs).
- `VideoSummaryApp`: document the exit codes once they exist.

### JSDoc on the server (Wave 2 "add multi-line comments for the methods")

Every function in `lib/jobStore.js` and `lib/validate.js` and every route handler has no JSDoc. Prioritize:
- `jobStore`: the atomic write-then-rename trick in `saveJob` (explain *why*), and that `getJob` returns `null` for invalid ids.
- `validate.js`: what each validator accepts, with examples.
- `routes/process.js`: the job lifecycle and the exact JSON shapes returned. The frontend builds against these.

### README

- 🔵 The README is still the Part 1 assignment text ("Wave 0: AI Rules", "Wave 2: Implement DfsBinaryGroupFinder", and so on), and its validation command uses `javac` with a `lib/` jar that no longer exists. **Direction:** move the original assignment text to `docs/`, and write a real README: what the project does, prerequisites (Java 21, Maven, Node, FFmpeg), how to build the JAR, how to run the server, the `.env` settings, the API endpoints with example responses, and how to run each test suite. Keep the AI-rules section somewhere visible, since it's a course requirement.
- Document the API contract in one place (endpoint, parameters, success and error JSON). The frontend's `TASKS.md` currently holds the only written copy.

---

## 5. Improving performance (optional)

The full-HD salamander video is about 2 million pixels per frame, so per-pixel costs add up.

- 🔵 **Converting frames that get skipped.** `FfmpegVideoFrameGrabber.nextFrame()` converts *every* decoded frame to a `BufferedImage` before `SamplingVideoGroupFinder` decides whether to skip it. At 30 fps with a 0.5s interval, that's about 15 conversions for every frame we use. **Direction:** let the sampler look at the timestamp before paying for conversion. For example, the grabber could expose the next timestamp, or return frames that convert lazily. FFmpeg can also seek or skip frames directly.
- 🔵 **Objects per pixel in color distance.** `EuclideanColorDistance.distance` creates two `java.awt.Color` objects and two `int[]` arrays on every call, which is millions of short-lived objects per frame. **Direction:** pull the red, green and blue channels out with bit shifts and masks. Also consider comparing *squared* distance against *squared* threshold to skip the square root. If you do, keep the interface returning a real distance, and do the squared comparison as an optimization inside the binarizer.
- **`getRGB` one pixel at a time** is slow on a `BufferedImage`. Reading a whole row (or the whole raster) into an array at once is much faster.
- **Validating every pixel again.** `DistanceImageBinarizer` produces only 0s and 1s, and then `DfsBinaryGroupFinder` immediately re-walks every pixel to check they're 0 or 1. That's correct for the public API, but it doubles the work per frame. Consider a fast path for trusted input.
- **`Coordinate` objects in the BFS queue.** One record per visited pixel adds up when a group is large. Storing an index (row × width + col) in an int-based queue avoids that.
- **`ImageSummaryApp` binarizes the image twice**: once for `binarized.png` and once inside `BinarizingImageGroupFinder`. That's fine for a single image, but worth knowing.
- **JAR size and build time.** `javacv-platform` pulls in native libraries for *every* operating system and CPU, which makes `videoprocessor.jar` very large and slows `mvn package`. **Direction:** JavaCV supports limiting the native platforms (via the `javacpp.platform` setting). Restrict it to the platforms you actually run on.
- **Measure.** Time `VideoSummaryApp` on the salamander video before and after each change, and put the numbers in the PR description.

---

## 6. Hardening security (optional)

The basics are in good shape. `isSafeFilename` blocks path traversal, `spawn` and `execFile` receive their arguments as an array (no shell), job ids must be UUIDs, and `npm audit` is clean. Things to tighten:

- **Hidden files and odd names.** `isSafeFilename` allows names like `.env` and other dot-files, and any extension. The `/thumbnail` and `/process` routes only check that the file *exists* in the videos folder. **Direction:** also require one of the allowed video extensions (the same list `routes/videos.js` uses), and reject names starting with a dot.
- **Static `/videos` route.** `app.js` serves the whole videos folder with `express.static`. If anything other than videos ends up in that folder, it's downloadable. Restrict it to video extensions, or remove it if the frontend doesn't use it (the frontend's dev proxy no longer forwards `/videos`).
- **No limit on concurrent jobs.** Each `POST /process` starts a full Java process, and nothing stops 50 requests from starting 50 JVMs. **Direction:** cap the number of running jobs and return 429 or 503 when busy. Consider a basic rate limit.
- **Threshold range.** `isValidThreshold` accepts any length of digits, such as `99999999999999999999`, which then fails in Java's `Integer.parseInt`. Add a sensible maximum.
- **Error messages leak internals.** Returning raw Java stderr exposes file paths and stack traces (see Error handling).
- **Security headers.** If this is ever hosted, add a small set of HTTP security headers, and set CORS to the frontend's exact origin rather than opening it up.
- **Out-of-date tooling.** The CI workflow uses `actions/checkout@v3` and `actions/setup-java@v3`, while the current major versions are v4. Also check the Maven plugin versions (`exec-maven-plugin` 3.1.0, `maven-assembly-plugin`, `maven-surefire-plugin`) for newer releases, and turn on Dependabot for both npm and Maven so this stays current.
- **Pin Java consistently.** `pom.xml` targets 21, CI uses 23, and the README says "21+". Pick one and set it everywhere. The modern `maven.compiler.release` property is preferred over separate source and target settings.

---

## 7. Bug fixes (optional)

- 🔵 **Missing `ball.mp4` breaks the tests and the README instructions.** See Tests. The frontend README and the `.gitignore` comment say `ball.mp4` is included, but it isn't.
- 🔵 **Integer overflow on big frames.** `DfsBinaryGroupFinder` sums x and y coordinates in an `int`. For one group covering a full 1920x1080 frame, the x sum is about 1.99 billion, just under `int`'s limit of about 2.147 billion. Any larger video (for example 4K), or a group that covers most of a big frame, overflows and gives a wrong (even negative) centroid. **Direction:** use a wider type for the sums, and add a test with a large all-ones image.
- 🔵 **Failed runs exit with code 0.** See Error handling.
- **Sampling burst after a gap.** After a long gap in timestamps, `SamplingVideoGroupFinder` processes frames back to back until `target` catches up, instead of sampling once and moving on. This is tested and may be intentional, but it produces CSV rows closer together than 0.5s. Decide which behavior you want, and match the code, docs and test to it.
- **Stuck `processing` jobs after a server restart.** See Error handling.
- **Resource cleanup.** `FfmpegVideoFrameGrabber.close()` stops the grabber, but doesn't release it or close the `Java2DFrameConverter`. Both hold native memory. It's fine for one run per process, but it leaks in tests that open many videos.
- **Committed `output.csv` versus `.gitignore`.** The file is ignored but already tracked, so local runs of `Experiment` will show up as changes. Untrack it or move it (see Refactoring).

---

## 8. Other

- **Stay inside the "no new features" rule.** Concurrency limits, startup cleanup of stuck jobs, and the job runner extraction are hardening and refactoring. An upload endpoint, hosting, or a database for jobs would be new features, so save them for later.
- **Keep the API contract stable while refactoring.** Any route rename has to land together with the frontend's `src/api/` change, so `main` never pairs a new backend with an old frontend.
- **One improvement per branch and PR** (Wave 5). Most items above are already the right size, for example "Error handling: VideoSummaryApp exits non-zero on failure" or "Tests: CI runs mvn test and npm test".
- **AI commit rule.** The repo's AI rule says AI commits must start with "AI Used". Keep that convention for any AI-assisted commit in Wave 5.

---

## 9. File-by-file pass (in the Wave 2 format)

- 🔵 `.github/workflows/run-tests.yml`: rewrite for Maven plus Node, Java 21, and actions v4.
- 🔴 `output.csv`: untrack, or move it into `processor/sampleOutput/`.
- 🔵 `README.md`: replace the Part 1 assignment text with real project docs.
- 🟣 `notes.md`, `options.md`, `plan.md`, `serverPlan.md`: move into `docs/`.
- `processor`
  - 🔵 `sampleInput`: add back a tiny test video (or generate one in tests).
  - ⚫ `sampleOutput`: fine. 🔵 Use it in an automated `ImageSummaryApp` test.
  - 🔵 `pom.xml`: point `exec` at `VideoSummaryApp`, limit JavaCV native platforms, use `maven.compiler.release`, check plugin versions.
  - `src/main/java`
    - ⚫ `BinarizingImageGroupFinder.java`: good. Remove the stray comment. Add null checks in the constructor.
    - 🔵 `BinaryGroupFinder.java`: fix the contradictory tie-break doc.
    - ⚫ `ColorDistanceFinder.java`: good.
    - ⚫ `Coordinate.java`: good.
    - 🔵 `DfsBinaryGroupFinder.java`: overflow-safe sums, move `isValidImage` out, address the DFS versus BFS name, trim leftover comments, add class JavaDoc.
    - 🔵 `DistanceImageBinarizer.java`: read pixels in bulk, stop depending on `DfsBinaryGroupFinder` for validation, null checks.
    - 🔵 `EuclideanColorDistance.java`: no per-call object allocation, remove the unused import, add class JavaDoc.
    - 🔴 `Experiment.java`: remove (or archive in a branch).
    - 🔵 `FfmpegVideoFrameGrabber.java`: specific exceptions, release native resources, skip converting unsampled frames.
    - ⚫ `FrameData.java`: good.
    - ⚫ `Group.java`: good. 🔵 Add direct `compareTo` tests.
    - 🔵 `ImageBinarizer.java`: fix `x000000` to `0x000000` in the docs.
    - 🔵 `ImageGroupFinder.java`: finish the `@param` doc, add class JavaDoc.
    - 🔵 `ImageSummaryApp.java`: specific exceptions, handle `ImageIO.read` returning null, share argument parsing.
    - 🔵 `SamplingVideoGroupFinder.java`: validate the interval, decide on gap behavior, look at timestamps before converting.
    - ⚫ `VideoFrameGrabber.java`, `VideoGroupFinder.java`, `VideoResult.java`: good.
    - 🟣 `VideoResultWriter.java`: add tests. Consider an interface so tests don't need files.
    - 🔵 `VideoSummaryApp.java`: non-zero exit codes, strict hex and threshold validation, narrower catches.
  - `src/test/java`
    - 🔵 `FfmpegVideoFrameGrabberTest.java`: fix the missing fixture and the working-directory-relative path.
    - ⚫ The rest pass. 🔵 Add the edge cases listed under Tests.
- `server`
  - `lib`
    - 🔵 `jobStore.js`: JSDoc, safe ordering of updates, recovery of stuck jobs on startup.
    - 🔵 `validate.js`: JSDoc, extension allow-list, no dot-files, maximum threshold length.
  - `routes`
    - 🔵 `process.js`: extract the job runner, cap concurrent jobs, friendlier error messages, JSDoc.
    - 🔵 `thumbnail.js`: distinguish "ffmpeg missing" from "bad video", JSDoc, success-path test.
    - ⚫ `videos.js`: good. JSDoc. Share the extension list with `validate.js`.
  - `test`
    - ⚫ `routes.test.js`, `validate.test.js`: pass. 🔵 Use temp folders, and add lifecycle and thumbnail-success tests.
  - 🔵 `app.js`: add a JSON error handler. Restrict or remove the static `/videos` route.
  - ⚫ `config.js`: good.
  - 🟣 `index.js`: rename to `server.js` as planned, and update the docs that reference it.
