# AI Improvements

## Refactoring Code

- Separate server routing, filesystem access, thumbnail generation, and job management into smaller modules to make the code easier to maintain and test.
- Review the Java video-processing pipeline and make sure frame extraction, binarization, grouping, centroid calculation, and CSV writing have clear responsibilities.
- Review duplicate Java source files and determine which source directory should be used to avoid maintaining outdated copies.
- Centralize configuration for video paths, JAR paths, result directories, and server ports.

## Adding Tests

- Add more tests for invalid input, missing parameters, boundary thresholds, and different hexadecimal color formats.
- Add tests for unknown jobs, failed jobs, completed jobs, and missing CSV output files.
- Add tests for invalid filenames, non-video files, and filenames containing special characters.
- Mock Java and FFmpeg process execution where appropriate to make unit tests more reliable.
- Ensure tests clean up temporary videos, thumbnails, and CSV files.

## Improving Error Handling

- Return clear error messages when FFmpeg or the Java processor fails.
- Add execution timeouts and clean up partially generated files when processing fails.
- Verify that generated thumbnails and CSV files exist before returning them.
- Improve validation error messages to distinguish missing parameters from invalid parameters.
- Review Java resource cleanup and error handling when video frames cannot be read.

## Writing Documentation

- Document the required Java and FFmpeg versions, environment variables, and accepted default values.
- Provide example API responses for running, completed, failed, and invalid processing requests.
- Explain the job lifecycle and where generated CSV and thumbnail files are stored.
- Improve the instructions for running tests, packaging the Maven project, and generating the JAR file.
- Document supported video formats and Java processor arguments.

## Improving Performance

- Review repeated color conversions and unnecessary object allocations during video processing.
- Consider streaming CSV output instead of storing all centroid rows in memory if the current implementation does not already do so.
- Limit simultaneous processing jobs to prevent excessive resource usage.

## Hardening Security

- Ensure generated file paths cannot escape the videos or results directories.
- Improve filename validation and reject invalid or unsupported file types.
- Apply appropriate request-size limits to JSON requests.
- Verify that requested CSV files belong to valid, completed processing jobs.

## Bug Fixes

- Review potential thumbnail filename collisions.
- Check whether successful Java process execution always produces the expected CSV file.
- Review whether hidden files, directories, and non-video files are incorrectly included in the video list.