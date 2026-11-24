# centroid-finder

Team project (3 students) with two parts:
- `processor/`: a Java video processor that tracks the largest colored centroid in an MP4 video and writes a CSV of `seconds,x,y`. Its design lives in `plan.md`; follow it.
- `server/`: an Express + TypeScript server that implements the Salamander API by running `videoprocessor.jar`. Its design lives in `serverPlan.md`; follow it.

## Layout
- `.env` (project root): paths the server needs, like the videos folder and the JAR. Read it with `dotenv`; never hard-code these paths.
- Java package: `io.github.nadia5129.centroidfinder` (every Java file starts with this package line)
- Java program code: `processor/src/main/java/io/github/nadia5129/centroidfinder/`
- Java tests: `processor/src/test/java/io/github/nadia5129/centroidfinder/` (never put `*Test.java` in `src/main`)
- Video library: JavaCV (`org.bytedeco:javacv-platform`). Only `JavaCVFrameReader` should use JavaCV; everything else goes through the `FrameReader` interface.
- Server code: `server/` (`package.json`, `tsconfig.json`)

## Commands
- Java (run from `processor/`):
  - Run tests: `mvn test` (must pass before any Java change is considered done)
  - Build the JAR: `mvn package` → `processor/target/videoprocessor.jar`
  - Run the app: `mvn compile exec:java -Dexec.args="..."` (main class is set in `pom.xml`)
- Server (run from `server/`):
  - Install packages: `npm install`

## Rules
- The server must call the JAR to process videos. Never put graph search / centroid logic in the server code.
- Don't change the existing centroid-finder Java classes except to fix bugs. New behavior goes in new code.
- Reuse the existing classes (`BinarizingImageGroupFinder`, `DistanceImageBinarizer`, `EuclideanColorDistance`, `DfsBinaryGroupFinder`, `Group`, `Coordinate`) instead of rewriting them.
- Write JUnit 5 tests for every new Java class. Use fakes/stubs for dependencies, not mocking libraries. Follow the existing test style: `method_condition_expectedResult` names, Arrange / Act / Assert comments.
- Use real TypeScript types in the server; avoid `any`.
- Keep comments short and beginner-friendly, matching the existing code.
- Never commit `.mp4`/`.mov` videos (except the existing `processor/sampleInput/bright_balloon_sample_video.mp4`), `.jar` files, or `node_modules/`.
- Don't commit or push; the students review and commit every change themselves.
- The students must be able to explain all code, so prefer simple, readable code over clever code.
