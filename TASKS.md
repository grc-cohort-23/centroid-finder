# Task Plan: centroid-finder backend (Wave 6: Polish and new features)

Goal: make the project presentable and add the features we chose in our three planning docs, on one branch per task, with Mason (`sabinMas`) and Drew (`onjamm`) and their AI agents working at the same time without ever editing the same file on two open branches.

Wave 5 (refactor, tests, errors, docs) is finished and merged. This file replaces the Wave 5 plan, which is still in git history. The unfinished backlog items from `priorityImprovements.md` that are still worth doing are carried over as P3 tasks below.

Planning docs this plan comes from (Google Docs):
- `polish.md`: https://docs.google.com/document/d/1N_2zOiaBYQEuWfaEvRkixsmPV8f9JIyet6B6DlBLOKg
- `goals.md`: https://docs.google.com/document/d/1wkgRvXCg3f1Z22FyMVEgtCJ7uVUjwnuEU91JfHBx29E
- `ideas.md`: https://docs.google.com/document/d/14McXyMaQQZ2r0qkniSWUz9fkieG6hbORiUazEuf5tFo

The matching plan for the frontend is in `sabinMas/centroid-finder-frontend/TASKS.md`. Sections 0, 1, 2, 7 and 8 are the same in both.

**This file is strict.** An agent working on a task may only create, edit, or delete the files listed under that task's **May touch**. Anything under **Must NOT touch**, or anything not listed at all, is off limits. If a task can't be finished without touching another file, the agent **stops and asks its human** (and tells the partner through the pair relay). It does not work around the rule.

Handles used below: **Mason = `sabinMas`**, **Drew = `onjamm`**.

---

## Section 0: Rules for every task (both people, both agents)

1. **One task = one branch = one PR.** Use the exact branch name given. Branch from the latest `main`:
   ```
   git checkout main && git pull
   git checkout -b <branch-name>
   ```
2. **Only the files listed under "May touch".** No drive-by fixes, no reformatting other files, no renames. If you spot another problem, put it in the PR description under "Noticed, not fixed". A file that a task **creates** belongs to that task's owner for the rest of Wave 6, even inside the other person's folder (for example a new component in `src/components/`). The only planned handover is the stub pages in F-N1.
3. **Dependencies.** Mason approved the free **Supabase** (database, sign-in) and **Vercel** (frontend hosting) route on 2026-11-25. That approves `@supabase/supabase-js` in `server/` (B-DB2) and in the frontend (F-A1), and nothing else. Mason also approved `multer` 2.x for uploads in `server/` (B-U1) on 2026-11-25, and said OpenCV may be added if a task really needs it (the Java processor already bundles it through JavaCV; if a task wants a new OpenCV dependency anywhere, say which artifact and why in the PR). Any other new dependency: say why in the PR and ask your human first.
4. **Contracts.** Every route, query parameter, status code and JSON shape that exists today stays exactly as it is. Wave 6 only adds what Section 2 lists. Changing Section 2 needs both humans to agree, and the same edit made to this section in both repos.
5. **Commits.** Small and frequent, on your own branch. The `AI Used` prefix was a Wave 1 rule and is no longer required (Mason, 2026-11-25). Every commit still has your human as author and the other partner as a `Co-authored-by:` trailer (see CLAUDE.md section 1). Keep README changes in their own human-written commits (rule 13).
6. **Before opening a PR.**
   - Backend: `mvn -B test` in `processor/` if you touched `processor/`, and `npm test` in `server/` if you touched `server/`.
   - Frontend: `npm run lint`, `npm test` and `npm run build`, plus `npm run test:e2e` if you touched `src/pages`, `src/components` or `tests/e2e`.
7. **PR.** From your branch into `main`. The title starts with the category given in the task. The description has: what changed, why, how you tested it (exact commands), and "Noticed, not fixed".
8. **Review.** The *other* partner reviews and comments **LGTM** when satisfied (an approval carries only that text). Nobody approves their own PR. Mason merges his own PRs after Drew's LGTM. Mason's agent may review, approve and squash-merge Drew's PRs. Drew's agent asks Drew first.
9. **After any PR merges into `main`,** everyone with an open branch merges `main` into it before continuing:
   ```
   git checkout <your-branch> && git merge main
   ```
