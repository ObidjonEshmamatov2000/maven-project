package com.learn.resourceservice.service;

import com.learn.resourceservice.dto.SongDTO;
import com.learn.resourceservice.entity.Resource;
import com.learn.resourceservice.repository.ResourceRepository;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${songs.url}")
    private String songServiceUrl;

    @Value("${songs.port}")
    private String songServicePort;

    public String getFullUrl() {
        return "http://" + songServiceUrl + ":" + songServicePort + "/songs";
    }

    public Long uploadResource(byte[] mp3Data) {
        Tika tika = new Tika();
        String mimeType = tika.detect(mp3Data);
        if (!"audio/mpeg".equals(mimeType)) {
            throw new IllegalArgumentException("Invalid MP3 file");
        }

        Resource resource = new Resource(mp3Data);
        resource = resourceRepository.save(resource);
        Long resourceId = resource.getId();

        Metadata metadata = new Metadata();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(mp3Data)) {
            Mp3Parser parser = new Mp3Parser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();
            parser.parse(bais, handler, metadata, context);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing MP3 metadata", e);
        }

        String title = getOrDefault(metadata, "title", "Unknown Title");
        String artist = getOrDefault(metadata, "xmpDM:artist", "Unknown Artist");
        String album = getOrDefault(metadata, "xmpDM:album", "Unknown Album");
        String releaseDate = getOrDefault(metadata, "xmpDM:releaseDate", "1900");
        String durationStr = getOrDefault(metadata, "xmpDM:duration", "0");

        String formattedDuration = convertDuration(durationStr);

        SongDTO songDTO = new SongDTO();
        songDTO.setId(resourceId);
        songDTO.setName(title);
        songDTO.setArtist(artist);
        songDTO.setAlbum(album);
        songDTO.setDuration(formattedDuration);
        songDTO.setYear(releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "1900");
        System.out.println("SONG url: " + getFullUrl());
        restTemplate.postForObject(getFullUrl(), songDTO, SongDTO.class);

        return resourceId;
    }

    private String getOrDefault(Metadata metadata, String key, String defaultValue) {
        String value = metadata.get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    private String convertDuration(String durationStr) {
        try {
            double seconds = Double.parseDouble(durationStr);
            int minutes = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            return String.format("%02d:%02d", minutes, secs);
        } catch (NumberFormatException e) {
            return "00:00"; // Fallback if conversion fails
        }
    }

    public Resource getResource(Long id) {
        validateId(id);
        return resourceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Resource with ID=" + id + " not found"));
    }

    public List<Long> deleteResources(String csvIds) {
        validateCsvIds(csvIds);
        List<Long> deletedIds = new ArrayList<>();
        List<Long> ids = Arrays.stream(csvIds.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        ids.forEach(id -> {
            if (resourceRepository.existsById(id)) {
                deletedIds.add(id);
                resourceRepository.deleteById(id);
                restTemplate.delete(songServiceUrl + "?id=" + id);
            }
        });

        return deletedIds;
    }

    public void validateMp3Data(byte[] mp3Data) {
        if (mp3Data == null || mp3Data.length == 0) {
            throw new IllegalArgumentException("Audio file is required");
        }
    }

    public void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID = " + id);
        }
    }

    public void validateCsvIds(String csvIds) {
        if (csvIds == null || csvIds.isEmpty()) {
            throw new IllegalArgumentException("CSV IDs are required");
        }
        if (csvIds.length() >= 200) {
            throw new IllegalArgumentException("CSV string length must be less than 200 characters. Got " + csvIds.length());
        }
        String[] ids = csvIds.split(",");
        for (String idStr : ids) {
            try {
                long id = Long.parseLong(idStr.trim());
                validateId(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid ID format: " + idStr);
            }
        }
    }
}
