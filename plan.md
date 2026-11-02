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
