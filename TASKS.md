# Task Plan: centroid-finder backend (Wave 5: Improving)

Goal: build the Wave 5 improvements chosen in [priorityImprovements.md](priorityImprovements.md) on one branch per improvement. sabinMas and onjamm (and their AI agents) work at the same time without ever editing the same file on two open branches.

**This file is strict.** An agent working on a task may only create, edit, or delete the files listed under that task's **May touch**. Anything under **Must NOT touch**, or anything not listed at all, is off limits. If a task can't be finished without touching another file, the agent **stops and asks its human**. It doesn't work around the rule.

---

## Section 0: Rules for every task (both people, both agents)

1. **One task = one branch = one PR.** Use the exact branch name given. Branch from the latest `main`:
   ```
   git checkout main && git pull
   git checkout -b <branch-name>
   ```
2. **Only the files listed under "May touch".** No drive-by fixes, no reformatting other files, no renames or package moves. If you spot another problem, write it down in the PR description under "Noticed, not fixed".
3. **No new dependencies** in `processor/pom.xml` or `server/package.json` in any Wave 5 task.
4. **Don't change the API contract.** Routes, query parameters, status codes and JSON shapes stay exactly as they are (see "Frozen contracts" below). The frontend is being refactored at the same time and relies on them.
5. **Commits:** small and frequent, pushed to your own branch. Every AI-assisted commit message starts with `AI Used`. Don't mix human-written and AI-written changes in one commit (the repo's AI rule).
6. **Before opening the PR:** run `mvn test` in `processor/` if you touched anything in `processor/`, and `npm test` in `server/` if you touched anything in `server/`. Both must pass. The only exception is the 2 known `FfmpegVideoFrameGrabberTest` failures, until B-T2 merges.
7. **PR:** from your branch into `main`. The title starts with the category, exactly as given in the task. Fill in: what changed, why, how you tested it (commands, Postman, manual runs), and "Noticed, not fixed".
8. **Review:** the *other* partner reviews, leaves comments, and comments **LGTM** when satisfied. Nobody approves or merges their own PR without that LGTM.
9. **After any PR merges into `main`,** everyone with an open branch merges `main` into it before continuing:
   ```
   git checkout <your-branch> && git merge main
   ```
10. **Don't delete old branches** (`main`, `server`, `video`, `video-agentic-approach`). The instructor still grades them.

### Frozen contracts (read-only for every task)

- **HTTP API:** `GET /api/videos` returns a JSON string array. `GET /thumbnail/:filename` returns a JPEG, or `{ error }` with 400, 404 or 500. `POST /process/:filename?targetColor=RRGGBB&threshold=N` returns 202 `{ jobId }`, or `{ error }` with 400 or 404. `GET /process/:jobId/status` returns `{ status: 'processing' }`, `{ status: 'done', result: '/results/<jobId>.csv' }`, `{ status: 'error', error }`, or 404. `/results/*.csv` is served statically.
- **JAR command line:** `java -jar videoprocessor.jar <input_video> <output_csv> <hex_target_color> <threshold>`. The argument order doesn't change.
- **CSV format:** one `timestamp,x,y` row per sampled frame, timestamp with 2 decimals, `-1,-1` when nothing matches.
- **Java structure:** every class stays in `processor/src/main/java/` in the default package. No package restructure in Wave 5.

---

## Section 1: Ownership map

| Area | Owner |
|---|---|
| `processor/**` (all Java source, Java tests, `pom.xml`, `sampleInput/`, `sampleOutput/`) | **sabinMas** |
| `.gitignore` | **sabinMas** |
| `server/**` (routes, lib, tests, `app.js`, `config.js`, `index.js`, `package*.json`) | **onjamm** |
| `.github/workflows/**` | **onjamm** |
| `README.md`, and a new `docs/part1-instructions.md` (B-D2 only) | **onjamm** |
| `TASKS.md`, `priorityImprovements.md`, `humanImprovements.md`, `robotImprovements.md`, `notes.md`, `options.md`, `plan.md`, `serverPlan.md`, `output.csv`, `.env.example`, `LICENSE` | **nobody in Wave 5** (read-only) |

No file is owned by both people, and no Wave 5 task crosses into the other person's area.

---

## Section 2: Merge order

```
sabinMas:  B-T2 ──► B-E1
onjamm:    B-R3 ──► B-T1 ──► B-D2
                     ▲          ▲
   (B-T1 merges after B-T2 so CI is green; B-D2 goes last, after all four others)
```

- **B-T2** (sabinMas) and **B-R3** (onjamm) can start right away, in parallel.
- **B-E1** starts after B-T2 merges (same lane, overlapping test files).
- **B-T1** can be written any time, but **merge it after B-T2**, so CI goes green on its first run.
- **B-D2** goes last, after B-T2, B-E1, B-R3 and B-T1 are merged, so the README describes the finished build, tests, CI and exit codes.

