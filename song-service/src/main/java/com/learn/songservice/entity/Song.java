package com.learn.songservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(nullable = false, length = 100)
    private String album;

    @Column(nullable = false, length = 5)
    private String duration;

    @Column(nullable = false, length = 4)
    private String year;

    public Song() {
    }

    public Song(Long id, String name, String artist, String album, String duration, String year) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getDuration() {
        return duration;
    }

    public String getYear() {
        return year;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
