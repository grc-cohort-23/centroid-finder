# JAVACV 
- Pros
- Very compatible with our code, almost any real world MP4 will open
- Nothing too install, setup is just MAVEN
- Highly maintained and errors with it are usually answered by someone already

- Cons
- It takes up alot of space and a very large download.
- Crashes are harder to debug and also hard to trace because it does not throw readable stack trace.
- Documentation is small and not very beginner friendly.

# Jaffree
- Pros
- Very compatible with our code just like JAVACV.
- Maven dependency is tiny and no other libararies to install.
- Very testable and can process data quick

- Cons
- Everyone has to install FFmpeg and set up PATH
- Less maintained, last release was August 2024
- if frame processing is slow, it has a known timeout quirk.

# JCODEC
- Pros 
- No native code at all and download is tiny
- Readable errors and its in plain Java
- Simple setup

- Cons
- Much slower than FFmpeg-based options
- supports only MP4 and may fail to decode other options 
- Very less maintained, last release was in June 2019

# JavaCPP presets for FFmpeg
- Pros
- On Maven build so there is nothing else to install
- Releases are very frequent just like JavaCV

- Cons
- Alot more code for the same result than JavaCV for the same result.
- It’s very low-level: open the file, find the video stream, read packets, send them to the decoder, receive frames.
- Converting to BufferedImage is manual, so some has to be converted because other variables are stored differently. The decoded frame is in YUV format rather than BGR, so it needs converting(swscale) and rows can have padding(linesze).

# OpenCV Java bindings
- Pros
- Its on Maven with native builds already
- Reading frames is simple and use basic loops
- You get acessed to opencv's processing tools

- Cons
- MP4 suppport is uncertain and user's state it fails to open videos and FFmpeg is not included.
- Less actively maintained. Latest release was on decemebr 2023.
- Many of OpenCV strengths are unused. Many of our own code already does the grouping and centroids.