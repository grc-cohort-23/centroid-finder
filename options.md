      <Matthew's Finds>
-------     -------    -------
# Java Video Processing Library Options

## 1. JCodec
**Description:** A pure Java implementation for decoding and encoding MP4 and MOV video formats without requiring external native binaries.
* Pros:
  * No Native Dependencies: 100% pure Java, meaning it runs smoothly anywhere without installing FFmpeg or C++ binaries on the host system.
  * Frame-by-Frame Control: Allows easy extraction of frames at specific numbers or timestamps (`FrameGrab.getFrameFromFile`).
  * Easy Conversion: Provides utility methods (`AWTUtil.toBufferedImage`) to turn decoded video frames directly into standard Java `BufferedImage` objects for image processing.
* Cons:
  * Limited Format Support: Focused heavily on MP4/MOV; less versatile for obscure video codecs.
  * Performance: Can be slower than native wrapper libraries when dealing with high-resolution or very long videos.


  ## 2. JavaCV
  **Description** JavaCV provides Java interfaces to powerful native libraries, thosei nclude FFmpeg and OpenCV. it allows java applications to read videos, extrat frames, and perform image processing without having to write C++ code.

  * Pros:
  * Extensive video format support: through FFmpeg, JavaCV can handle MP4, MOV, AVI, MKV and many other formats, depending on availale codecs.
  * Easy frame extration: FFmpegFrameGrabber allows us to read video frames individually. 
  * BufferedImage compatibility: Java2DFrameConverter converts decoded frames into BufferedIMage objects, making integration with our existing Centroid Finder straightforward.
  * Video metadata: Can retrieve information such as frame rate, frame count, video dimensions and timestamps.

  * Cons:
  Large dependencies: Building FFmpeg and OpenCV binaries increases the size of the project dependencies.
  *Native libraries: Relies on platform-specific native components, which can introduce compatibility issues.
  * More complex: requires understanding frame grabbers, converters, and native resource management.

  ## 3. OpenCV
  **Description**
  OpenCV is an open source computer vision library that provides tools for image analysis, object detection, tracking, and video processing. it supports Java through official bindings.

  * Pros:
  * Powerful computer vision tools: Provides algorithms for imaging filtering, object detection, image segmentation, and tracking.
  * Video processing: The VideoCapture class can open videos and read individual frames.
  * Useful for future development: Could help improve our Centroid Finder with more advanced detection and tracking techniques.

  * Cons:
  * Native dependencies: Requires native OpenCV libraries. Packages such as OpenPnP simplify installation through Maven.
  * More complicated image representation: OpenCV uses Mat objects rather than Java'sstandard BufferedImage , requiring conversion for our existing classes. 
  * Learning curve: OpenCV offers many features beyond what our current assignment requires, which makes it more complex to learn.



*Example Code Snippet for JCodec:*
```java
int frameNumber = 42;
Picture picture = FrameGrab.getFrameFromFile(new File("video.mp4"), frameNumber);
BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
ImageIO.write(bufferedImage, "png", new File("frame42.png"));
```

*Example Code Snippet for JavaCV:*
```java
import org.bytedeco.javacv.*;
import java.awt.image.BufferedImage;

try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("video.mp4")) {
    grabber.start();

    Java2DFrameConverter converter = new Java2DFrameConverter();

    Frame frame = grabber.grabImage();

    BufferedImage image = converter.convert(frame);
}
```
*Example Code Snippet for OpenCV:*
```java
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

VideoCapture capture = new VideoCapture("video.mp4");

Mat frame = new Mat();

while (capture.read(frame)) {
    // Process each frame here
}

capture.release();
```