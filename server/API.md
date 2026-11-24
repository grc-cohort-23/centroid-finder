# Centroid Finder REST API

The Express server listens on port `3000` by default. The React dev server proxies the API and the returned asset paths to Express. Resolve response paths against the same origin used for the API request.

## Processing settings

Send `Content-Type: application/json` with `POST /api/preview` and `POST /jobs`.

```json
{
  "videoFilename": "Field clip.mp4",
  "hexColor": "#FFA200",
  "tolerance": 30
}
```

| Field | Accepted value |
| --- | --- |
| `videoFilename` | An `.mp4` or `.mov` filename from `GET /api/videos`. Use the filename as returned in JSON. |
| `hexColor` | Six hexadecimal digits, with an optional `#` prefix. |
| `tolerance` | An integer from `0` to `442` inclusive, or a string of decimal digits representing that integer. Pixels with a color distance strictly below this value are selected. |

Both POST routes return `400` for malformed JSON or invalid settings and `404` when the selected video is unavailable. Error responses from the API use a JSON object with an `error` string.

## `GET /api/videos`

Returns `200` with a sorted array of video filenames, for example `["Field clip.mp4", "Next clip.mov"]`. An empty or missing videos directory yields `[]`. Other directory read failures return `500`.

To fetch a listed video directly, use `GET /videos/{filename}` with the filename URL-encoded as one path segment. The route returns `404` when the video is unavailable.

## `POST /api/preview`

Returns `200` after generating the original and binarized PNGs for the first video frame.

```json
{
  "originalUrl": "/previews/21ad8760-4512-45a0-82dc-3178fee10b0e/original.png",
  "previewUrl": "/previews/21ad8760-4512-45a0-82dc-3178fee10b0e/binarized.png",
  "width": 320,
  "height": 240,
  "centroid": { "x": 79, "y": 59 }
}
```

`width` and `height` are in pixels. Centroid coordinates are zero-based from the top-left corner. A centroid of `{ "x": -1, "y": -1 }` means the frame has no matching pixel group. Fetch either PNG with `GET` on its returned URL. Preview generation failures return `500`.

## `POST /jobs`

Starts full-video processing in the background. Returns `202` with a job ID and an initial status of `processing`.

```json
{
  "jobId": "11111111-1111-4111-8111-111111111111",
  "status": "processing",
  "message": "Video processing job started in the background."
}
```

An error preparing the job, such as failure to create the output directory, returns `500`. Failures after acceptance appear in the job status.

## `GET /jobs/{jobId}`

Pass the UUID returned by `POST /jobs`. Poll while the status is `processing`. Each `200` response includes `jobId` and one of these status values.

| `status` | Additional fields |
| --- | --- |
| `processing` | None. |
| `completed` | `downloadUrl`, a relative path under `/outputs/`. Fetch it with `GET` to download the CSV. |
| `failed` | `error`, a description of the failure. |

The CSV has no header. Each row contains `timestamp,x,y`. Coordinates of `-1,-1` indicate no match for that frame.

The status route returns `400` for a malformed job ID, `404` for an unknown job, and `500` if the server cannot check its output. In-progress and failed job statuses are kept in memory. After a server restart, a previously completed job can still be retrieved if its CSV is available on disk.
