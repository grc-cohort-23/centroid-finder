package api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoController {

@GetMapping("/api/video/{fileName}")
 public String getVideo(@PathVariable String fileName){
    return fileName;
 }
}
