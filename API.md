## Server API

The server runs at `http://localhost:3000` by default.
No request body is needed. Encode filenames when using them in URLs.

### List Videos

`GET /api/videos`

Returns the available video filenames in alphabetical order.

```json
["sample.mp4"]
```

Returns `200` on success or `500` if the video directory cannot be read.

### Get a Thumbnail

`GET /thumbnail/sample.mp4`

Returns the first video frame as a JPEG.

Returns `200` on success, `400` for an invalid filename,
`404` for a missing video, or `500` if thumbnail generation fails.

### Start Processing

```bash
curl -X POST "http://localhost:3000/process/sample.mp4?targetColor=ffa200&threshold=164"
```

Use a six-digit hex color without `#` and a non-negative integer
threshold that fits in a Java integer.

Returns `202` with a job ID:

```json
{ "jobId": "550e8400-e29b-41d4-a716-446655440000" }
```

Returns `400` for invalid settings or filenames, `404` for a missing
video, or `500` if the job cannot be started.

### Check Job Status

`GET /process/{jobId}/status`

Replace `{jobId}` with the ID returned when processing started.

Returns `200` with one of these responses:

```json
{ "status": "processing" }
```

```json
{ "status": "done", "result": "/results/{jobId}.csv" }
```

```json
{ "status": "error", "error": "Description of the failure" }
```

Returns `404` for an invalid or unknown job ID, or `500` if the status
cannot be read.

### Download CSV

`GET /results/{jobId}.csv`

Use the result URL returned by the completed job.

Each row contains `seconds,x,y`, without a header.
Coordinates of `-1,-1` mean no matching group was found.

Returns `200` for a successful download, `404` for an invalid filename,
missing job, or unfinished job, or `500` if the download fails.

### Errors

Route errors use this format:

```json
{ "error": "Video not found" }
```
