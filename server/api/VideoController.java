package api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoController {

@GetMapping("/api/video/{fileName}")
 public ResponseEntity<Resource> getVideo(@PathVariable String fileName){
    File file = new File("videos/"+fileName);
    if(!file.exists()){
        return ResponseEntity.notFound().build();
    }
    Resource video = new FileSystemResource(file);
    return ResponseEntity.ok()
    .contentType(MediaType.parseMediaType("video/mp4"))
    .body(video);
 }

 @PostMapping("/api/process/{fileName}")
public ResponseEntity<String> startProcess(@PathVariable String fileName,
                                           @RequestParam String targetColor,
                                           @RequestParam int threshold) {
    File video = new File("videos/" + fileName);
    if (!video.isFile()) {
        return ResponseEntity.status(404).body("Video not found: " + fileName);
    }
   String jobId = UUID.randomUUID().toString();
    ProcessBuilder builder = new ProcessBuilder("java", "-jar", "../centriod-finder-jar/target/videoprocessor.jar",
        "videos/" + fileName,
        "metadata/" + jobId + ".csv",
        targetColor,
        String.valueOf(threshold));
    builder.inheritIO(); // show the processor's output/errors in the server console
    try {
        builder.start();
    } catch (IOException e) {
        return ResponseEntity.internalServerError().body("Could not start processing");
    }
    //Esxpress version: res.status(202).send(jobId)
    return ResponseEntity.accepted().body(jobId);

}

  @GetMapping("/api/videos")
public List<String> getAllVideos(){
    List<String> names = new ArrayList<>();
    for (File file : new File("videos").listFiles()) {
        names.add(file.getName());
    }
    return names;
}

@GetMapping("/api/jobcheck")
public boolean jobCheck(){
    return false;
}
}
