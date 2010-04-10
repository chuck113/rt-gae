package com.rt.data;

import com.googlecode.objectify.Key;

import java.io.Serializable;
import java.util.List;

/* used as the entry to represent rhymeLines from one song for a word*/
public class RhymeData implements Serializable {
    private final Key<Song> song;
    private final List<String> rhymeLines;
    private final List<String> rhymeParts;

    public RhymeData(Key<Song> song, List<String> rhymes, List<String> rhymeParts) {
        this.song = song;
        this.rhymeLines = rhymes;
        this.rhymeParts = rhymeParts;
    }

    public Key<Song> getSong() {
        return song;
    }

    public List<String> getRhymeParts() {
        return rhymeParts;
    }

    public List<String> getRhymeLines() {
        return rhymeLines;
    }

    @Override
    public String toString() {
        return "RhymeData{" +
                "song=" + song +
                ", rhymeLines=" + rhymeLines +
                '}';
    }
}
