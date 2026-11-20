# Centroid Finder front end

React + Vite app for picking a target color, tuning the threshold with a live
preview, running the Java processor, and viewing the tracked path.

## Run it (development)

1. Start the API server: `cd server && npm install && npm start` (port 3000).
2. In another terminal: `cd client && npm install && npm run dev`.
3. Open http://localhost:5173. Vite forwards `/api`, `/thumbnail`, `/process`,
   `/videos` and `/results` to the server.

## Run it (one server)

`cd client && npm run build`, then `cd server && npm start` and open
http://localhost:3000. Express serves `client/dist`.

## Using it

1. Pick a video from the library (files come from the `videos/` folder).
2. **Eyedropper** (`E`): click the object in the video to sample its color.
   Hovering shows each pixel's color and its distance from the target.
3. **Threshold**: drag until only the object lights up. **Highlight** dims
   everything that doesn't match; **Mask** shows exactly what the processor
   sees. The yellow crosshair is the largest blob's centroid in the current
   frame, computed in the browser with the same math as the Java code.
4. Scrub or step through frames (`←`/`→`) to check that the settings hold up.
5. **Run detection** starts a job. When it finishes, the pink path shows where
   the processor tracked the object, the chart plots x/y over time (click to
   seek, red bands = not found), and you can download the CSV.

## Tests

`npm test` runs the Vitest unit tests for the detection math and CSV parsing.
