# Priority Improvements

The top two improvements in each category, picked by Paris, Nadia and Jessica from `humanImprovements.md` and `robotImprovements.md`.

# Refactoring code
1. **Move `/thumbnail` and `/api/videos` out of `app.ts`.** They're still written directly in `app.ts`. Every other endpoint is its own `xRoute(config)` file, so move these two out to match. *(robot)*
2. **Remove leftover files and dead code.** Nothing uses `JavaCVExperiment.java` or `processor/extractedFrame.png`. The experiment imports `ImageIO` and `File` but never uses them, and `pom.xml` still has two commented-out `<mainClass>` lines. *(human + robot)*

# Adding tests
1. **`NeonJobStore`'s write functions.** Test `createJob`, `markDone` and `markError` against a fake pool. Right now its tests only cover `listJobs` and `findJob`. *(human + robot)*
2. **`ImageCentroidTrackerTest` and `Group.compareTo`.** The tracker test's names aren't `method_condition_expectedResult` and it has no Arrange / Act / Assert comments. Nothing tests `Group.compareTo` directly, even though the tracker depends on the biggest group coming first. *(robot)*

# Improving error handling
1. **Validate `POST /process` before starting a job.** A bad threshold (`"abc"`, `"12.5"`) gives a 500 instead of a 400, the color isn't checked, and the video isn't checked to be a real video file. Use the same rules as `ArgumentParser` and send a 400. *(human + robot)*
2. **Stop a dropped database connection from crashing the server.** The pg Pool has no error listener, so a dropped Neon connection can crash the server. `index.ts` also doesn't check that `DATABASE_URL` is set. *(human + robot)*

# Writing documentation
1. **Update the README's server section.** Remove the leftover lines, and add the `.env` setup, installing ffmpeg, how to run the server and its tests, and the list of endpoints. *(human + robot)*
2. **Javadoc for `ImageCentroidTracker`, and fix `BinaryGroupFinder`'s Javadoc.** The tracker returns `(-1, -1)` when there are no groups, and that isn't written down anywhere. `BinaryGroupFinder` says ties break by y then x, but `Group.compareTo` checks x first. *(human + robot)*

# Improving performance (optional)

# Hardening security (optional)

# Bug fixes (optional)

# Other (optional)
