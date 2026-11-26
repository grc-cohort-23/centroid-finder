# Priority Improvements (Backend, Wave 4)

Scoped to `onjamm/centroid-finder`. This file only lists improvements for this repo. The shared Google doc [priorityImprovements](https://docs.google.com/document/d/1u7U2ftQbZdAJyk1OF5iQ6v1unYbatToOxnap652U4Tk/edit) covers both repos together, including the concerns they share.

**Status: Wave 5 set chosen.** The rest are AI-ranked proposals pulled from `humanImprovements.md` and `robotImprovements.md`. The assignment says the two of us decide the final top two per category, so edit freely. Once it's settled, this becomes the basis for the new `TASKS.md`.

## What the assignment asks for

- **Wave 4:** list the top **two** improvements in each category. Refactoring, tests, error handling and documentation are required. Performance, security, bug fixes and other are optional.
- **Wave 5:** make at least **6** improvements across the project, with at least one from each required category. Each one gets its own branch and a PR whose title starts with the category. The partner reviews it and says LGTM, and nobody approves their own PR.

## Lanes (who owns which files in this repo)

- **sabinMas:** `processor/**` (all Java code, tests, `pom.xml`, `sampleInput/`) and the root `.gitignore`
- **onjamm:** `server/**` (routes, lib, tests, `app.js`, `index.js`), `.github/workflows/**`, `README.md` and `docs/`

Lanes follow the split that already worked in the original `TASKS.md`, so the two agents almost never edit the same file.

## Chosen for Wave 5 (confirmed)

We picked these 8 branches across both repos: 4 for sabinMas and 4 for onjamm. Together they cover every required category and clear the minimum of 6. The exact steps, file ownership and merge order for this repo are in `TASKS.md`. Everything else in this file stays as backlog.

| ID | Category | Owner | Reviewer | Branch | Repo |
|---|---|---|---|---|---|
| F-R1 | Refactoring code | sabinMas | onjamm | `refactoring/remove-scaffold-dead-code` | frontend |
| F-T1 | Adding tests | sabinMas | onjamm | `tests/recent-jobs-and-api-client` | frontend |
| B-T2 | Adding tests | sabinMas | onjamm | `tests/restore-video-fixture` | **this repo** |
| B-E1 | Improving error handling | sabinMas | onjamm | `error-handling/exit-codes-and-exceptions` | **this repo** |
| B-R3 | Refactoring code | onjamm | sabinMas | `refactoring/extract-job-runner` | **this repo** |
| F-E1 | Improving error handling | onjamm | sabinMas | `error-handling/video-list-retry` | frontend |
| B-T1 | Adding tests | onjamm | sabinMas | `tests/fix-ci-workflow` | **this repo** |
| B-D2 | Writing documentation | onjamm | sabinMas | `docs/readme-and-server-jsdoc` | **this repo** |

The ones in this repo:

- **B-T2** (Adding tests, sabinMas): Restore a test video and add an end-to-end JAR test (`tests/restore-video-fixture`)
- **B-E1** (Improving error handling, sabinMas): Non-zero exit codes and specific exception types (`error-handling/exit-codes-and-exceptions`)
- **B-R3** (Refactoring code, onjamm): Extract a job runner from routes/process.js (`refactoring/extract-job-runner`)
- **B-T1** (Adding tests, onjamm): Fix CI so it actually runs our tests (`tests/fix-ci-workflow`)
- **B-D2** (Writing documentation, onjamm): Real README plus server JSDoc (`docs/readme-and-server-jsdoc`)

## Top two per category

### Refactoring code (required)

#### B-R1: Remove dead code and tidy the root

- **Why:** Experiment.java duplicates VideoSummaryApp, and pom.xml's exec plugin still points at it. output.csv is committed even though it's ignored. notes, options, plan and serverPlan clutter the root (the Wave 2 'monolithic parent directory' note).
- **Branch:** `refactoring/remove-dead-code`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/Experiment.java (delete); processor/pom.xml (exec block only); output.csv; notes.md, options.md, plan.md, serverPlan.md (move to docs/); unused import in EuclideanColorDistance.java; leftover comments in DfsBinaryGroupFinder.java
- **Collision notes:** Merge FIRST in the backend. Do NOT do the big package restructure this round (see 'Deferred').
- **PR title:** "Refactoring: removed Experiment.java and moved planning docs into docs/"

#### B-R3: Extract a job runner from routes/process.js

- **Why:** One route handler validates, checks files, creates the job, spawns Java and handles the exit. Moving 'run the processor' into server/lib/ lets you test it with a fake spawn.
- **Branch:** `refactoring/extract-job-runner`
- **Suggested owner:** onjamm
- **Files this branch may touch:** server/routes/process.js; new server/lib/jobRunner.js; new server/test/jobRunner.test.js
- **Collision notes:** Server lane. Do this before B-E2 and B-D2, which touch the same files.
- **PR title:** "Refactoring: moved Java process handling into a jobRunner module"

**Also considered:**

#### B-R2: Extract an ArgumentParser from the two main methods

- **Why:** ImageSummaryApp and VideoSummaryApp both parse and validate arguments by hand, and the hex parsing is duplicated. This is the assignment's own example PR.
- **Branch:** `refactoring/extract-argument-parser`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/VideoSummaryApp.java; ImageSummaryApp.java; new ArgumentParser.java; processor/src/test/java/VideoSummaryAppTest.java; new ArgumentParserTest.java
- **Collision notes:** Same files as B-E1, so same lane. Merge B-E1 first.
- **PR title:** "Refactoring: extracted ArgumentParser from the main methods"

### Adding tests (required)

#### B-T1: Fix CI so it actually runs our tests

- **Why:** run-tests.yml still uses the Part 1 setup (javac src/*.java with a lib/ jar). Neither exists any more, so CI can't test anything. It should run mvn test in processor/ and npm test in server/, on Java 21 with actions v4.
- **Branch:** `tests/fix-ci-workflow`
- **Suggested owner:** onjamm
- **Files this branch may touch:** .github/workflows/run-tests.yml only
- **Collision notes:** CI goes green only once B-T2 restores the test video. Merge B-T2 first, or expect the 2 FFmpeg tests to fail.
- **PR title:** "Tests: CI now runs mvn test and the server tests"

#### B-T2: Restore a test video and add an end-to-end JAR test

- **Why:** A fresh clone fails 2 of 40 tests because sampleInput/ball.mp4 was deleted. Add a tiny fixture back and a test that runs VideoSummaryApp on it and checks the CSV.
- **Branch:** `tests/restore-video-fixture`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/sampleInput/ (one tiny fixture video); .gitignore (the fixture exception line); processor/src/test/java/FfmpegVideoFrameGrabberTest.java; new VideoSummaryAppEndToEndTest.java
- **Collision notes:** The only backend branch allowed to edit .gitignore.
- **PR title:** "Tests: restored a fixture video and added an end-to-end VideoSummaryApp test"

### Improving error handling (required)

#### B-E1: Non-zero exit codes and specific exception types

- **Why:** VideoSummaryApp exits with code 0 even when it fails, and the server relies on that code. catch (Exception e) and throws Exception appear across the apps and FfmpegVideoFrameGrabber. The assignment calls this out by name.
- **Branch:** `error-handling/exit-codes-and-exceptions`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/VideoSummaryApp.java; ImageSummaryApp.java; FfmpegVideoFrameGrabber.java; matching test files
- **Collision notes:** Merge before B-R2 (same files).
- **PR title:** "Error handling: non-zero exit codes and specific exceptions in the Java apps"

#### B-E2: Recover stuck jobs and return friendly errors

- **Why:** Jobs stay 'processing' forever after a server restart. Raw Java stderr, with stack traces and paths, is sent to the browser. There's no JSON error handler.
- **Branch:** `error-handling/server-job-recovery`
- **Suggested owner:** onjamm
- **Files this branch may touch:** server/index.js; server/lib/jobStore.js; server/app.js; server/routes/process.js (or jobRunner.js after B-R3); server/test/
- **Collision notes:** After B-R3 in the server lane.
- **PR title:** "Error handling: recover stuck jobs on startup and hide raw stderr"

### Writing documentation (required)

#### B-D1: Fix and complete the JavaDoc

- **Why:** BinaryGroupFinder's sort-order doc contradicts itself. 'Dfs' is actually BFS. Several classes have no class JavaDoc, and ImageBinarizer writes x000000.
- **Branch:** `docs/javadoc-fixes`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** comment-only changes in processor/src/main/java/*.java
- **Collision notes:** Comment-only, but it touches many Java files. Do it after B-R1, B-E1, B-R2 and B-B1 in the Java lane.
- **PR title:** "Documentation: fixed the sort-order JavaDoc and added missing class docs"

#### B-D2: Real README plus server JSDoc

- **Why:** The README is still the Part 1 assignment text, with a javac command that no longer works. The server has no JSDoc (Wave 2 asked for multi-line comments on every method).
- **Branch:** `docs/readme-and-server-jsdoc`
- **Suggested owner:** onjamm
- **Files this branch may touch:** README.md (move the old Part 1 text to docs/); comment-only changes in server/lib/*.js and server/routes/*.js
- **Collision notes:** Keep the AI-rules section visible. After B-R3 and B-E2 in the server lane.
- **PR title:** "Documentation: new README and JSDoc for the server"

### Improving performance (optional)

#### B-P1: Only convert frames that are sampled

- **Why:** Every decoded frame becomes a BufferedImage, but only about 1 in 15 is used at 30 fps and 0.5s sampling.
- **Branch:** `performance/skip-unsampled-frames`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/FfmpegVideoFrameGrabber.java; SamplingVideoGroupFinder.java; VideoFrameGrabber.java; their tests
- **Collision notes:** FfmpegVideoFrameGrabber is also edited in B-E1. Do this after it.
- **PR title:** "Performance: skip converting frames that aren't sampled"

#### B-P2: No per-pixel objects in color distance and binarizing

- **Why:** EuclideanColorDistance creates two Color objects and two arrays per pixel, and getRGB is called one pixel at a time.
- **Branch:** `performance/color-distance-allocations`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/EuclideanColorDistance.java; DistanceImageBinarizer.java; their tests
- **Collision notes:** Keep the existing tests green as the safety net.
- **PR title:** "Performance: removed per-pixel allocations from color distance"

### Hardening security (optional)

#### B-S1: Tighter filename and threshold validation

- **Why:** isSafeFilename allows dot-files such as .env and any extension, and isValidThreshold allows any number of digits.
- **Branch:** `security/tighter-input-validation`
- **Suggested owner:** onjamm
- **Files this branch may touch:** server/lib/validate.js; server/test/validate.test.js; server/routes/videos.js (to share the extension list)
- **Collision notes:** No collisions with B-R3 or B-E2.
- **PR title:** "Security: allow only video extensions and cap the threshold"

#### B-S2: Cap concurrent jobs

- **Why:** Every POST /process starts a full JVM, and nothing stops 50 requests from starting 50 of them.
- **Branch:** `security/limit-concurrent-jobs`
- **Suggested owner:** onjamm
- **Files this branch may touch:** server/routes/process.js or server/lib/jobRunner.js; server/test/
- **Collision notes:** After B-R3 and B-E2 in the server lane.
- **PR title:** "Security: limit how many processing jobs run at once"

### Bug fixes (optional)

#### B-B1: Integer overflow in centroid sums

- **Why:** The x and y sums are ints. Above roughly 1080p (for example 4K) one big group overflows and gives a wrong or negative centroid.
- **Branch:** `bugfix/centroid-sum-overflow`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/DfsBinaryGroupFinder.java; DfsBinaryGroupFinderTest.java
- **Collision notes:** After B-R1, which trims comments in the same file.
- **PR title:** "Bug fix: centroid sums no longer overflow on large frames"

#### B-B2: Decide the sampling behavior after a timestamp gap

- **Why:** After a gap, SamplingVideoGroupFinder samples frames back to back until it catches up, which gives CSV rows closer together than 0.5s. A test locks this in, so decide whether it's intended.
- **Branch:** `bugfix/sampling-gap-behavior`
- **Suggested owner:** sabinMas
- **Files this branch may touch:** processor/src/main/java/SamplingVideoGroupFinder.java; SamplingVideoGroupFinderTest.java
- **Collision notes:** After B-P1, which edits the same file.
- **PR title:** "Bug fix: sampling no longer bursts after a timestamp gap"

## Merge order (per lane)

- **sabinMas:** B-R1 → B-T2 → B-E1 → B-R2 → B-B1 → B-P1 → B-B2 → B-P2 → B-D1
- **onjamm:** B-T1 → B-S1 → B-R3 → B-E2 → B-S2 → B-D2

The two lanes run in parallel. Exception: B-T1 (CI) only goes green after B-T2 restores the test video.

## Deferred (not this round)

- **Package restructure** (interfaces, records and processes folders from Wave 2): every Java file and test moves at once, so it collides with every other Java branch. If you want it, do it alone, first, and merge it before anything else starts.
- **API route rename** (a single `/api` prefix): couples both repos, so leave it until after Wave 5.
- **Upload endpoint, hosting, a database for jobs:** new features.

## Rules so two AI agents don't cross paths

1. **Stay in your lane.** Each agent only edits files in its owner's lane (above) and only the files listed for its current branch. If it needs a file outside that list, it stops and asks a human instead of editing it.
2. **One branch at a time per agent**, in the merge order listed above. Branch from the latest `main` every time.
3. **Shared files have one owner.** Only the listed owner changes them; anyone else asks first. That covers `package.json` and `package-lock.json` in the frontend, and `pom.xml`, `.gitignore` and `README.md` in the backend.
4. **After any merge to `main`**, both agents merge `main` into their open branch before continuing.
5. **No drive-by fixes.** If an agent notices another problem, it writes it down instead of fixing it in the current branch.

## Shared concerns (both repos)

These affect both repos, so both agents need to know about them. The full list is in the shared Google doc.

- **The API contract is frozen for Wave 5.** No branch renames routes or changes the JSON shapes (`GET /api/videos`, `GET /thumbnail/:filename`, `POST /process/:filename`, `GET /process/:jobId/status`, `/results/*.csv`). The frontend's `src/api/` and the backend's `server/routes/` must keep matching.
- **The missing `ball.mp4`.** Backend B-T2 restores a test video. Frontend F-D1 updates the frontend README's setup steps to match, after B-T2 merges.
- **Error messages.** Backend B-E2 makes the server return short, friendly errors in the same `{ error }` shape. The frontend shows `error` as-is, so nothing changes on the frontend side.
- **Course rules for every branch:** one improvement per branch. The PR title starts with its category. The partner who didn't write it reviews it and comments LGTM. Nobody approves their own PR. Merge `main` into your other open branches after each merge. Every AI-assisted commit starts with "AI Used".
