package com.rt.data;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

import javax.persistence.Id;

public class Song {
    @Id Long id;
    String title;
    int trackNumber;
    String url;
    /*@Parent */Key<Album> album;

    public Song(){        
    }

    public Song(String title, int trackNumber, String url, Key<Album> album) {
        this.title = title;
        this.trackNumber = trackNumber;
        this.url = url;
        this.album = album;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Key<Album> getAlbum() {
        return album;
    }

    public void setAlbum(Key<Album> album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", url='" + url + '\'' +
                ", album=" + album +
                '}';
    }
}
