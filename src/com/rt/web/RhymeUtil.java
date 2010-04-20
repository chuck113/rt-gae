package com.rt.web;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.rt.data.*;
import com.rt.util.NameMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RhymeUtil {

    public static final class RhymeData {
        private final String title;
        private final String artist;
        private final List<String> lines;
        private final List<String> parts;
        private final List<String> referenceParts;

        public RhymeData(String title, String artist, List<String> lines, List<String> parts, List<String> partsFromOtherRhymes) {
            this.title = title;
            this.artist = artist;
            this.lines = lines;
            this.parts = parts;
            this.referenceParts = partsFromOtherRhymes;
        }

        public List<String> getParts() {
            return parts;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public List<String> getLines() {
            return lines;
        }

        public List<String> getReferenceParts() {
            return referenceParts;
        }
    }

    public static final class RandomResult {
        private final List<RhymeData> rhymeData;
        private final String word;

        public RandomResult(List<RhymeData> rhymeData, String word) {
            this.rhymeData = rhymeData;
            this.word = word;
        }

        public List<RhymeData> getRhymeData() {
            return rhymeData;
        }

        public String getWord() {
            return word;
        }
    }

//    public static String wrapOtherPartsInCss(String lineRaw, List<String> otheParts){
//        return wrapStringsInCss(lineRaw, otheParts, new ReferencingRhymePartCssApplyer());
//    }
//
//    public static String wrapPartsInCss(String lineRaw, List<String> parts){
//        return wrapStringsInCss(lineRaw, parts, new RhymePartCssApplyer());
//    }

    public static void main(String[] args) {
        RhymeData d = new RhymeData("", "", Collections.singletonList("No hopes folks, I quote note for note / You mind float on the rhyme on I wrote"),
                Lists.newArrayList("QUOTE", "NOTE", "FLOAT", "WROTE"), Collections.<String>emptyList());
        
        String line = "You mind float on the rhyme on I wrote";
        System.out.println("RhymeUtil.main: "+wrapStringsInCss(line, d));
    }

    public static String wrapStringsInCss(String lineRaw, RhymeData rhyme){
        List<Integer> wordsToWrapInPartCss = new ArrayList<Integer>();
        List<Integer> wordsToWrapInRefPartCss = new ArrayList<Integer>();
        // TODO use method as we use to generate rhymes

        String[] uppercaseWords = NameMapper.nSpace(lineRaw).split(" ");
        for(int i=0; i< uppercaseWords.length; i++){
            if(rhyme.getParts().contains(uppercaseWords[i])){
                wordsToWrapInPartCss.add(i);
            }else if(rhyme.getReferenceParts().contains(uppercaseWords[i])){
                wordsToWrapInRefPartCss.add(i);
            }
        }

        String[] words = lineRaw.split(" ");
        StringBuilder builder = new StringBuilder();

        for(int i=0; i< lineRaw.split(" ").length; i++){
            if(wordsToWrapInPartCss.contains(i)){
                builder.append(applyPartCss(words[i]));
            }else if(wordsToWrapInRefPartCss.contains(i)){
                builder.append(applyRefPartCss(words[i]));
            }else{
                builder.append(words[i]);
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    private static String applyRefPartCss(String st){
        String href = "<a href=\"/index.jsp?word="+st+"\">"+st+"</a>";
        return "<span class=\"rrp\">" + href + "</span>";
    }

     private static String applyPartCss(String st){
        String href = "<a href=\"/index.jsp?word="+st+"\">"+st+"</a>";
        return "<span class=\"rp\">" + href + "</span>";
    }

     public static List<Rhyme> randoms(int amount) {
        return new RhymeDao().getRandomRhymes(amount);
    }

    public static RandomResult random() {
        RhymeDao rhymeDao = new RhymeDao();
        Rhyme rhyme = rhymeDao.getRandomRhymes(1).get(0);
        return new RandomResult(convert(rhyme, rhymeDao), rhyme.getWord());
    }

    //TODO this reads from the data store too much.
    private static List<RhymeData> convert(Rhyme rhyme, RhymeDao rhymeDao){
        List<RhymePartData> dataList = rhyme.getRhymes();
        List<RhymeData> res = new ArrayList<RhymeData>();
        for(RhymePartData d : dataList){
            Song song = rhymeDao.lookUpSong(d.getSong());
            Album album = rhymeDao.lookUpAlbum(song.getAlbum());
            res.add(new RhymeData(song.getTitle(), album.getArtist(), d.getRhymeLines(), d.getRhymeParts(), d.getOtherRhymeParts()));
        }
        return res ;
    }


    public static List<RhymeData> findRhymes(String word) {
        RhymeDao rhymeDao = new RhymeDao();
        Rhyme rhyme = rhymeDao.lookUpWord(word);

        return rhyme == null ? null : convert(rhyme, rhymeDao);
    }

    public static Iterable<Key<Song>> getSongs(List<RhymePartData> dataList){
        return Iterables.transform(dataList, new Function<RhymePartData, Key<Song>>(){
            public Key<Song> apply(RhymePartData rhymeData) {
                return rhymeData.getSong();
            }
        });
    }
}

