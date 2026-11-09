package api;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
