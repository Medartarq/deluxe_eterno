package com.deluxeterno.web;

import com.deluxeterno.service.MediaStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.*;
import org.springframework.core.io.Resource;
import java.util.Map;

@RestController
public class MediaController {
    private final MediaStorageService storage;
    public MediaController(MediaStorageService storage) { this.storage=storage; }
    @PostMapping("/admin/media")
    public ResponseEntity<Map<String,String>> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url",storage.store(file)));
    }
    @GetMapping("/media/{filename}")
    public ResponseEntity<Resource> read(@PathVariable String filename) {
        var resource=storage.load(filename);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)))
                .contentType(filename.endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG).body(resource);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalidImage(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error",exception.getMessage()));
    }
}
