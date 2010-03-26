package com.rt.data;

import com.googlecode.objectify.Key;

import java.io.Serializable;
import java.util.List;

/* used as the entry to represent rhymes from one song for a word*/
public class RhymeData implements Serializable {
    private final Key<Song> song;
    private final List<String> rhymes;

    public RhymeData(Key<Song> song, List<String> rhymes) {
        this.song = song;
        this.rhymes = rhymes;
    }

    public Key<Song> getSong() {
        return song;
    }

    public List<String> getRhymes() {
        return rhymes;
    }

    @Override
    public String toString() {
        return "RhymeData{" +
                "song=" + song +
                ", rhymes=" + rhymes +
                '}';
    }
}
