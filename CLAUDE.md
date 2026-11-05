# centroid-finder

Team project (3 students) that tracks the largest colored centroid in an MP4 video and writes a CSV of `seconds,x,y`. The design lives in `plan.md`; follow it.

## Layout
- Package: `io.github.nadia5129.centroidfinder` (every file starts with this package line)
- Program code: `src/main/java/io/github/nadia5129/centroidfinder/`
- Tests: `src/test/java/io/github/nadia5129/centroidfinder/` (never put `*Test.java` in `src/main`)
- Video library: JavaCV (`org.bytedeco:javacv-platform`). Only `JavaCVFrameReader` should use JavaCV; everything else goes through the `FrameReader` interface.

## Commands
- Run tests: `mvn test` (must pass before any change is considered done)
- Run the app: `mvn compile exec:java -Dexec.args="..."` (main class is set in `pom.xml`)

## Rules
- Reuse the existing classes (`BinarizingImageGroupFinder`, `DistanceImageBinarizer`, `EuclideanColorDistance`, `DfsBinaryGroupFinder`, `Group`, `Coordinate`) instead of rewriting them.
- Write JUnit 5 tests for every new class. Use fakes/stubs for dependencies, not mocking libraries. Follow the existing test style: `method_condition_expectedResult` names, Arrange / Act / Assert comments.
- Keep comments short and beginner-friendly, matching the existing code.
- Never commit `.mp4`/`.mov` videos (except the existing `sampleInput/bright_balloon_sample_video.mp4`) or `.jar` files.
- Don't commit or push; the students review and commit every change themselves.
- The students must be able to explain all code, so prefer simple, readable code over clever code.
