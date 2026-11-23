## Goal

Build a command line tool that reads an MP4, runs every frame through 
centroid-finder logic, and writes a CSV tracking the largest group's centroid over time.

## Architechture
![Architecture diagram](docs/diagram_centroid.JPG)

```
 java -jar videoprocessor.jar inputPath outputCsv targetColor threshold
 ```

Example output (`seconds,x,y`):

AI Example:
``` 
0.000,412,233
0.033,413,233
0.067,-1,-1
```

If no group matches in a frame, we write `-1,-1` for the coordinates.

---

## Library choice

We are going with **JavaCV** (`FFmpegFrameGrabber` + `Java2DFrameConverter`).
 It hands us frames we can convert straight into `BufferedImage`, which is the
exact input type our `ImageGroupFinder` already accepts. 
---

 AI used to make pretty
## What we RE-USE 
| Existing class | Role in the video tool |
|---|---|
| `ColorDistanceFinder` / `EuclideanColorDistance` | Color distance from each pixel to the target color |
| `ImageBinarizer` / `DistanceImageBinarizer` | Turns each frame into a 0/1 array |
| `BinaryGroupFinder` / `DfsBinaryGroupFinder` | Finds connected groups and their centroids |
| `ImageGroupFinder` / `BinarizingImageGroupFinder` | One call: frame in → sorted groups out |
| `Group`, `Coordinate` | Data passed between components |



The video tool never touches pixels directly. It just feeds `BufferedImage`s into an
`ImageGroupFinder`. That means all the hard image logic is already tested.

`ImageSummaryApp`  it is useful for checking a single frame during validation
`VideoExperiment` was our experiment and will be replaced by the classes below.

---

AI Used to help create a nice table, asking AI how to make a table and what are some things i need to include
## New components

| New class | Responsibility |
|---|---|
| `VideoProcessorApp` | `main`. Parses args, wires objects together, prints errors. No logic of its own. |
| `ArgumentParser` | Turns `String[] args` into a `VideoProcessorArgs`; throws `IllegalArgumentException` with a clear message on bad input. |
| `VideoProcessorArgs` (record) | `inputPath`, `outputCsv`, `targetColor` (int `0xRRGGBB`), `threshold` (int). |
| `FrameSource` (interface, `Closeable`) | `Optional<TimedFrame> nextFrame()` — hands out frames one at a time. |
| `JavaCvFrameSource` | The only class that knows about JavaCV. Wraps `FFmpegFrameGrabber` and `Java2DFrameConverter`. |
| `TimedFrame` (record) | `double seconds`, `BufferedImage image`. |
| `CentroidTracker` | Given a `TimedFrame`, asks the `ImageGroupFinder` for groups and returns a `TrackingResult` with the largest group's centroid, or `(-1,-1)`. |
| `TrackingResult` (record) | `double seconds`, `Coordinate centroid`; has `toCsvRow()`. |
| `ResultWriter` (interface, `Closeable`) | `void write(TrackingResult r)`. |
| `CsvResultWriter` | Writes `seconds,x,y` lines to a `Writer` (a file in real use, a `StringWriter` in tests). |
| `VideoProcessor` | The loop: pull frame → track → write, until the source is empty. Depends only on interfaces. |

- Changed FrameSource and ResultWriter to Closeable instead of AutoCloseable

## Validation plan 

**Choosing color and threshold**
1. Export one frame of the salamander video to PNG 
2. Use an eyedropper tool to sample the salamander's color in a few spots; pick a middle value.
3. Run `ImageSummaryApp` on that frame with a few thresholds and look at `binarized.png`.
   Good settings show the salamander as one solid white blob with little background noise.
4. Repeat on a frame from early, middle and late in the video, since lighting can change.

**Checking it actually tracks the salamander**
- Spot-check: pick 3 timestamps, pause the video there, and compare the salamander's position with
  the CSV's `x,y`.
- Plot `x` and `y` over time; the path should be smooth. Big sudden jumps likely mean the largest
  group switched to something else.
- Count the `-1,-1` rows; lots of them means the threshold is too tight.


## Results

note: We sample one frame per second (STEP_SECONDS = 1.0); it’s fast and still follows the salamander, which moves slowly

**Video:** ensantina.mp4, 816×720, ~24 fps, ~8 minutes
- Color chosen: 5C0005 (Digital Color Meter w sRGB) use on mander in start, middle and end then avg them out.

**Threshold chosen: 80**
we compared between 25, 35, 50 80 and 90 -> we picked 80 because less of the background turns white.
- 25 n 35 was bad, dark spot/container at the top was bigger than mander.
- 50 barely work

**Full run:** `java -jar target/videoprocessor.jar sampleInput/ensantina.mp4 output.csv 5C0005 80`
- 479 rows (one per second), no `-1,-1` rows.

**What went wrong:** When the salamander is in the bottom-left corner and partly
out of frame, the dark strip along the top of the container becomes a bigger
group than the salamander, so the tracker jumps to it. (1:03 and 1:16)

**Check**

- We compared the CSV to the video by hand. Picked three random times (0:30, 3:20, and 6:40), paused the video at each one, and looked at where the salamander was. In all 3, the position in the CSV matched where the salamander really was.
- 29 out of 479 rows that's wrong, it said the mander stuck at the very top of the picture or far right side.
- Conclusion why its wrong: the mander actually was at the bottom left but its body was out of frame mostly or enough that the dark strip/shadows? at the top actually bigger than mander in that moment -> the program following that instead of "half" of the mander.