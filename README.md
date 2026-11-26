# Centroid Finder

## Overview

Centroid Finder is a video processing application that tracks the position of the largest group of pixels matching a selected color. The project was originally developed to track the movement of a salamander throughout a video.

The application is split into two main parts:

- **Java Video Processor:** Reads a video, samples one frame per second, finds the largest matching color group, and writes its centroid coordinates to a CSV file.
- **Express Server:** Provides API endpoints for selecting videos, generating thumbnails, starting processing jobs, checking job status, and downloading CSV results.

The Java processor handles all image processing and graph-search algorithms. The Express server handles requests, input validation, file storage, and communication with the Java processor.


## Project Structure

```text
centroid-finder/
├── processor/
│   ├── src/
│   │   ├── main/java/
│   │   └── test/java/
│   └── pom.xml
├── server/
│   ├── src/
│   │   ├── app.js
│   │   ├── config.js
│   │   ├── routes.js
│   │   └── services.js
│   ├── index.js
│   └── package.json
├── videos/
├── results/
├── jobs/
├── docs/
├── .env
├── README.md
├── plan.md
└── serverPlan.md
```

- `processor/`: Contains the Java video processing application and its unit tests.
- `server/`: Contains the Express server and its tests.
- `videos/`: Stores videos available for processing.
- `results/`: Stores generated CSV files.
- `jobs/`: Stores job status information and error logs.
- `docs/`: Contains project diagrams and images.
- `plan.md`: Contains the Java processor design and validation results.
- `serverPlan.md`: Contains the server design and implementation plan.

## Installation and Build

### 1. Build the Java Processor

From the root of the repository, run:

```bash
cd processor
mvn package
```

Maven downloads the required dependencies, compiles the Java code, runs the tests, and packages the application into a JAR file.

The generated JAR is located at:

```text
processor/target/videoprocessor.jar
```

### 2. Install Server Dependencies

From the root of the repository, run:

```bash
cd server
npm install
```

This installs Express, dotenv, Jest, Supertest, and the other required dependencies.

### 3. Add Videos
The server supports the following video file extensions:

- `.mp4`
- `.mov`
- `.avi`
- `.mkv`

File extensions are case-insensitive.

Video uploads are not currently supported through the API. Videos must be added to the folder manually.

### Arguments

| Argument | Description |
|---|---|
| `inputPath` | Path to the input video file |
| `outputCsv` | Path where the generated CSV will be saved |
| `targetColor` | Six-digit hexadecimal RGB color, with an optional leading `#` |
| `threshold` | Whole number between 0 and 442 representing the allowed color distance |

### Example

```bash
java -jar target/videoprocessor.jar ../videos/ensantina.mp4 output.csv 5C0005 80
```

This command processes `ensantina.mp4`, searches for pixels matching the color `5C0005` using a threshold of `80`, and writes the results to `output.csv`.

### Color Threshold

The threshold determines how close a pixel's color must be to the selected target color.

A smaller threshold matches fewer colors, while a larger threshold allows more colors to match.

The maximum possible Euclidean distance between two RGB colors is approximately 441.67. Therefore, the maximum accepted threshold is 442.

A pixel matches when its color distance is strictly less than the threshold.

## CSV Output Format

The video processor samples one frame per second and writes one row for each sampled frame.

The CSV contains three columns:

```text
seconds,x,y
```

The actual CSV file does not contain a header row.

| Column | Description |
|---|---|
| `seconds` | Time in seconds from the beginning of the video |
| `x` | Horizontal coordinate of the largest matching group's centroid |
| `y` | Vertical coordinate of the largest matching group's centroid |

If no matching group is found, both coordinates are recorded as `-1`.

### Example Output

```csv
0.000,412,233
1.000,413,233
2.000,-1,-1
3.000,415,235
4.000,417,236
```

In this example, no matching group was found at the two-second mark.

Timestamps are written with three decimal places.

## Running the Server

Make sure the Java JAR has been built and the server dependencies have been installed.

From the root of the repository, run:

```bash
cd server
npm start
npm run dev
```
## API Endpoints

