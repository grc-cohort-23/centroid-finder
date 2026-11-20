## Brainstorm 
```
from my test video file after selecting the library for this i assignment , i  imagine i will try and re adapt its logic to work with the centroid finder assignment but instead of giving the centroid finder a single image and outputing it as a single csv, i will instead giv e the centroid finder a single frame of a video and save that data to the output csv in the document format the video assignemnt expects
```
## video decoding flow diagram
```
maven jar entry with video --> 
{

    retrieve video frame from video --> 
    run the centroid finder that frame --> 
    save the data to the video csv in a column for that frames timestamp --> 
    repeat until video completes-->
}
test for errors
```

# updated plans

- create a main class that will construct general necessities like a class declarations (imageBinarizer,Java2DFrameConverter, ColorDistanceFinder, ImageGroupFinder, Frame) to be used for within the video processing loop on each frame.
- create helper classes that append structured information (viudeo timestamp, x coordiniate of largest groups centroid, y coordiniate of largest groups centroid ) per frame to a list
- from that list construct the final csv file and save it