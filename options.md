## Java Video Library Options

1. JavaCV
JavaCV is a library that allows Java programs to work with videos and images. It uses tools like FFmpeg and OpenCV.

# Pros:
- It supports MP4 and many other video formats.
- We can extract frames from videos.
- It provides useful video information, such as frame rate and video length.

# Cons:
- It requires additional libraries and native dependencies.
- It can be a little complicated for beginners.

2. JCodec
JCodec is a video-processing library written in Java. It can be used to read and process video files.

# Pros:
- It is written in Java, so we do not need native libraries.
- It can extract frames from supported MP4 videos.
- It is relatively simple for small projects.

# Cons:
- It does not support as many video formats as JavaCV.
- Some MP4 videos may use codecs that it cannot decode.

3. OpenCV
OpenCV is a popular library for working with images and videos. It also provides tools for image processing and object detection.

# Pros:
- It has many useful image-processing features.
- It can read videos and process frames.
- It could be useful for finding centroids in video frames.

# Cons:
- Setting it up in Java can be difficult.
- It requires native libraries.
- It has many features that we may not need for this project.

## My Choice

I would like to try JavaCV first because our project needs to read MP4 videos and extract frames. JavaCV supports many video formats and provides useful video information. The setup may be a little complicated, but it seems like a good option for our project.