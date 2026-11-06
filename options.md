**1. Javacv**
1. Chatgpt recpmended this one and i will likely use it because i was able to get it to work and i think I am gonna go with it for now
AI gave me this description of it "...Useful for opening MP4 videos, extracting frames, and processing those frames with your existing image code."

<!-- Source: https://mvnrepository.com/artifact/org.bytedeco/javacv-platform -->
```xml
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.14</version>
    <scope>compile</scope>
</dependency>
```


### These next 2 responses came from my own helper agent I made, i think that thats very cool!


**2. Xuggler** — A Java API for working with video and audio, including reading video files and extracting frames. It’s an older project, so check that it supports your operating system andJava version before choosing it.

- I like this as a consideration, however it is mentioned that it is a bit dated, so i will consider it with a grain of salt

<!-- Source: https://mvnrepository.com/artifact/xuggle/xuggle-xuggler -->
```xml
<dependency>
    <groupId>xuggle</groupId>
    <artifactId>xuggle-xuggler</artifactId>
    <version>5.4</version>
</dependency>
```



**3. VLCJ** — A Java wrapper for VLC media player. It can open and play video files, but requires VLC’s native libraries to be installed separately.

- I think this one might be very limiting because it lkocks you into vlc excosystem type of libraries.

<!-- Source: https://mvnrepository.com/artifact/uk.co.caprica/vlcj -->
```xml
<dependency>
    <groupId>uk.co.caprica</groupId>
    <artifactId>vlcj</artifactId>
    <version>4.8.3</version>
</dependency>
```



