## GOALS
(necessary)
- Process mp4 videos
- Use old centroid code
- Output a csv file
- first col: number of seconds
- second and third: x and y (-1, -1) if no centroid

## Focus on:
Object oriented, multiple classes
Take in arguments (that error when not correct)

**No reason to output a new video**

1. Deconstruct the video (VideoDeconstructor)
2. Frame by frame finding centroids (call GroupFinder)
3. Put them in a list
4. Build the csv looping through centroids

another class pull frames

biggestframe = null;
for (frame in frames) {
    if !biggestframe = frame;
    else if (biggestframe < frame) biggestframe = frame;
}

csv.output(biggestframe);

java -jar videoprocesser.jar inputPath outputCsv targetColor threshold

We had our own video in which we tested the program with. We validated the program in two ways,
first we checked to see if there was an actual input being read in, in which there was! Sometimes it would not detect anything if the threshold was too low, which is to be expected.
The numbers looked reasonable enough.
second, in a different branch (advanced-video), we wrote some code in combination with Claude to output the binarized frames into a new video.
Claude helped with codec setup and added a dot to where the centroid was in each BufferedImage frame.
Checking the outputs of each video, the sillouettes matched, and was tracking the centroids very well.
To find our color, we used color picker software and screenshots.
We also found that a threshold value from 40-70 is a pretty good sweet spot if you're dealing with shadows.