# Progress log: processing frames in parallel

Goal: make `videoprocessor.jar` use all CPU cores, while the CSV stays byte-for-byte the same.
Nothing here is committed; the students review and commit it themselves.

## Step 1: Setup
- Made the worktree `centroid-finder-proc-parallel` on the new branch `upgrade/proc-parallel` (from `server`).
- Read `CLAUDE.md`, `plan.md`, `VideoSummaryApp`, `FrameReader`, `JavaCVFrameReader`, `ImageCentroidTracker`, `CSVOutputWriter` and their tests.
- No files changed.

## Step 2: New class `ParallelFrameProcessor`
- File: `src/main/java/io/github/nadia5129/centroidfinder/ParallelFrameProcessor.java` (new).
- The calling thread reads frames one by one and reads each timestamp right after the frame.
- Each frame is copied (`copyImage`), because `JavaCVFrameReader` reuses one image for every frame.
- The copy goes to a fixed thread pool. Each worker thread makes its own `ImageCentroidTracker`
  from a `Supplier`, through a `ThreadLocal`.
- The futures wait in a queue, oldest first. When `2 × threads` frames are waiting, the reader stops and
  writes the oldest one first. That keeps the rows in order and keeps memory small.
- If a worker throws, the `IOException` names the frame, and the pool is always shut down (`finally`).
  The workers are daemon threads, so they can never keep the program running.
- Threads: `availableProcessors() - 1` (one core for the reader), at least 1. The optional
  `CENTROID_THREADS` environment variable can choose another number (for testing). No new arguments.

## Step 3: Wire it into `VideoSummaryApp`
- File: `VideoSummaryApp.java` (changed).
- The tracker is now built by the new `buildTracker(targetColor, threshold)`, once per worker thread.
- `processFrames(reader, trackerMaker, writer)` now takes a `Supplier<ImageCentroidTracker>` and calls
  `ParallelFrameProcessor`. The usage and the 4 arguments did not change.

## Step 4: Tests
- New: `ParallelFrameProcessorTest.java` (14 tests): rows in order when frames finish out of order,
  1 thread, frame copying with a fake reader that reuses one image, a worker that throws
  (IOException, names the frame, stops reading, no hang), empty video, one tracker per thread,
  at most 2 frames per thread in flight, 0 threads rejected, `copyImage`, and 5 thread-count tests.
- Changed: `VideoSummaryAppTest.java` (8 tests). The fake group finder now finds its answer from the
  frame number stored in pixel (0,0), because frames can arrive in any order. The test that used
  `assertSame` now checks that the finder gets a copy of each frame. Added a `buildTracker` test.
- Checked the copy tests really work: with the copy removed, 2 tests fail. With it back, all pass.
- `mvn test`: 128 tests, 0 failures, 0 errors.

## Step 5: End-to-end check against the reference CSVs
- `mvn package`, then ran `target/videoprocessor.jar` and the original JAR on the 3 videos and compared
  each CSV with `cmp` and `shasum -a 256` against the reference CSVs.
- Another agent was benchmarking on the same machine, so the timings are noisy.

| Video | Original JAR | New JAR (14 workers) | New JAR, `CENTROID_THREADS=1` | CSV matches reference |
|---|---|---|---|---|
| salamander.mp4 (30 s) | 5.3 s | 1.7 s / 1.5 s | 5.5 s | yes (all runs) |
| bright_balloon (11 s, 1080p) | 7.6 s | 1.9 s / 1.9 s | 7.0 s | yes (all runs) |
| ensantina.mp4 (8 min) | 78.2 s | 17.6 s / 18.4 s | 95.9 s | yes (all runs) |

- Missing input file: exit code 1 and `Error processing video: ...`, same as before.

How to repeat the manual check (from `processor/`):
```
mvn package
java -jar target/videoprocessor.jar <video> out.csv 420500 60
shasum -a 256 out.csv <reference.csv>     # the two hashes must be the same
```

Notes on the timings:
- With 1 worker thread the new JAR is about as fast as the original, so the speed-up really comes from the extra threads.
- The 8-minute video got about 4x faster, not 14x. One likely reason: the reader thread decodes, converts and
  copies every frame by itself, and it may now be the slowest part. If the per-frame code gets faster, this
  limit matters more.

## Per-frame speed-ups (done together with the student, after the parallel work)
All CSVs stayed byte-for-byte identical to the original JAR's after every step.

| Step | Change | Files | ensantina.mp4, 14 threads | 1 thread |
|---|---|---|---|---|
| start | parallel frames only | | 18 s | 96 s |
| 1 | binarizer reads a whole row with one `getRGB` call | DistanceImageBinarizer | 17 s | 67 s |
| 2 | frames copied with one `System.arraycopy` of the BGR `byte[]` (was `copyData`, 0.91 ms -> 0.06 ms per frame); fixed the fallback copy for sub-images | ParallelFrameProcessor | 8.2 s | - |
| 3 | binarizer reads the BGR bytes directly for TYPE_3BYTE_BGR frames (other images keep the getRGB path) | BgrImages (new), DistanceImageBinarizer | 6.5 s | 27 s |
| 4 | DFS uses an `int[]` stack of pixel indexes instead of `Coordinate` objects on an `ArrayDeque` | DfsBinaryGroupFinder | 6.7 s | 15 s |

Original JAR: 77 s. With 14 threads the limit is now decoding on the single reader thread.
New tests: 5 copy tests (ParallelFrameProcessorTest), BgrImagesTest (5),
DfsBinaryGroupFinderEquivalenceTest (4, compares with OriginalDfsBinaryGroupFinder, a copy
of the original kept in the test folder). `mvn test`: 142 tests, 0 failures.