10. **One open branch per agent per repo.** Take tasks in the merge order of Section 4. Different repos can run at the same time.
11. **Never delete the branches the professor grades.** Backend: `main`, `server`, `video`, `video-agentic-approach`. Frontend: `main`, `onjamm-dev`, `sabinmas-dev`.
12. **Secrets.** Supabase keys live only in `.env` files, which are gitignored. Share them through a password manager, never through chat, the pair relay, a commit or a PR. The **service-role key** is server-only and must never reach the frontend or any `VITE_` variable.
13. **README is human-written.** Our `polish.md` says the README must not be AI-written, because recruiters spot that style. For the two README tasks (B-D3, F-D1) the agent does **not** draft prose. It may run commands to check the README's steps, check links, and leave review comments. Commits to a README contain only human-written text.
14. **Where new features land in the PR title.** New features use the `Other:` prefix (Mason confirmed, decision D1). Their branch prefix is `other/` (as used below) or `feature/`.

---

## Section 1: Wave 6 at a glance (same table in both repos)

Sources: `polish.md`, `goals.md` and `ideas.md` (Google Docs, linked from the top of this file), plus the unfinished backlog from `priorityImprovements.md`. Wave 5 is finished: all 8 PRs merged (list in CLAUDE.md section 9).

Priority: **P1** = required by `polish.md` or marked a priority by Mason. **P2** = "Easy" and "Goal" items from `goals.md`. **P3** = hosting and the hygiene backlog.

| Feature | Pri | Backend tasks | Frontend tasks |
|---|---|---|---|
| Human-written README + demo GIF | P1 | B-D3 (Drew) | F-D1 (Mason) |
| Remove unneeded files | P1 | B-R1 (Mason) | F-R3 (Mason) |
| **File picker: upload your own video** (+ phone camera) | P1 | B-S1, B-U1 (Drew) | F-API1 (Mason), F-U1 (Drew) |
| **Jobs database + jobs table** (replaces the plain recent-jobs list) | P1 | B-DB1, B-DB2, B-J1 (Drew), S-0 (Mason) | F-API1 (Mason), F-J1 (Drew) |
| **Export formats** (CSV, JSON, TSV, PNG trajectory) | P1 | none (done in the browser) | F-X1 (Mason) |
| Color picker no longer covers the object | P2 | none | F-C1 (Drew) |
| Settings page + Help/Docs panel | P2 | none | F-ST1 (Mason) |
| UI/UX polish | P2 | none | F-TH1 (Mason), F-UX1 (Drew) |
| Metadata extraction + geolocation tag | P2 | B-M1 (Drew) | F-API1 (Mason), F-M1 (Drew) |
| Legacy vs new behaviors in menu tabs | P2 | none | F-N1 (Mason) |
| Sign-in + persistent history | P2 | B-A1 (Drew), S-0 (Mason) | F-A1, F-A2 (Mason) |
| Mobile-first + phone camera | P2 | none | F-TH2 (Mason), F-MB1 (Drew), F-U1 (Drew) |
| Hosting (**parked: local only for now**, decision D2) | P3 | B-H1 (parked) | F-H1 (parked) |
| Hygiene backlog (tests, perf, docs, security, bugs) | P3 | B-R2, B-B1, B-P1, B-B2, B-P2, B-D1 (Mason); B-E2, B-S2 (Drew) | F-S1, F-S2, F-P1, F-O1, F-B1 (Mason); F-E2, F-R2, F-P2, F-B2, F-T2, F-D2 (Drew) |

Not assigned (backlog, Section 8): live stream + live stats, AI real-time readings, sound tracking in silent videos, full AR overlay.

---

## Section 2: Contract additions (the new halves are built in parallel against this)

Everything under `/api` is already proxied by the frontend's `vite.config.js`, so **no proxy change is needed** and `vite.config.js` is not touched by any feature task.

### Sign-in header (optional everywhere)

- The frontend sends `Authorization: Bearer <Supabase access token>` when the user is signed in. Guests send nothing and everything keeps working.
- The server sets `req.user = { id, email }` when the token is valid. A missing header means guest. A present but invalid or expired token gets `401 { "error": "Invalid or expired sign-in" }`.
- When a signed-in user starts a job, the job is saved with their user id.

### New routes

| Method | Path | Success | Errors (always `{ "error": "..." }`) |
|---|---|---|---|
| POST | `/api/videos` (multipart form, one file in the field `video`) | 201 `{ "filename": "<stored name>" }` | 400 no file or extension not allowed, 413 too large, 500 |
| GET | `/api/videos/:filename/metadata` | 200 `VideoMetadata` (below) | 400 bad name, 404 `Video not found`, 500 ffprobe failed |
| GET | `/api/jobs` | 200 array of `JobSummary` (below), newest first, at most 100, only the signed-in user's | 401 `Sign in to see saved jobs` (guests), 500 |
| POST | `/process/:filename?...` (existing) | adds **429** `Too many jobs running, try again shortly` when the cap in B-S2 is reached | existing codes unchanged |

