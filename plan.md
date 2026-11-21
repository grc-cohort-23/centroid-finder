# Video Processor Plan

## Program flow

```
1. Read the 4 arguments: inputPath, outputCsv, targetColor, threshold
2. Check them / convert them:  targetColor → int   threshold → int
3. Build the centroid-finding pieces once, before the loop
   (reuse the same classes ImageSummaryApp uses: EuclideanColorDistance,
   DistanceImageBinarizer, DfsBinaryGroupFinder, BinarizingImageGroupFinder)
4. Open the video file at inputPath
5. Open the output CSV file for writing
6. For each frame in the video:
     a. Turn it into a BufferedImage
     b. Get its timestamp and convert microseconds -> seconds (/ 1,000,000.0)
     c. Find the groups in it (reuse BinarizingImageGroupFinder)
     d. If there is at least one group → write  seconds, groups.get(0).centroid().x(), .y()
        If the list is empty → write  seconds, -1, -1
7. Close the video and the CSV file
```

## Reused classes
We will reuse the same classes ImageSummaryApp uses:
- EuclideanColorDistance
- DistanceImageBinarizer
- DfsBinaryGroupFinder
- BinarizingImageGroupFinder
- Group, Coordinate

## New classes
<!-- For each class: what is its job? what does it take in and give back? -->

### VideoSummaryApp
- Is like ImageSummaryApp but it's the video version.. So it's our main and runs the loop
- It creates the ArgumentParser, FrameReader, ImageCentroidTracker, CSVOutputWriter and passes them what they need... AKA a coordinator...

### ArgumentParser
- Reads the 4 arguments: inputPath, outputCsv, targetColor, threshold
- it then checks them / converts them: targetColor:int threshold:int
- it wraps conversions in try/catch to check for the wrong number of arguments, or if strings get put in as the threshold, and that the color is a real hex code and if it's not valid it throws an exception
- added a check for the color being exactly 6 hex digits and that the threshold can't be negative
### FrameReader (interface)
- next frame → BufferedImage (or null when the video is over)
- timestamp → double (seconds)
- close → void

### JavaCVFrameReader (implements FrameReader)
- takes in the inputPath ... the video file
- it uses grabber.grabImage() -> gets the next frame
- converter.convert(frame) -> turns it into a BufferedImage
- grabber.getTimestamp() -> the time, in microseconds / 1,000,000.0
- grabber.close() -> shuts the video

### ImageCentroidTracker
- it takes one BufferedImage, uses BinarizingImageGroupFinder, returns the Coordinate of the biggest group, or -1,-1 if no groups are found...

### CSVOutputWriter
- opens the output CSV, writes one seconds,x,y row per frame, and closes the file
- seconds always have 3 decimal places (example: 1.500,320,180)
- no header row, to match the format of sampleOutput/groups.csv

### TopEdgeFilteringGroupFinder (added during Wave 4)
- wraps another ImageGroupFinder (BinarizingImageGroupFinder) and removes any group whose centroid is in the top 30 rows of the frame
- added because the salamander video has a dark strip along the top edge that is almost the same color as the salamander (see Validation)

## Design decisions
- VideoSummaryApp is also the coordinator: lets stick with VideoSummaryApp its more clear... I just think it might go to far to make them separate because Coordinator would be likely such a small class.
- ImageCentroidTracker is its own smaller class: it's easier to test if separate instead of needing to test the whole program.. and FrameReader is only meant to take frames of the video
- ImageCentroidTracker returns a Coordinate instead of a Group: because if we use group it also returns a size and that would be more info than we need returned especially if there is no group found.
- FrameReader is an interface: like we did with the FakeDistanceFinder, we can give it a fake FrameReader that gives back images we made ourselves, to test that the code is working. If we for some reason decided that JavaCV needed to be switched to JCodec we would only need to write a new JCodecFrameReader, and the rest of our code wouldn't change.
- CSV seconds use 3 decimal places: that way its more precise without feeling messy.
- TopEdgeFilteringGroupFinder wraps the existing group finder instead of changing it: ImageCentroidTracker only knows about the ImageGroupFinder interface, so we could add the filter without changing ImageCentroidTracker, DfsBinaryGroupFinder, or Group. The margin (30) is a constant in VideoSummaryApp so the 4-argument command stays the same.

## Architecture diagram
```
VideoSummaryApp (main + runs the loop)
   │
   ├── ArgumentParser          reads & checks the 4 arguments
   │
   ├── FrameReader (interface) next frame → BufferedImage, timestamp → double, close → void
   │       ├── JavaCVFrameReader   the real one, uses JavaCV
   │       └── (fake one in tests)
   │
   ├── ImageCentroidTracker    BufferedImage → Coordinate (biggest group, or -1,-1)
   │       └── TopEdgeFilteringGroupFinder   removes groups in the top 30 rows
   │               └── wraps existing: BinarizingImageGroupFinder
   │                            ├── DistanceImageBinarizer → EuclideanColorDistance
   │                            └── DfsBinaryGroupFinder
   │
   └── CSVOutputWriter         writes "seconds,x,y" rows
```

## Testing plan
<!-- How will you test each class? Which ones can use fakes? -->

## Validation (Wave 4)
Video: ensantina.mp4 (816x720, about 8 minutes, 11,477 frames). Not committed (*.mp4 is in .gitignore).

**What the video looks like:** a dark salamander walking on red/pink paper under red light. Under the red light the salamander is not black, it is a very dark red.

**Choosing the color and threshold**
- Black (000000) with threshold 50: the salamander never matched. The only match was a tiny dark speck in the top-right corner, so every row tracked that corner.
- Dark red 4E0A0E with threshold 50: the salamander matched, but the centroid was on the top edge in 16% of frames and jumped 146 times.
- 650010 with threshold 40 (Nadia): the best result, 0 missing frames, but still 1,229 frames (10.7%) on the top edge.

**How we checked it was really tracking the salamander**
- "0 missing" was not enough proof: the dark strip at the top always counted as a detection, so the CSV had numbers in every row even when it wasn't following the salamander.
- We counted rows where the centroid was near the top edge (y < 25), and rows where the centroid jumped more than 100 pixels between two frames 0.04 seconds apart (a salamander can't move that fast).
- We binarized single frames and circled the biggest groups to see what was matching. At 83 seconds the top strip (6,765 pixels) was bigger than the salamander (6,305 pixels), so the tracker followed the strip.
- Changing the threshold didn't help, because the strip and the salamander are the same color and grow/shrink together.

**The fix:** TopEdgeFilteringGroupFinder ignores groups with a centroid in the top 30 rows. The strip's centroid was always above y = 20 and the salamander's was always below y = 100, so 30 is safely in between.

| 650010 / 40 | Missing | On the top edge | Big jumps |
|---|---|---|---|
| Before the fix | 0 | 1,229 | 16 |
| After the fix | 0 | 0 | 0 |

At 83 seconds the CSV now says (39, 581), which is where the salamander is, instead of (307, 7) on the strip. The balloon video output did not change.

**Limitation:** the margin of 30 is tuned to this video's setup. If the salamander walks along the very top edge, those frames could be ignored.
