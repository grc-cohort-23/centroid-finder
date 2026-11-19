# Video Plan

## videoprocessor.java
- Contains the main for video proceessing

### public static void main(String args[])
- Handel arguments
    -   ```
        java -jar videoprocessor.jar inputPath outputCsv targetColor threshold
        ```
    - make sure inputPath ends in .mp4
- try to create FFmpegFrameGrabber with inputPath 
- Make a new VideoProcessorHelper with a BinaryGroupFinder
- Call a processVideo in the VideoProcessorHelper with the video file as the parameter

- Make a new CSVCreator
- Call CSVCreator with result of processVideo


## VideoProcessorHelper.java
- Have a BinaryGroupFinder variable

### public List\<CentroidAtTime> processVideo( video_file )
- Create a list of CentroidAtTimes
- For each Frame 
- Convert Frame into a BufferedImage (if not already)
- Call the BinaryGroupFinder.findConnectedGroups() on the Frame
- Take the first index of result
    - If no result then new Centroid at -1,-1 of size 0 (nothing)
- Store Centroid & Time in list
- Return the list

## record CentroidAtTime
- Centroid
- Time (seconds)

## CSVCreator.java

### public void CreateCSV( List\<CentroidAtTime> )
- Creates CSV file
    - Error checks
- For each CentroidAtTime
- Add the CentroidAtTime to the CSV


> ## Possible Additions  
> * Create a binary video representing the tracked centroid(s) 
> * Varying frame intervals  
> * Tracking multiple centroids   
> * More video types