The stored filename is the sanitized original name. If it already exists, the server adds a suffix, so an upload never overwrites a video. The response tells the frontend which name was used.

```
VideoMetadata = {
  filename: string,
  durationSeconds: number | null,
  width: number | null, height: number | null,
  frameRate: number | null,
  videoCodec: string | null,
  bitRate: number | null,
  sizeBytes: number,
  createdAt: string | null,            // ISO 8601 from the container tags
  location: { latitude: number, longitude: number, altitude: number | null } | null,
  tags: { [key: string]: string }      // raw container tags, strings only
}

JobSummary = {
  jobId: string, filename: string, targetColor: string, threshold: number,
  status: "processing" | "done" | "error",
  result: string | null,               // "/results/<jobId>.csv" when done
  error: string | null,
  createdAt: string, updatedAt: string // ISO 8601
}
```

### Jobs table (Supabase Postgres), built in B-DB2

`jobs(id uuid primary key, user_id uuid null references auth.users, filename text not null, target_color text not null, threshold int not null, status text not null check (status in ('processing','done','error')), result text null, error text null, created_at timestamptz not null default now(), updated_at timestamptz not null default now())`. The server talks to it with the service-role key and always filters by the verified user id. Row level security is switched on as a second layer: users may read only their own rows.

### Unchanged on purpose

`GET /api/videos`, `GET /thumbnail/:filename`, `POST /process/:filename`, `GET /process/:jobId/status`, `/results/*.csv`, the JAR arguments, the CSV format and the exit codes. The frontend's existing `src/api/*` function names and `findLargestCentroid` keep their signatures. New functions may be added.

---

## Section 3: Ownership map (this repo)

| Area | Owner |
|---|---|
| `processor/**` (Java source, Java tests, `pom.xml`, `sampleInput/`, `sampleOutput/`) and `.gitignore` | **Mason** |
| Root clutter and planning docs, only for B-R1: `output.csv`, `notes.md`, `options.md`, `plan.md`, `serverPlan.md`, `goals.md`, `ideas.md`, `polish.md`, `humanImprovements.md`, `robotImprovements.md`, `priorityImprovements.md`, and the new folder `docs/planning/**` | **Mason** |
| `server/**`, `.github/workflows/**`, `README.md`, `.env.example`, `Dockerfile`, `.dockerignore`, and `docs/**` except `docs/planning/**` | **Drew** |
| `TASKS.md`, `LICENSE` | nobody (read-only) |

No file is owned by both people. The two lanes meet only through the contracts in Section 2.

## Section 4: Merge order

### Lane order in this repo

```
Mason:  S-0 (manual, no code)      B-R1 -> B-B1 -> B-R2 -> B-P1 -> B-B2 -> B-P2 -> B-D1
Drew:   B-S1 -> B-U1 -> B-DB1 -> B-DB2 -> B-A1 -> B-J1 -> B-M1 -> B-E2 -> B-S2 -> B-D3      (B-H1 is parked)
```

Drew's server tasks all touch `server/app.js`, so they go strictly one after another. Mason's Java tasks share files too (`VideoSummaryApp.java`, `FfmpegVideoFrameGrabber.java`, `SamplingVideoGroupFinder.java`), so they also go in order.

### Cross-repo gates (what has to merge before something else can start or be checked)

| Gate | Needed by |
|---|---|
| Frontend F-API1 and F-N1 merged (both Mason, both small and early) | Drew's frontend F-U1, F-J1, F-M1 |
| S-0 stage 1 done (Supabase project exists, keys in local `.env`) | B-DB2 live check, B-A1 live check, frontend F-A1 live check. Code and tests use fakes, so writing the code does not wait. |
| B-R1 merged (planning docs moved to `docs/planning/`) | B-D3 (README links point to the new places) |
| B-U1, B-J1, B-M1 merged | Real end-to-end check of the matching frontend tasks. Their Playwright specs mock the API, so the frontend PRs do not wait. |
| B-R1 and the features merged | B-D3 (README goes last) |

---

## Section 5: Mason's tasks (this repo)

**You own:** `processor/**`, `.gitignore`, and the root clutter files listed in Section 3 (for B-R1 only).
**You must NOT touch:** `server/**`, `.github/**`, `README.md`, `.env.example`, `Dockerfile`, `docs/**` (except `docs/planning/**`).

