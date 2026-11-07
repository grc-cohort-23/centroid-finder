## javaCV
- t is fast and popular and get maintained often. 
- it turn frames into exactly what my code needed
- NOTE: it is very big in size and harder to fix if it crash. Can leak memory if you dont close with "grabber"

##JCodec
- very small and simple to install and work. Error are very easy to understand 
- Note: Will pick this consider the fact that it mainly support H.264 video in MP4 (which we will use MP4)
- NOTE: it is a bit slower than javaCV and metadata take more work, so you do the math yourself.
- Could have bug because it is not very active anymore so no maintained.

## vicj
- actively maintain and can open any videos, fast
- NOTE: VLC have to i on every computer that run the programs, converting frames to bufferedImage take xtra work.

--

- I think we should use javaCV and depend on what video we look at we could use JCodec

- (KC) decided to go with JavaCV, and implemented a sample.mp4 of a yellow snail and a white background. The snail is going very slow. Also implemented VideoExperiment.java, where the code takes a video, looks at one frame per second, and prints where the biggest bob of a targeted color is in each frame (looking for red, which there isn't any). 
 ----------
int target = Integer.parseInt(args[1], 16);
int threshold = Integer.parseInt(args[2]);
this is the Maven for the javaCV: 
------------
* runs VideoExperiment snail.mp4 FFA200 60.args[0], which is the video path. args[1] is the target color, and args[2] is the threshold. 

---------
try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(args[0]);
     Java2DFrameConverter converter = new Java2DFrameConverter()) {
------------
* opens the video, where FFmpegFrameGrabber is JavaCV's reader for the video files. Java2DFrameConverter converts JavaCV's own Frame type into a Java BufferedImage.
------------

grabber.start();
System.out.println("FPS: " + grabber.getFrameRate());
System.out.println("Duration (s): " + grabber.getLengthInTime() / 1_000_000.0);
--------
* start() opens the file and read its header, which duration is divided by microsends (1,000,000)
------

long durationUs = grabber.getLengthInTime();
for (long t = 0; t < durationUs; t += 1_000_000) {
----------
* finds the duration in microseconds of the video 
------------

grabber.setTimestamp(t);              // seek
Frame frame = grabber.grabImage();
if (frame == null) break;
-----------
* setTimestamp jumps to the moment in video, and grabImage() reads the frame. Null means nothing left to read so the loop stops
----------

BufferedImage img = converter.convert(frame);
List<Group> groups = finder.findConnectedGroups(img);
---------
* Converts frame to a BufferedImage, and runs in through the finder. 
----------
String out = groups.isEmpty()
    ? "-1,-1"
    : groups.get(0).centroid().x() + "," + groups.get(0).centroid().y();
System.out.println((t / 1_000_000) + "," + out);
---------
* If no pixels match, it prints out -1, -1, otherwise, group.get(0) is the largest group, and prints the centroid. 
------

## test to find color of snail
mvn compile exec:java -Dexec.mainClass=VideoExperiment -Dexec.args="sampleInput/sample.mp4 FFA200 60"

  <dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.14</version>
  </dependency>
^^ went with JAvaCV

this is the Maven for JCodec:
<dependency>
    <groupId>org.jcodec</groupId>
    <artifactId>jcodec</artifactId>
    <version>0.2.5</version>
</dependency>
