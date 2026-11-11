# Video Library Options

## JCodec
- Pro: Easy to add to our Java project.
- Pro: Doesn't require installing additional libraries.
- Con: It works with fewer types of video.

## JavaCV
- Pro: It can read more types of video.
- Con: It needs a bigger download.
- Con: More video types are good, but it isn't hard to convert.

## GStreamer Java
- Pro: It gives us more control over how we read the video.
- Con: We have to install GStreamer separately.
- Con: Do we even need the extra control?

## Our Choice
We want to try JCodec because it's easy to set up. We'll use a short H.264 MP4 and try to save one frame every second.

Another reason is because we don't *need* the extra functionality the other two give.