### S-0: Set up the Supabase project (manual, no code)

- **Priority:** P1 (the jobs table and sign-in need it). **Owner:** Mason. **Depends on:** decision D9.
- **Stage 1** (do first): create a free Supabase project. Turn on email + password sign-in. Copy the project URL, the anon key and the service-role key. Put them in your local `.env` files (backend: `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`; frontend: `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY`). Give Drew the same values through a password manager.
- **Stage 2** (after B-DB2 merges): run `docs/supabase-schema.sql` in the Supabase SQL editor to create the `jobs` table and its row level security policies.
- **Done when:** both partners can start the server with the keys and `GET /api/jobs` works for a signed-in test user. Never paste keys into chat, the relay, a PR or a commit.
- Agents: you can explain the steps, but you do not log in to anyone's account and you never print a key.

### B-R1: Remove dead code and unneeded files

- **Category / PR title:** `Refactoring: removed Experiment.java and moved planning docs into docs/planning/`
- **Priority:** P1 ("Removing unneeded files" in `polish.md`). **Branch:** `refactoring/remove-dead-code`. **Depends on:** nothing. Merge first in this lane.
- **May touch:**
  - `processor/src/main/java/Experiment.java`: delete.
  - `processor/pom.xml`: the `exec-maven-plugin` block only (it points at `Experiment`).
  - `output.csv`: remove from git (`git rm`). Never commit a regenerated copy.
  - Move into `docs/planning/`: `notes.md`, `options.md`, `plan.md`, `serverPlan.md`, `goals.md`, `ideas.md`, `polish.md`, `humanImprovements.md`, `robotImprovements.md`, `priorityImprovements.md`. `TASKS.md` and `README.md` stay in the root.
  - The unused import in `EuclideanColorDistance.java` and the leftover comments in `DfsBinaryGroupFinder.java`.
  - `.gitignore`: only if a moved or removed file needs it.
- **Check before deleting (ask Mason for each):** `processor/sampleOutput/binarized.png`, `processor/sampleOutput/groups.csv` and `processor/sampleInput/squares.jpg`. Search the repo and the tests first. If a test or doc uses it, keep it.
- **Must NOT touch:** `README.md`, anything in `server/`, `docs/part1-instructions.md`.
- **Done when:** `mvn -B test` passes, `mvn -DskipTests package` still builds the JAR, and the PR lists every moved and deleted path so Drew can fix the README links in B-D3.

### B-B1: Centroid sums can overflow

- **Category / PR title:** `Bug fix: centroid sums no longer overflow on large frames`
- **Priority:** P3. **Branch:** `bugfix/centroid-sum-overflow`. **Depends on:** B-R1 merged (same file).
- **May touch:** `processor/src/main/java/DfsBinaryGroupFinder.java`, `processor/src/test/java/DfsBinaryGroupFinderTest.java`.
- **Do:** the x and y sums are `int`. One large group on a 4K frame overflows. Use `long` for the sums, keep the result a `Coordinate`. Write the failing test first (a group big enough to overflow an `int` sum).
- **Done when:** the new test fails on `main` and passes on your branch. `mvn -B test` passes.

### B-R2: Extract an ArgumentParser

- **Category / PR title:** `Refactoring: extracted ArgumentParser from the main methods`
- **Priority:** P3. **Branch:** `refactoring/extract-argument-parser`. **Depends on:** B-B1 merged.
- **May touch:** `processor/src/main/java/VideoSummaryApp.java`, `ImageSummaryApp.java`, new `ArgumentParser.java`, `processor/src/test/java/VideoSummaryAppTest.java`, new `ArgumentParserTest.java`.
- **Rules:** exit codes (1 wrong count, 2 bad threshold, 3 bad color) and the argument order do not change. Add or extend tests first, then move the code.
- **Done when:** the existing exit-code tests pass unchanged, and `ArgumentParserTest` covers each bad-argument case.

### B-P1: Convert only the frames we sample

- **Category / PR title:** `Performance: skip converting frames that aren't sampled`
- **Priority:** P3. **Branch:** `performance/skip-unsampled-frames`. **Depends on:** B-R2 merged.
- **May touch:** `processor/src/main/java/FfmpegVideoFrameGrabber.java`, `SamplingVideoGroupFinder.java`, `VideoFrameGrabber.java`, and their tests.
- **Do:** today every decoded frame becomes a `BufferedImage`, but only about 1 in 15 is used. Convert only the sampled ones. The CSV output must not change.
- **Done when:** the end-to-end test on `ball.mp4` still passes with the same CSV, and you report a before and after timing in the PR.

