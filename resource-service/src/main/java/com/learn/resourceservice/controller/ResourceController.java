package com.learn.resourceservice.controller;

import com.learn.resourceservice.entity.Resource;
import com.learn.resourceservice.service.ResourceService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<?> uploadResource(@RequestBody byte[] mp3Data)
            throws IOException, TikaException, SAXException {
        resourceService.validateMp3Data(mp3Data);
        Long resourceId = resourceService.uploadResource(mp3Data);
        return ResponseEntity.ok(Map.of("id", resourceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResource(@PathVariable("id") Long id) {
        Resource resource = resourceService.getResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(resource.getData());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResources(@RequestParam("id") String csvIds) {
        List<Long> ids = resourceService.deleteResources(csvIds);
        return ResponseEntity.ok(Map.of("ids", ids));
    }
}
