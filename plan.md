# Centroid Finder Video Part II Plan

## Goal

The goal is to find out the location of the largest group of pixels matching a designated color in an MP4 video. A path to the video file, destination location of the CSV file, target color, and tolerance in pixel colors have to be provided by the user. In a CSV file, timestamps and center coordinates of the matching group will be logged, in case there are no matches “-1,-1” will be recorded in the files.

## Components

- Video reader
- Argument parser
- Centroid processor
- CSV writer
- Main application

## Architecture

```mermaid
flowchart TD
    A["Open the video"] --> B["Read a frame"]
    B --> C["Find the center of the biggest color match"]
    C --> D["Save the time and coordinates to the CSV"]
    D -->|More frames| B
```

## Division of Labor

We have a lot of this working already.

I can handle the command line and CSV writing. If you want to handle the frames and integrating them into our existing centroid finder, I think we will be good.

## Testing

I first ran the JAR on the salamander video with FFA200 and a threshold of 164. When I checked the frame at about 13 seconds, the marker landed on the background. I tried a darker red (650000) with a threshold of 40. That run produced 11,477 CSV rows, which matched the video’s frame count. In one frame I checked with the new settings, the marker landed just below the curve of the salamander’s body.