### B-B2: Sampling after a timestamp gap

- **Category / PR title:** `Bug fix: sampling no longer bursts after a timestamp gap`
- **Priority:** P3. **Branch:** `bugfix/sampling-gap-behavior`. **Depends on:** B-P1 merged (same file).
- **May touch:** `processor/src/main/java/SamplingVideoGroupFinder.java`, `processor/src/test/java/SamplingVideoGroupFinderTest.java`.
- **Do:** after a gap the sampler emits several rows back to back until it catches up, so rows can be closer than 0.5 s. A test locks this in. Decide with Mason whether that is intended. If not, change the behavior and the test together.
- **Done when:** the decision is written in the PR and the tests match it.

### B-P2: No per-pixel objects in color distance

- **Category / PR title:** `Performance: removed per-pixel allocations from color distance`
- **Priority:** P3. **Branch:** `performance/color-distance-allocations`. **Depends on:** B-B2 merged.
- **May touch:** `processor/src/main/java/EuclideanColorDistance.java`, `DistanceImageBinarizer.java`, and their tests.
- **Do:** `EuclideanColorDistance` creates two `Color` objects and arrays per pixel. Compare packed RGB ints instead. Keep the current tests green as the safety net.
- **Done when:** all tests pass, outputs are identical, and the PR shows a timing before and after.

### B-D1: JavaDoc fixes

- **Category / PR title:** `Documentation: fixed the sort-order JavaDoc and added missing class docs`
- **Priority:** P3. **Branch:** `docs/javadoc-fixes`. **Depends on:** B-P2 merged. Do this last in the Java lane.
- **May touch:** comment-only changes in `processor/src/main/java/*.java`.
- **Do:** `BinaryGroupFinder`'s sort-order doc contradicts itself, `DfsBinaryGroupFinder` is named "Dfs" but the original review found it behaves like BFS (check the code, then fix the name or the doc), several classes have no class JavaDoc, `ImageBinarizer` says `x000000`.
- **Done when:** no executable line changed (`git diff` shows only comments) and `mvn -B test` passes.

---

## Section 6: Drew's tasks (this repo)

**You own:** `server/**`, `.github/workflows/**`, `README.md`, `.env.example`, `Dockerfile`, `.dockerignore`, and `docs/**` except `docs/planning/**`.
**You must NOT touch:** `processor/**`, `.gitignore`, the root planning files in Section 3.

Every task: JSDoc on every exported function and every route handler (CLAUDE.md section 6), and tests that run **without Java, ffmpeg or a Supabase account** (use injected fakes, like `spawnFn` in `runJob`).

### B-S1: Tighter filename and threshold validation

- **Category / PR title:** `Security: allow only video extensions and cap the threshold`
- **Priority:** P1 (uploads build on it). **Branch:** `security/tighter-input-validation`. **Depends on:** nothing.
- **May touch:** `server/lib/validate.js`, `server/test/validate.test.js`, `server/routes/videos.js` (to share the extension list).
- **Do:**
  - `isSafeFilename` also rejects names that start with a dot (like `.env`) and any extension outside `.mp4 .mov .avi .mkv .webm`. Export one `VIDEO_EXTENSIONS` list and use it in `videos.js` too.
  - `isValidThreshold` caps the value. The largest possible RGB distance is about 441, so allow 0 to 441 and say so in the PR.
- **Done when:** tests cover dot-files, wrong extensions, a path-traversal name, threshold 0, 441 and 442. `npm test` passes.

### B-U1: Upload your own video (the file picker's backend)

- **Category / PR title:** `Other: added an upload route so users can process their own videos`
- **Priority:** **P1, Mason's stated priority.** **Branch:** `other/video-upload`. **Depends on:** B-S1 merged. (`multer` 2.x is approved, decision D3.)
- **May touch:** new `server/routes/upload.js`, `server/app.js` (mount it), `server/config.js` (`MAX_UPLOAD_MB`, default 200), `server/package.json` and `server/package-lock.json` (add `multer` 2.x only), `.env.example`, new `server/test/upload.test.js`.
- **Do:** implement `POST /api/videos` exactly as in Section 2.
  - One file in the field `video`. Reject anything whose extension is not in `VIDEO_EXTENSIONS`.
  - Sanitize the name (use `isSafeFilename` on the result). Never overwrite: add a numeric suffix if the name exists.
  - Save into `VIDEOS_DIR`. Enforce the size limit (413). Delete a partly written file if the upload is aborted.
  - Success is 201 `{ filename }`. The new file must then show up in `GET /api/videos` and work with `/thumbnail/:filename` and `POST /process/:filename` unchanged.
