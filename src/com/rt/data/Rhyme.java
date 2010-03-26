package com.rt.data;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

import javax.persistence.Embedded;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Unindexed
public class Rhyme {
    private @Id String word;
    private @Serialized List<RhymeData> rhymes = new ArrayList<RhymeData>();

    public Rhyme() {
    }

    public Rhyme(String word, List<RhymeData> rhymes) {
        this.word = word;
        this.rhymes = rhymes;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<RhymeData> getRhymes() {
        return rhymes;
    }

    public void setRhymes(List<RhymeData> rhymes) {
        this.rhymes = rhymes;
    }

    @Override
    public String toString() {
        return "Rhyme{" +
                "word='" + word + '\'' +
                ", rhymes=" + rhymes +
                '}';
    }
}
