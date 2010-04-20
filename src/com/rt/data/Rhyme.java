package com.rt.data;

import com.googlecode.objectify.annotation.Indexed;
import com.googlecode.objectify.annotation.Serialized;
import com.googlecode.objectify.annotation.Unindexed;

import javax.persistence.Id;
import java.util.*;

@Unindexed
public class Rhyme {
    private @Id String word;
    private @Serialized List<RhymePartData> rhymes = new ArrayList<RhymePartData>();
    private @Indexed int rhymeWordRating;

    public Rhyme() {
    }

    /**
     * Assumes rhymes are sorted on rating
     */
    public Rhyme(String word, List<RhymePartData> rhymes, int rhymeWordRating) {
        this.word = word;
        this.rhymes = rhymes;
        this.rhymeWordRating = rhymeWordRating;
    }

    public int getRhymeWordRating() {
        return rhymeWordRating;
    }

    public void setRhymeWordRating(int rhymeWordRating) {
        this.rhymeWordRating = rhymeWordRating;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<RhymePartData> getRhymes() {
        return rhymes;
    }

    public void setRhymes(List<RhymePartData> rhymes) {
        this.rhymes = rhymes;
    }

    
    /** factory which sorts rhymes correctly when creating a Rhyme object */
    public static Rhyme createRhyme(String word, List<RhymePartData> rhymes){
        if(word == null || word.length() == 0){
            System.out.println("Rhyme.createRhyme got null word with rhmes "+rhymes);
        }
        List<RhymePartData> rhymesCopy = new ArrayList<RhymePartData>(rhymes);

        /** sorting so largest number is at the top of the list */
        Collections.sort(rhymesCopy, new Comparator<RhymePartData>(){
            public int compare(RhymePartData o1, RhymePartData o2) {
                return o2.getRating() - o1.getRating();
            }
        });

        int result = new RhymeWordRatingCalculator().calculate(rhymesCopy);
        return new Rhyme(word, rhymesCopy, result);
    }

    @Override
    public String toString() {
        return "Rhyme{" +
                "word='" + word + '\'' +
                ", rhymes=" + rhymes +
                '}';
    }
}