- **Done when:** supertest tests cover success, no file, wrong extension, too large, a name collision, and a path-traversal name. Manual check: upload a real phone video and process it.
- **Note:** we run local only for now (decision D2), so uploads stay on the developer's disk. If hosting is revived, free hosts often wipe their disk on redeploy and uploads would disappear.

### B-DB1: Put the job store behind an interface

- **Category / PR title:** `Refactoring: job storage sits behind an interface so the database can plug in`
- **Priority:** P1. **Branch:** `refactoring/job-store-interface`. **Depends on:** B-U1 merged.
- **May touch:** `server/lib/jobStore.js`, `server/lib/jobRunner.js`, `server/routes/process.js`, `server/app.js`, `server/index.js`, and the files in `server/test/` (adapt calls, add `jobStore.test.js` if missing). No new dependency.
- **Do:**
  - Turn the three functions into `createFileJobStore({ jobsDir })` returning `{ saveJob, getJob, updateJob, listJobs }`, all returning promises. The file store keeps the write-then-rename behavior.
  - `createApp` receives the store (default: the file store). Routes and `runJob` use the injected store.
  - **`runJob` stays synchronous** (a spawn failure must still become the route's 500). Only the store calls inside its event handlers are awaited. The route awaits `saveJob` *before* calling `runJob`.
  - Add the optional `userId` field to saved jobs. It is `null` for now.
- **Done when:** HTTP behavior is identical, the old tests pass with only the changes needed for the new call style (list them in the PR), and `listJobs({ userId, limit })` is tested on the file store.

### B-DB2: Supabase job store

- **Category / PR title:** `Other: jobs are stored in a Supabase database`
- **Priority:** P1. **Branch:** `other/supabase-job-store`. **Depends on:** B-DB1 merged. S-0 stage 1 for the live check.
- **May touch:** new `server/lib/supabaseJobStore.js`, `server/config.js`, `server/index.js` (pick the store from the environment), `server/package.json` and `package-lock.json` (add `@supabase/supabase-js` only), `.env.example`, new `docs/supabase-schema.sql`, new `server/test/supabaseJobStore.test.js`.
- **Do:**
  - `createSupabaseJobStore({ client })` with the same four functions as the file store, mapped to the `jobs` table in Section 2. Convert snake_case columns to the camelCase `JobSummary` shape.
  - If `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` are both set, use it. Otherwise use the file store, so local development and every test need no account.
  - `docs/supabase-schema.sql` creates the table and the row level security policies.
- **Done when:** tests use a fake client and cover save, get, update, list ordering and limit, and a Supabase error becoming a clear rejected error. Live check after S-0 stage 2: start a job, see the row in Supabase, and read it back through `/process/:jobId/status`. The service-role key appears in no log line.

### B-A1: Optional sign-in on the server

- **Category / PR title:** `Other: the server recognizes signed-in users and saves their jobs`
- **Priority:** P2. **Branch:** `other/server-auth`. **Depends on:** B-DB2 merged.
- **May touch:** new `server/lib/auth.js`, `server/app.js`, `server/routes/process.js`, `server/lib/jobRunner.js` and the job stores (only to pass `userId` through), new `server/test/auth.test.js`.
- **Do:** an `optionalAuth` middleware as described in Section 2. Make the token check injectable (`verifyToken(token)`), with the Supabase `auth.getUser(token)` call as the real one. `POST /process` saves `userId`. Guests keep working.
- **Done when:** tests cover no header (guest), a valid token, an invalid token (401 with the exact message), and that a job saved by a signed-in user carries their id. The token is never logged.

### B-J1: List my jobs

- **Category / PR title:** `Other: added GET /api/jobs for the signed-in user's saved jobs`
- **Priority:** P1. **Branch:** `other/jobs-list-route`. **Depends on:** B-A1 merged.
- **May touch:** new `server/routes/jobs.js`, `server/app.js`, the job stores (only `listJobs` if B-DB1 left a gap), new `server/test/jobs.test.js`.
- **Do:** `GET /api/jobs` as in Section 2. A user sees only their own jobs.
- **Done when:** tests cover a guest (401 with the exact message), a user with jobs (newest first, max 100), a user with none (`[]`), and that user A can never see user B's jobs.

### B-M1: Video metadata and location

- **Category / PR title:** `Other: added GET /api/videos/:filename/metadata with the location tag`
- **Priority:** P2. **Branch:** `other/video-metadata`. **Depends on:** B-S1 merged. Decisions D5 and D6.
- **May touch:** new `server/lib/probe.js`, new `server/routes/metadata.js`, `server/app.js`, new `server/test/probe.test.js` and `server/test/metadata.test.js`.
- **Do:**
  - `probeVideo(path, { execFileFn })` runs `ffprobe -v error -print_format json -show_format -show_streams` through `execFile` with an argument array (no shell). The fake `execFileFn` makes tests need no ffprobe.
  - A pure `parseLocation(tag)` turns an ISO 6709 string such as `+47.6062-122.3321/` or `+47.6062-122.3321+012.000/` into `{ latitude, longitude, altitude }`, and returns `null` for anything else.
  - Build `VideoMetadata` from Section 2. Missing fields are `null`, never made up.
  - Check the filename with `isSafeFilename` and that the file exists (404 `Video not found`). A failing ffprobe is a 500 with a short message, not the raw stderr.
- **Done when:** tests cover a normal file, a file with a location tag, a file without one, a garbage location string, a missing file, a bad name, and ffprobe failing.

### B-E2: Recover stuck jobs, friendlier errors

- **Category / PR title:** `Error handling: recover stuck jobs on startup and hide raw stderr`
- **Priority:** P3. **Branch:** `error-handling/server-job-recovery`. **Depends on:** B-M1 merged.
- **May touch:** `server/index.js`, `server/lib/jobStore.js`, `server/lib/supabaseJobStore.js`, `server/app.js`, `server/lib/jobRunner.js`, `server/test/`.
- **Do:** on startup, every job still `processing` becomes `error` with a short message ("The server restarted while this video was processing."). A job's `error` text is a short message plus the exit code. The full Java stderr (stack traces, paths) goes to the server log only. Add `markStuckJobsAsError()` to both stores.
- **Done when:** tests cover both stores and the runner, and a stuck job reads as `error` through the status route after a restart.

### B-S2: Cap concurrent jobs

- **Category / PR title:** `Security: limit how many processing jobs run at once`
- **Priority:** P3. **Branch:** `security/limit-concurrent-jobs`. **Depends on:** B-E2 merged.
- **May touch:** `server/lib/jobRunner.js`, `server/routes/process.js`, `server/config.js`, `.env.example`, `server/test/`.
- **Do:** at most `MAX_CONCURRENT_JOBS` (default 2) running at once. Over the limit, `POST /process` answers 429 `Too many jobs running, try again shortly` and does not save a job. The count goes down when a job ends, including when it errors.
- **Done when:** tests cover the limit, the 429, the count going back down after success and after failure.

### B-H1: Deploy the backend (PARKED, do not start)

**Parked by decision D2 (2026-11-25): local only for now.** The text below is kept so the task can be revived. Nobody takes it in Wave 6 unless Mason says so.

- **Category / PR title:** `Other: Docker setup and deploy notes for hosting the backend`
- **Priority:** P3. **Branch:** `other/backend-deploy`. **Depends on:** a new decision on D2 by Mason, and B-DB2 merged.
- **May touch:** new `Dockerfile`, new `.dockerignore`, new `docs/deploy.md`, `server/config.js` (read `PORT` from the environment if it does not already), `.env.example`, and `server/` code for the sign-in-to-upload setting from decision D4 if Mason chooses it.
- **Do:**
  1. First write `docs/deploy.md` with the host comparison from D2 (memory, disk, sleep behavior, cost). Stop and wait for Mason's pick.
  2. Then the `Dockerfile`: build the JAR with Maven, run on Node 20 with a Java 21 runtime and ffmpeg (which includes ffprobe).
  3. Document: the environment variables, the disk being wiped on redeploy (so uploads and CSVs vanish while the jobs table survives), and the memory the JVM needs.
- **Never** create the hosting account, push, or deploy from an agent. Mason does that, and approves each step.
- **Done when:** `docker build` and `docker run` work locally with the sample video, and `docs/deploy.md` lets a teammate redeploy from scratch.

### B-D3: README (human-written)

- **Category / PR title:** `Documentation: rewrote the README by hand with a demo GIF`
- **Priority:** **P1.** **Branch:** `docs/human-readme`. **Depends on:** B-R1 merged and the features done. Do this last.
- **Written by Drew, not by an AI** (Section 0 rule 13). The agent may run the README's commands to check they work, check every link, and leave review comments. It does not write or rewrite sentences.
- **May touch:** `README.md`, new `docs/` images or the GIF.
- **Write it for a recruiter or hiring manager** (`polish.md`): what the project does and why it is interesting, a GIF of the frontend working (recorded from the frontend, see F-D1), how to run it, the architecture in a few lines, what each of us built, and what we learned. Remove the old "AI only helps with tests" rules section (CLAUDE.md rule 9 says it is obsolete) and keep an honest note on how we used AI. Link to `docs/planning/` and `docs/part1-instructions.md`.
- **Done when:** a teammate can clone, build, run and test using only the README, and every link works.

---

## Section 7: Decisions needed (same list in both repos)

Each line has a **default**. An agent that reaches a task blocked by an open decision uses the default only if its human says so; otherwise it asks.

| # | Decision | Default if nobody objects | Blocks |
|---|---|---|---|
| D1 | PR label for new features. | **Decided 2026-11-25:** `Other:` in the PR title, branch prefix `other/` or `feature/`. | none |
| D2 | **Where the backend runs.** | **Decided 2026-11-25: local only for now.** B-H1 and F-H1 are parked. Reminder for later: Vercel can host the React app but not the Java 21 + ffmpeg backend, so that part would need a Docker host, and each host's free limits (memory especially) must be checked first. | nothing in Wave 6 |
| D3 | Add `multer` (2.x) to `server/` for uploads. | **Decided 2026-11-25: yes.** OpenCV may also be added if a task really needs it, with the reason in the PR. | none |
| D4 | Guests on a deployed site. | Locally everything works without signing in. On the deployed site, uploading requires sign-in, set by an env variable added in B-H1. | B-H1 |
| D5 | Metadata engine. | `ffprobe` from the server (it ships with ffmpeg, no new dependency). Java stays untouched. | B-M1 |
| D6 | What "geolocation tags" means (`goals.md` asks Drew's agent to revise this item). | Read the location tag if the file has one, show it and link to a map. We do not write tags into videos. | B-M1, F-M1 |
| D7 | Export formats. | CSV, JSON, TSV and a PNG of the path drawn over the first frame. No wav/mp3. | F-X1 |
| D8 | Sign-in method. | Supabase email + password. Sign-in stays optional, guest mode keeps working. | B-A1, F-A1, F-A2 |
| D9 | Who owns the Supabase project. | Mason creates it and shares keys with Drew through a password manager. | S-0 |
| D10 | UI direction (`goals.md`: "find some uniqueness"). | Before F-TH1 and F-UX1, the agent proposes two or three directions and the humans choose. No restyle before then. | F-TH1, F-UX1 |

---

## Section 8: Backlog (not assigned in Wave 6)

Add one of these to this file first, with its own **May touch** list and owner, and get your partner's OK before starting.

- **Hosting** (B-H1 Docker setup and deploy notes for the backend, F-H1 `vercel.json` for the frontend). Parked by decision D2 (local only for now). Both task texts stay in Section 5 or 6, marked PARKED. If we revive them, also decide guest access on a public site (uploads without sign-in) and where results are stored if the host wipes its disk.
- **Live stream capabilities and live stats** (distance, speed, geolocation overlay). Needs a new streaming path and a decision on the processor. `goals.md` calls it a stretch.
- **AI for real-time readings.** The biggest stretch.
- **Metadata for images** (EXIF). Needs a new Java or Node dependency.
- **Sonification export** (wav/mp3 from the path). A fun idea, decision D7 leaves it out.
- **Settings and history synced to the account** beyond what F-ST1 stores locally.
- **Results kept in Supabase Storage** instead of the server's disk. Needed if the chosen host has an ephemeral disk (B-H1 will say).
- **Sound tracking in silent videos.** Marked impossible in `goals.md` (out of scope for our equipment).
- **Full AR overlay.** Marked impossible in `goals.md`. A canvas drawn over the camera feed is possible, true AR is not reliable in browsers yet.
- **Single `/api` prefix for every route.** Couples both repos, leave until Wave 6 is done.
- **Java package restructure.** Touches every Java file, so only do it alone and first.
- Stretch ideas from Mason's Google Docs (pan-tilt servo tracker, phone livestream report app, AR walking guide). New projects, plan them with the humans first.

## Keeping the two files in sync

Sections 0, 1, 2, 7 and 8 are identical in `onjamm/centroid-finder` and `sabinMas/centroid-finder-frontend`. Sections 3 to 6 are about the repo they sit in. When a shared section changes, change it in both repos in the same session and tell the partner through the pair relay.
