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

## Design decisions
- VideoSummaryApp is also the coordinator: lets stick with VideoSummaryApp its more clear... I just think it might go to far to make them separate because Coordinator would be likely such a small class.
- ImageCentroidTracker is its own smaller class: it's easier to test if separate instead of needing to test the whole program.. and FrameReader is only meant to take frames of the video
- ImageCentroidTracker returns a Coordinate instead of a Group: because if we use group it also returns a size and that would be more info than we need returned especially if there is no group found.
- FrameReader is an interface: like we did with the FakeDistanceFinder, we can give it a fake FrameReader that gives back images we made ourselves, to test that the code is working. If we for some reason decided that JavaCV needed to be switched to JCodec we would only need to write a new JCodecFrameReader, and the rest of our code wouldn't change.
- CSV seconds use 3 decimal places: that way its more precise without feeling messy.

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
   │       └── uses existing: BinarizingImageGroupFinder
   │                            ├── DistanceImageBinarizer → EuclideanColorDistance
   │                            └── DfsBinaryGroupFinder
   │
   └── CSVOutputWriter         writes "seconds,x,y" rows
```

## Testing plan
<!-- How will you test each class? Which ones can use fakes? -->

## Validation (Wave 4)
<!-- How you tested it on the salamander video, and how you chose the color and threshold -->
