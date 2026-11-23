Kelley's AI Suggestions:
- VideoExperiment is a prototype: no validation, hardcoded -1,-1, a redundant stop(), and it duplicates the real pipeline. Delete it or move it out of the shipped sources.
- main calls System.exit directly. Move the logic into a method that returns an exit code, and let main just exit with it. That makes the whole CLI testable.
- Tests to add (building on the earlier list)
VideoProcessor: Use a fake FrameSource and ResultWriter.
An empty source produces zero results.
N frames produce N writes, in order.
A source that throws midway, and a writer that throws, each propagate the exception.
Check that the processor doesn't close either collaborator.
JavaCvFrameSource:
Use a tiny checked-in video fixture, for example a short clip with a solid-colored square at a known position.
Test nonexistent and corrupt files, a zero or tiny step, NaN, and a step larger than the video.
- JavaCvFrameSource, VideoProcessorApp, and Group have no tests. These hold the infinite-loop risk, the silent-truncation behavior, and the System.exit problem from last round.

Kate's AI Suggestions:

##Refactoring
- Files: server.js — Separate route handling, video processing, and file operations into smaller modules.
- Files: Java processor — Break large methods into smaller methods with clear responsibilities.
- Files: Java processor — Replace repeated code with reusable helper methods.
- Files: Backend routes — Move repeated validation and response logic into shared functions.
- Files: Java processor — Replace hardcoded values with named constants or configuration.

##Bug Fixes
- Files: Video processing — Handle videos that cannot be opened or contain no readable frames.
- Files: Java processor — Handle frames where the target color is not detected.
- Files: CSV output — Ensure the output file is properly closed, even when processing fails.
- Files: Backend routes — Validate video IDs and file paths before processing.
- Files: Job status handling — Ensure failed or interrupted jobs do not remain marked as running.

##Adding Tests
- Files: Java processor — Test centroid calculations with different frame inputs.
- Files: Java processor — Test behavior when no target color is found.
- Files: Java processor — Test invalid, empty, or corrupted video files.
- Files: Backend routes — Test missing or invalid request parameters.
- Files: Job management — Test successful completion, failure, and status updates.

##Performance
- Files: Java processor — Avoid unnecessary image conversions and repeated calculations.
- Files: Video processing — Release frame and image resources as soon as they are no longer needed.
- Files: Backend routes — Avoid blocking requests while the Java processor runs.

##Error Handling
- Files: Java processor — Replace broad throws Exception declarations with specific exception types.
- Files: Java processor — Replace catch (Exception e) with more specific exception handling.
- Files: Backend routes — Return clear and consistent error responses.
- Files: Job management — Record processing failures so users can understand what went wrong.
- Files: File operations — Handle missing files, permission errors, and failed writes.

Additional cleanup: Review all uses of throws Exception and catch (Exception e) throughout the project.

##Security
- Files: Backend routes — Validate and sanitize user-provided file names and paths.
- Files: File upload handling — Restrict accepted file types and file sizes.
- Files: Backend routes — Prevent users from accessing files outside the intended video directory.
- Files: Configuration — Avoid exposing sensitive information or hardcoded credentials.

##Documentation
- Files: Java processor — Document the main processing workflow and centroid calculation.
- Files: Backend routes — Explain each endpoint, its parameters, and expected responses.
- Files: README — Include setup instructions and required dependencies.
- Files: README — Explain how to run the Express server and Java processor.
- Files: Project documentation — Document CSV output format and job status behavior.