---

## Section 3: sabinMas's tasks

**You own:** `processor/**` and `.gitignore`.
**You must NOT touch:** `server/**`, `.github/**`, `README.md`, `docs/**`, and the read-only files in Section 1.

### B-T2: Restore a test video and add an end-to-end JAR test

- **Category / PR title:** `Tests: restored a fixture video and added an end-to-end VideoSummaryApp test`
- **Branch:** `tests/restore-video-fixture`
- **Depends on:** nothing.
- **May touch:**
  - `processor/sampleInput/ball.mp4`: add back a **tiny** fixture video (a couple of seconds, low resolution, ideally under about 200 KB). It must contain one clearly colored moving object, so the expected centroids are predictable. Generate it yourself (FFmpeg can create synthetic test video) and note in the PR how you made it.
  - `.gitignore`: only if the existing `!/processor/sampleInput/ball.mp4` exception needs adjusting.
  - `processor/src/test/java/FfmpegVideoFrameGrabberTest.java`: only to make the fixture path reliable (it currently depends on running from `processor/`).
  - New `processor/src/test/java/VideoSummaryAppEndToEndTest.java`: runs the full pipeline on the fixture into a temporary output file, and checks the CSV row count, the format, and a known centroid for at least one row.
- **Must NOT touch:** any file in `processor/src/main/java/`; `pom.xml`; any other test file.
- **Done when:**
  - A fresh clone runs `mvn test` in `processor/` with **0 failures and 0 errors** (today it's 40 tests with 2 errors).
  - The new end-to-end test fails if you deliberately break the CSV format (check this locally, then undo the break).

### B-E1: Non-zero exit codes and specific exception types

- **Category / PR title:** `Error handling: non-zero exit codes and specific exceptions in the Java apps`
- **Branch:** `error-handling/exit-codes-and-exceptions`
- **Depends on:** B-T2 merged.
- **May touch:**
  - `processor/src/main/java/VideoSummaryApp.java`
  - `processor/src/main/java/ImageSummaryApp.java`
  - `processor/src/main/java/FfmpegVideoFrameGrabber.java`
  - `processor/src/test/java/VideoSummaryAppTest.java`, `processor/src/test/java/FfmpegVideoFrameGrabberTest.java`, and an optional new `processor/src/test/java/ImageSummaryAppTest.java`
- **Must NOT touch:** every other file in `processor/src/main/java/` (interfaces, records, binarizer, group finders, sampler, writer); `pom.xml`; the JAR's argument order.
- **Behavior to build:**
  - `VideoSummaryApp` exits **non-zero** on every failure path: wrong number of arguments, bad threshold, bad color, the video can't be opened, an error while reading frames, and the CSV can't be written. Use a distinct code per kind of failure and list the codes in the class JavaDoc. Success still exits 0.
  - **Keep it testable.** Calling `System.exit` inside `main` would kill the JUnit run (the existing `main_wrongArgumentCount_printsUsageAndReturns` test calls `main`). Structure it so the logic that decides the exit code can be tested without the JVM exiting, and update that test to check the code.
  - Replace `catch (Exception e)` and `throws Exception` in these three files with the specific types that can actually be thrown: I/O exceptions, JavaCV's frame-grabber exception, and number-format exceptions. `FfmpegVideoFrameGrabber.close()` declares only what it really throws.
  - `ImageSummaryApp`: handle `ImageIO.read` returning `null` (an unsupported or corrupt image) with a clear message instead of a `NullPointerException` later.
  - Error messages still go to stderr. The server stores stderr as the job's error text.
- **Done when:**
  - `mvn test` passes with tests covering at least: wrong argument count gives a non-zero code, a bad color gives a non-zero code, a missing video gives a non-zero code, and the fixture video gives 0.
  - `grep -n "catch (Exception" processor/src/main/java/VideoSummaryApp.java processor/src/main/java/ImageSummaryApp.java processor/src/main/java/FfmpegVideoFrameGrabber.java` finds nothing.
  - Manual check: build the JAR (`mvn -DskipTests package`), start the server, and process a missing or bad video through the UI or Postman. The job ends in `error`, not `done`.

---

## Section 4: onjamm's tasks

**You own:** `server/**`, `.github/workflows/**`, `README.md`, and `docs/part1-instructions.md` (B-D2 only).
**You must NOT touch:** `processor/**`, `.gitignore`, and the read-only files in Section 1.

### B-R3: Extract a job runner from routes/process.js

- **Category / PR title:** `Refactoring: moved Java process handling into a jobRunner module`
- **Branch:** `refactoring/extract-job-runner`
- **Depends on:** nothing.
- **May touch:**
  - `server/routes/process.js`
  - New `server/lib/jobRunner.js`: owns spawning `java -jar`, collecting stderr, and updating the job when the process errors or exits.
  - New `server/test/jobRunner.test.js`
- **Must NOT touch:** `server/routes/videos.js`, `server/routes/thumbnail.js`, `server/lib/validate.js`, `server/lib/jobStore.js` (use its existing functions as they are), `server/app.js`, `server/config.js`, `server/index.js`, `server/test/routes.test.js`, `server/test/validate.test.js`, `server/package*.json`.
- **Rules:**
  - The route keeps doing validation and responding. The runner does the process work. Behavior seen from HTTP is **identical**: the same status codes, JSON and timing (still respond 202 right away).
  - Make the runner testable without Java, for example by letting the test pass in a fake spawn. Don't add a mocking library.
  - Keep the current rule that a job is `done` only when the exit code is 0 **and** the CSV exists. B-E1 makes the exit codes trustworthy, so the two tasks fit together without coordinating.
- **Done when:**
  - `server/test/routes.test.js` passes **unchanged**.
  - `jobRunner.test.js` covers: exit 0 with a CSV gives `done` with `/results/<id>.csv`; exit 0 without a CSV gives `error`; a non-zero exit gives `error` containing the stderr text; a spawn `error` event gives `error`.
  - `npm test` passes.

### B-T1: Fix CI so it actually runs our tests

- **Category / PR title:** `Tests: CI now runs mvn test and the server tests`
- **Branch:** `tests/fix-ci-workflow`
- **Depends on:** nothing to start. **Merge after B-T2**, so the Java tests are green.
- **May touch:** `.github/workflows/run-tests.yml` only (rewriting or renaming it is fine, as long as only one workflow file exists at the end).
- **Must NOT touch:** anything outside `.github/workflows/`.
- **The workflow must:**
  - Run on pull requests to `main` (and optionally on pushes to `main`).
  - Use current major versions of the checkout, Java setup and Node setup actions.
  - Set up **Java 21** (matching `pom.xml`) and run `mvn -B test` in `processor/`.
  - Set up Node and run `npm ci` then `npm test` in `server/`.
  - Fail the run if either suite fails. Keeping the "at least 15 tests" check is optional. If you keep it, make it read Maven's report rather than the old console output.
- **Done when:** the PR's own checks show both suites running and passing. Link the green run in the PR description.

### B-D2: Real README plus server JSDoc

- **Category / PR title:** `Documentation: new README and JSDoc for the server`
- **Branch:** `docs/readme-and-server-jsdoc`
- **Depends on:** B-T2, B-E1, B-R3 and B-T1 all merged. **Do this last.**
- **May touch:**
  - `README.md`
  - New `docs/part1-instructions.md`: move the original Part 1 assignment text here, unchanged.
  - **Comment-only** changes (JSDoc blocks) in `server/lib/jobStore.js`, `server/lib/validate.js`, `server/lib/jobRunner.js`, `server/routes/process.js`, `server/routes/thumbnail.js`, `server/routes/videos.js`, `server/app.js`, `server/config.js`.
- **Must NOT touch:** any executable line of code in `server/`; anything in `processor/`; any other file in `docs/`.
- **README must include:**
  - What the project does (one paragraph) and how it pairs with `centroid-finder-frontend`.
  - Prerequisites: Java 21, Maven, Node, and FFmpeg (for thumbnails).
  - Setup: `.env` from `.env.example`, building the JAR, and starting the server.
  - The API endpoints with example success and error responses (copy the frozen contract above).
  - The JAR's command line, the CSV format, and the exit codes from B-E1.
  - How to run each test suite, and what CI runs.
  - **The course AI rules section, kept visible near the top** (the "AI Used" commit rule).
  - A link to `docs/part1-instructions.md` and to the planning docs.
- **JSDoc must cover:** every exported function in `server/lib/` and every route handler: purpose, parameters, return value or response shapes, and errors. Explain *why* where it isn't obvious, for example the write-then-rename in `saveJob`.
- **Done when:** a teammate can clone, build, run and test using only the README. `npm test` still passes, since only comments changed in code files.

---

## Section 5: Why this split causes no conflicts

- sabinMas only edits inside `processor/` (plus `.gitignore`, which only B-T2 might touch). onjamm only edits inside `server/`, `.github/workflows/`, `README.md` and one new file in `docs/`. The two sets never overlap.
- Within each lane, tasks that share files are ordered: B-T2 before B-E1, and B-R3 before B-D2.
- The two lanes meet only through frozen contracts. B-E1 makes the JAR's exit codes honest, and B-R3 keeps reading them the same way it does today. Neither side changes the JAR's arguments, the CSV format, or the HTTP API.

## Backlog (not in Wave 5)

Everything else in `priorityImprovements.md` (B-R1, B-R2, B-E2, B-D1, B-P1, B-P2, B-S1, B-S2, B-B1, B-B2) is backlog. If you finish early and want to add one, add it to this file first with its own **May touch** list and owner, and get your partner's OK before starting.
