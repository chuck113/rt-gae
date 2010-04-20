package com.rt.data;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.rt.indexing.RhymeLeaf;
import com.rt.util.Strings;

import java.io.Serializable;
import java.util.List;
import static com.rt.CollectionsUtils.*;


/* used as the entry to represent rhyme parts and associated from one song for a word*/
public class RhymePartData implements Serializable {
    private final Key<Song> song;
    private final List<String> rhymeLines;
    private final List<String> rhymeParts;
    private final List<String> otherRhymeParts;

    private final int rating;

    public RhymePartData(Key<Song> song, List<String> rhymes, List<String> rhymeParts, int rating, List<String> otherRhymeParts) {
        this.song = song;
        this.rhymeLines = rhymes;
        this.rhymeParts = rhymeParts;
        this.rating = rating;
        this.otherRhymeParts = otherRhymeParts;
    }

    public int getRating() {
        return rating;
    }

    public Key<Song> getSong() {
        return song;
    }

    public List<String> getOtherRhymeParts() {
        return otherRhymeParts;
    }

    public List<String> getRhymeParts() {
        return rhymeParts;
    }

    public List<String> getRhymeLines() {
        return rhymeLines;
    }

    public static RhymePartData createRhymeData(Key<Song> song, RhymeLeaf r, List<String> allKnownParts){
        return new RhymePartData(song,
                toJList(r.lines(), String.class),
                toJList(r.parts(), String.class),
                r.rating(),
                findOtherRhymeParts(toJList(r.lines(), String.class), allKnownParts));
    }

    private static List<String> findOtherRhymeParts(List<String> lines, List<String> allKnownParts){
        List<String> foundParts = Lists.newArrayList();
        for (String line : lines) {
            String[] strings = line.split(" ");
            for (String st : strings) {
                String upperCased = Strings.removePunctuation(st).toUpperCase();
                if(allKnownParts.contains(upperCased)){
                    foundParts.add(upperCased);
                }
            }
        }
        return foundParts;
    }

    @Override
    public String toString() {
        return "RhymePartData{" +
                "song=" + song +
                ", rhymeParts=" + rhymeParts +
                ", rhymeLines=" + rhymeLines +
                ", rating=" + rating +
                '}';
    }
}
