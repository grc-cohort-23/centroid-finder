# Video Libraries


## [JavaCV](https://bytedeco.org/javacv/apidocs/)
### Pros:
- Widly used libray and updated recently
- Able to give a BufferedImage for each frame in the video
- Supports live video feed

### Cons:
- Large download

--- 
```xml
<!-- Source: https://mvnrepository.com/artifact/org.bytedeco/javacv -->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv</artifactId>
    <version>1.5.14</version>
    <scope>compile</scope>
</dependency>
```

----

## [OpenCV](https://docs.opencv.org/4.13.0/javadoc/index.html)
### Pros
- Last updated July 2026
- Supports live video feed

### Cons
- Have to convert a Mat to BufferedImage

--- 
```xml
<!-- Source: https://mvnrepository.com/artifact/org.opencv/opencv -->
<dependency>
    <groupId>org.opencv</groupId>
    <artifactId>opencv</artifactId>
    <version>5.0.0.1</version>
    <scope>runtime</scope>
</dependency>
```
---
## [JCodec](http://www.jcodec.org/)
### Pros
- FrameGrab.getFrameFromFile returns a BufferedImage
### Cons
- Does not support live video
- Last updated July 2019

---
```xml
<!-- Source: https://mvnrepository.com/artifact/org.jcodec/jcodec -->
<dependency>
    <groupId>org.jcodec</groupId>
    <artifactId>jcodec</artifactId>
    <version>0.2.5</version>
    <scope>compile</scope>
</dependency>
```