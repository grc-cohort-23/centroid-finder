Kate's:
## Refactoring code (required)
1. Merge the `server` branch into `main` with a single folder layout, and stop committing runtime output (`jobs/*.json`, `results/*.csv`), adding both folders to `.gitignore`. Right now no single branch runs as the whole project. (Human: "loose files scattered around"; Robot: repository structure)
2. Remove unused code: delete `VideoExperiment.java` (our early experiment, replaced by `JavaCvFrameSource` + `VideoProcessor`), remove the unused `orsonpdf` and `jfreechart` dependencies from `pom.xml`, and delete the duplicate `mkdirSync` line in `server/index.js`. (Human: "remove unused files/methods")

## Adding tests (required)
1. Test `createProcessorRunner` in `services.js`, the only code that turns a Java exit into a job status: exit 0 → done, non-zero → error, Java missing → error, and the callback fires only once.
2. Test `JavaCvFrameSource` with the real `sample.mp4`: expect 10 frames at 0–9 s, a non-video file should throw `IOException`, and `stepSeconds <= 0` should throw `IllegalArgumentException`. (Human: "check if an mp4 file is bad")

## Improving error handling (required)
1. `VideoProcessorApp.java:24` uses `catch (Exception e)`, which treats I/O failures and programming bugs the same way (a missing output folder prints only the path). Catch `NoSuchFileException`/`AccessDeniedException` first, then `IOException`, then `RuntimeException` with a stack trace, and exit non-zero each time. Also exit 1 when 0 frames are read.
2. Show Java's real error in the job status. The server uses `stdio: "ignore"`, so every failure appears as "exit code 1". Send Java's stderr to `jobs/<id>.log` and save its last line in `job.error`. (Human: "make sure front end shows it")

## Writing documentation (required)
1. Rewrite the README, which is still the assignment brief and whose only command (`javac … src/*.java`) no longer works. Add: prerequisites (JDK 21+, Maven, Node 18+, ffmpeg), the build command (`mvn package`), CLI usage and exit codes, the CSV format (`seconds,x,y`, no header, `-1,-1` when nothing is found, one row per second), `.env` variables, the 4 API endpoints with examples, and how to run the tests.
2. Finish the half-done Javadoc/JSDoc on our own classes (not the instructor's starter files), and fix the example in `plan.md`, which shows a row every 0.033 s while the real output is one row per second. (Human: "quite a few are half done")

## Improving performance (optional)
1. Shrink the 731 MB `videoprocessor.jar`. `javacv-platform` bundles native libraries for 13 platforms, so build with `-Djavacpp.platform=<our platform>` (e.g. `macosx-arm64`).
   (Our idea of skipping frames is already done: we sample once per second, and an 8-minute video takes about 29 s.)

## Hardening security (optional)
1. Bind the server to `127.0.0.1`. It currently listens on all network interfaces, and each request starts a ~200 MB Java process with no limit. Optionally allow at most 2 jobs at once and return 429 after that.
2. Add a final JSON error handler in `app.js`. A malformed URL currently returns an HTML page with a stack trace and server file paths.

## Bug fixes (optional)
1. CI is broken: `run-tests.yml` compiles `src/*.java`, which matches nothing since we moved to Maven, so every PR fails. It's the instructor's file, so ask before changing it to `mvn -B test`.
2. Express accepts inputs that Java rejects: `threshold=99999999999` gets a 202 and then fails with "exit code 1", and a filename of `..` starts a job on the project folder. Reject thresholds above 442 and any filename that isn't a real video file with 400.

## Other (optional)
1. Jobs stay "processing" forever if the server restarts. On startup, mark leftover "processing" jobs as `error: "server restarted"`.
