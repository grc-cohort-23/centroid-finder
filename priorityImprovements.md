# Priority Improvements 
#At #Least #Six #Improvements #To #Be #Made

#### What are the top two improvements in each category? (We'll be making at least 1 from each category)

refactoring code

-  **Replace Legacy Java Collections with Modern Idiomatic Types:**
   `DfsBinaryGroupFinder` relies on `java.util.Stack` (a synchronized legacy class dating back to Java 1.0) and multiple `LinkedList` instances.
   *Direction:* Replace `Stack` with `ArrayDeque` (or primitive index arrays) and `LinkedList` with `ArrayList` to adhere to modern Java best practices and eliminate unnecessary synchronization overhead.
- **Revitalize The Frontend Experience, Remaking Our Frontend Code:**
`If we remake our frontend we can tackle our binarized image evaluation box, reducing visible buffering, eliminating ease of use concerns and additionally adding features to interface with new backend infrastructure.`

adding tests (required)

- **Expand Server Integration & Failure Tests:**
  * `server/test/api.test.js` tests happy paths with simulated processes, but lacks coverage for edge conditions.
  *Direction:* Add tests for server behavior when directories (`VIDEOS_DIR`, `OUTPUT_DIR`) do not exist, when malformed JSON bodies are received, when invalid job IDs are requested, and when disk write errors occur during job staging.

- **Edge Case and Boundary Testing for Image Algorithms:**
  * Add unit tests in `processor/src/test/` for extreme image dimensions (1x1, 0x0, non-square rectangular), all-black or all-white images, threshold edge values (0, 442, and out-of-bounds), and disconnected components with equal sizes.

improving error handling (required)

* **Signal Non-Zero Exit Code on CLI Failure (`VideoProcessorApp`):**
  * In `VideoProcessorApp.main`, when an exception occurs during video processing, the error is printed to `System.err`, but the program continues to the end of `main` and exits with code 0.
  * *Direction:* Ensure any uncaught processing exception results in a non-zero exit code (`System.exit(1)` or rethrown unchecked exception) so that calling processes (like the Express server or CLI scripts) can reliably detect failure via process exit codes rather than parsing stderr text with regex.

* **Replace Generic `Exception` with Specific Exception Types:**
  * Multiple classes (`VideoProcessorApp`, `VideoPreviewApp`, `VideoCentroidTracker`, `ImageSummaryApp`) use `catch (Exception e)`.
  * *Direction:* Catch and handle specific exceptions (`IOException`, `JCodecException`, `NumberFormatException`, `IllegalArgumentException`) and avoid catch-all `catch (Exception e)`.

writing documentation (required)


* **Server REST API Documentation:**
  * The Express server endpoints (`/api/videos`, `/api/preview`, `/jobs`, `/jobs/:jobId`) lack formal documentation.
  * *Direction:* Add JSDoc block comments or an `API.md` file describing route methods, URL params, JSON request payload schemas, HTTP status codes, and JSON response bodies.
* **Frontend Component Documentation:**
  * *Direction:* Add JSDoc / prop documentation for React components (`ProcessingJob`, `PreviewPage`, `VideoPicker`, `VideoFrame`, `withRequestStatus`) detailing expected props and component states.



---
# Optional Improvements

#### Top two improvements, once again

improving performance (optional)

hardening security (optional)

bug fixes (optional)

other (optional)