The server provides four main API endpoints.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/videos` | Returns available video filenames |
| GET | `/thumbnail/:filename` | Returns a video thumbnail |
| POST | `/process/:filename` | Starts a video processing job |
| GET | `/process/:jobId/status` | Returns the current status of a job |

### GET /thumbnail/:filename

Returns the first frame of the selected video as a JPEG image.

The server uses FFmpeg to generate the thumbnail.

**Example request:**

```bash
curl http://localhost:3000/thumbnail/ensantina.mp4 -o thumbnail.jpg
```

**Response:**

```text
Content-Type: image/jpeg
```

**Possible status codes:**

- `200`: Thumbnail generated successfully.
- `400`: Invalid filename, unsupported extension, or the requested path is not a regular file.
- `500`: The video could not be accessed or the thumbnail could not be generated.

### POST /process/:filename

Starts a video processing job using the selected video, target color, and threshold.

Processing runs in the background, allowing the server to respond without waiting for the Java processor to finish.

**Query parameters:**

| Parameter | Required | Description |
|---|---|---|
| `targetColor` | Yes | Six-digit hexadecimal RGB color |
| `threshold` | Yes | Whole number between 0 and 442 |

**Example request:**

```bash
curl -X POST "http://localhost:3000/process/ensantina.mp4?targetColor=5C0005&threshold=80"
```

**Example response (202 Accepted):**

```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000"
}
```

Each processing job receives a unique ID. The ID is used to check the job's status and identify its result file.

Before starting a job, the server validates that:

- The filename is valid and has a supported video extension.
- The requested video exists and is a regular file.
- The target color contains six hexadecimal digits, with an optional leading `#`.
- The threshold is a whole number between 0 and 442.
- The required query parameters are provided and are not repeated.

**Example error response (400 Bad Request):**

```json
{
  "error": "threshold must be a whole number between 0 and 442."
}
```

**Possible status codes:**

- `202`: Processing job accepted.
- `400`: Invalid input or missing video.
- `500`: The server could not start the job.

### GET /process/:jobId/status

Returns the current status of a video processing job.

**Example request:**

```bash
curl http://localhost:3000/process/123e4567-e89b-12d3-a456-426614174000/status
```

A job can have one of three statuses: `processing`, `done`, or `error`.

**Processing:**

```json
{
  "status": "processing"
}
```

The Java processor is still running.

**Done:**

```json
{
  "status": "done",
  "result": "/results/123e4567-e89b-12d3-a456-426614174000.csv"
}
```

The processor completed successfully, and the CSV file is available for download.

**Error:**

```json
{
  "status": "error",
  "error": "Error processing video: processor exited with code 1"
}
```

The processing job failed.

**Possible status codes:**

- `200`: Job status returned successfully, including failed processing jobs.
- `404`: The job ID is invalid or does not exist.
- `500`: The server could not retrieve the job status.

### Downloading Results

After a job finishes successfully, its status response contains a path to the generated CSV file.

The results directory is served using `express.static`, so there is no separate download endpoint.

**Example:**

```bash
curl http://localhost:3000/results/123e4567-e89b-12d3-a456-426614174000.csv -o output.csv
```

Replace the example job ID with the ID returned by your processing request.

## Job Storage and Error Handling

Each processing request creates a JSON file in the `jobs/` directory.

The job file stores information such as:

- Job status
- Selected video
- Target color
- Threshold
- Result file location
- Error message, if processing failed

Each job also has its own CSV output file:

```text
results/<jobId>.csv
```

Using unique filenames allows the same video to be processed multiple times without overwriting previous results.

The Java processor's standard error output is saved to:

```text
jobs/<jobId>.log
```

If processing fails, the server uses the processor's exit code and the last available error message to explain the failure.

When the server restarts, unfinished jobs are marked as errors so clients do not wait indefinitely for results.

### Java Tests

The Java processor uses JUnit to test image processing, argument validation, color distance calculations, centroid tracking, CSV generation, and video processing.

From the root of the repository, run:

```bash
cd processor
mvn test
```

To clean previously compiled files before running tests:

```bash
mvn clean test
```

Maven also runs the tests when building the JAR using `mvn package`.

### JavaScript Tests

The Express server uses Jest and Supertest.

The tests cover:

- API endpoints
- Video filename validation
- Color and threshold validation
- Job creation and status updates
- Job file storage
- Processor execution and error handling
- Thumbnail responses

From the root of the repository, run:

```bash
cd server
npm test
```

The server tests use fake processor and thumbnail functions, temporary directories, and test files. This allows the API behavior to be tested without processing real videos.

### Manual API Testing

The API can also be tested manually using Postman.

A Postman collection is available at:

```text
server/postman_collection.json
```
A complete manual test should include listing videos, retrieving a thumbnail, starting a processing job, checking its status, and downloading the generated CSV.
