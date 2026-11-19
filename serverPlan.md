# Server Planning

## What We Understand

We looked at the Salamander API. We need to build an Express server that works with our Java JAR to process videos.
The API has four main endpoints:
- `GET /api/videos` - Shows all available videos.
- `GET /thumbnail/:filename` - Gets the first frame of a video.
- `POST /process/:filename` - Starts processing a video.
- `GET /process/:jobId/status` - Checks the processing status.

## Our Plan
We plan to use Node.js and Express to build our server.
We will use a `.env` file to store the paths to our videos folder and Java JAR.
When someone wants to process a video, Express will create a job ID and call our Java JAR.
The Java JAR will process the video and save the results in a CSV file.
We want Express to return the job ID right away instead of waiting for the video to finish processing.
We will use `child_process` to run the JAR in the background.

## Architecture

```text
       User / Client
             v
       Express Server
             v
          Java JAR
             v
       Process Video
             v
         CSV File
             v
       Results Folder
```

Our videos will be stored in a separate folder, and Express will also keep track of each job's status.

## API Endpoints

**GET /api/videos**
We will read the videos folder and return a list of videos.

**GET /thumbnail/:filename**
We will use FFmpeg to get the first frame of a video.

**POST /process/:filename**
When someone requests video processing:
1. Check if the video exists.
2. Create a job ID.
3. Call our Java JAR to process the video.
4. Return the job ID right away.

**GET /process/:jobId/status**
We will check if the video is still processing, finished, or failed.
If it is finished, we will return the CSV result.

## File Storage
We plan to use folders instead of a database because it seems easier for this project.
```text
salamander-server/
├── server.js
├── .env
├── serverPlan.md
├── videos/
├── results/
└── jobs/
```

- `videos/` - Stores videos.
- `results/` - Stores CSV files.
- `jobs/` - Stores job status.

We can use `express.static()` to make files available through the server.

## Questions We Have
1. How will Express know when the Java JAR finishes?
2. What should happen if the Java JAR fails?
3. Is using folders better than a database for this project?
4. Do we need to change anything in our Java JAR?

