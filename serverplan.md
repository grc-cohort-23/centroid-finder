## Restructure files:
```
centroid-finder/
├── .github
├── .gitignore
├── LICENSE
├── README.md
├── plan.md
├── serverPlan.md
├── .env                  # Environment variable file (e.g., for JAR path)
├── server/               # Express server
│   ├── package.json
│   ├── index.js
│   └── ...other files
├── processor/            # Maven Java project
    ├── pom.xml
    ├── sampleInput
    ├── sampleOutput
    └── src/
        └── main/
            └── java/
                └── ...
```
--- 
# API
## List Available Videos

**GET** `/api/videos`

**Description:**  
Return a list of all video files in the mounted directory, available publicly at /videos/VIDEO_NAME.

**Responses:**

- **200 OK**

  ```json
  ["intro.mp4", "demo.mov"]
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error reading video directory"
  }
  ```

---

## Generate Thumbnail

**GET** `/thumbnail/{filename}`

**Path Parameters:**

- `filename` (string, required) — Name of the video file (e.g. `demo.mov`)

**Description:**  
Extract and return the first frame from the video as a JPEG.

**Responses:**

- **200 OK**  
  JPEG binary data  
  _Content-Type: image/jpeg_


- **500 Internal Server Error**

  ```json
  {
    "error": "Error generating thumbnail"
  }
  ```

---

## Start Video Processing Job

**POST** `/process/{filename}`  
_Query parameters:_ `?targetColor=<hex>&threshold=<int>`

**Path Parameters:**

- `filename` (string, required) — Name of the video file to process (e.g. `intro.mp4`)

**Query Parameters:**

- `targetColor` (string, required) — Hex color code to match (e.g. `ff0000`)
- `threshold`   (number, required) — Match threshold (e.g. `120`)

**Description:**  
Kick off an asynchronous job to analyze the video. Returns a `jobId` you can poll.

**Responses:**

- **202 Accepted**

  ```json
  {
    "jobId": "123e4567-e89b-12d3-a456-426614174000"
  }
  ```

- **400 Bad Request**

  ```json
  {
    "error": "Missing targetColor or threshold query parameter."
  }
  ```


- **500 Internal Server Error**

  ```json
  {
    "error": "Error starting job"
  }
  ```

---

## Get Processing Job Status

**GET** `/process/{jobId}/status`

**Path Parameters:**

- `jobId` (string, required) — ID returned by the **POST** `/process` call

**Description:**  
Check whether the job is still running, has completed, or failed.

**Responses:**

- **200 OK** (processing)

  ```json
  {
    "status": "processing"
  }
  ```

- **200 OK** (done)

  ```json
  {
    "status": "done",
    "result": "/results/intro.mp4.csv"
  }
  ```

- **200 OK** (error)

  ```json
  {
    "status": "error",
    "error": "Error processing video: Unexpected ffmpeg error"
  }
  ```

- **404 Not Found**

  ```json
  {
    "error": "Job ID not found"
  }
  ```

- **500 Internal Server Error**

  ```json
  {
    "error": "Error fetching job status"
  }
  ```

  ---
  ---
# Server

## Database
The database will be handled using better-sqlite3.

### Schema
jobs
- id TEXT PRIMARY KEY
- status TEXT
- threshold INT
- target_color TEXT
- input_path TEXT
- output_path TEXT

##  Routes
 - router.get("api/videos", getVideos);
 - router.post("process/{filename}", processVideo);
 - router.get("process/{jobid}/status", getJobStatus);
 - router.get("thumbnail/{filename}", getThumbnail)
## Controllers
Each controller will be separated into its own file.

- getVideos: 
    - Loop through videos file in sever/videos and return JSON list of video names
- processVideo:
    - Create jobId
    - Insert jobId and status into database
    - Call videoprocessor.jar with params
      - Once done insert into metadata table for the jobId with status "done" or "error"
    - Keep querying database until status is "done"
    - Return jobId
- getJob:
    - Query database
      - "SELECT status FROM jobs WHERE id = ?", (jobId)
- getThumbnail:
    - ???