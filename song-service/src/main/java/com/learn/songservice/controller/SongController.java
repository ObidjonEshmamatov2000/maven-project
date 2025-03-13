package com.learn.songservice.controller;

import com.learn.songservice.dto.SongDTO;
import com.learn.songservice.entity.Song;
import com.learn.songservice.service.SongService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @PostMapping
    public ResponseEntity<?> createSong(@Valid @RequestBody SongDTO songDTO) {
        Song created = songService.createSong(songDTO);
        return ResponseEntity.ok(Map.of("id", created.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSong(@PathVariable("id") Long id) {
        Song song = songService.getSong(id);
        return ResponseEntity.ok(song);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSongs(@RequestParam("id") String csvIds) {
        List<Long> deletedIds = songService.deleteSongs(csvIds);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}
