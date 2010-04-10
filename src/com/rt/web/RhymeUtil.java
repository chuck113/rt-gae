package com.rt.web;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.googlecode.objectify.Key;
import com.rt.data.*;
import com.rt.dto.DataMapper;
import com.rt.indexing.RhymeLines;
import com.rt.util.NameMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RhymeUtil {

    public static final class RhymeData {
        private final String title;
        private final String artist;
        private final List<String> lines;
        private final List<String> parts;

        public RhymeData(String title, String artist, List<String> lines, List<String> parts) {
            this.title = title;
            this.artist = artist;
            this.lines = lines;
            this.parts = parts;
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

    public static String wrapPartsInCss(String lineRaw, List<String> parts){
        List<Integer> wordsToWrapInCss = new ArrayList<Integer>();
        // TODO use method as we use to generate rhymes

        String[] uppercaseWords = NameMapper.nSpace(lineRaw).split(" ");
        for(int i=0; i< uppercaseWords.length; i++){
            if(parts.contains(uppercaseWords[i])){
                wordsToWrapInCss.add(i);
            }
        }

        String[] words = lineRaw.split(" ");
        StringBuilder builder = new StringBuilder();

        for(int i=0; i< words.length; i++){
            if(wordsToWrapInCss.contains(i)){
                builder.append(applyCss(words[i]));
            }else{
                builder.append(words[i]);
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    private static String applyCss(String st){
      return "<span class=\"rhymeWord\">" + st + "</span>";
    }

//    /**
//     * FIXME can do this because we know only the last individualWords will rhyme
//     */
//    public static String wrapLastWordInCss(String lineRaw) {
//        //<span class="rhymeWord">word</span>
//        String line = lineRaw.trim();
//        int lastSpace = line.lastIndexOf(" ");
//        if (lastSpace == -1) return line;
//
//        return line.substring(0, lastSpace) + "<span class=\"rhymeWord\">" + line.substring(lastSpace) + "</span>";
//    }
//
//    private static Map<String, List<RhymeLines>> getRhymes() {
//        URL resource = Thread.currentThread().getContextClassLoader().getResource("indexes/index.ser");
//        try {
//            DataMapper d = new DataMapper();
//            return d.read(resource.openStream());
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            return null;
//        }
//    }

    public static RandomResult random() {
        RhymeDao rhymeDao = new RhymeDao();
        Rhyme rhyme = rhymeDao.getRandomRhyme();
        return new RandomResult(convert(rhyme, rhymeDao), rhyme.getWord());
    }

    //TODO this reads from the data store too much.
    private static List<RhymeData> convert(Rhyme rhyme, RhymeDao rhymeDao){
        List<com.rt.data.RhymeData> dataList = rhyme.getRhymes();
        List<RhymeData> res = new ArrayList<RhymeData>();
        for(com.rt.data.RhymeData d : dataList){
            Song song = rhymeDao.lookUpSong(d.getSong());
            Album album = rhymeDao.lookUpAlbum(song.getAlbum());
            res.add(new RhymeData(song.getTitle(), album.getArtist(), d.getRhymeLines(), d.getRhymeParts()));
        }
        return res ;
    }

    public static List<RhymeData> findRhymes(String word) {
        RhymeDao rhymeDao = new RhymeDao();
        Rhyme rhyme = rhymeDao.lookUpWord(word);
        if(rhyme == null)return null;

        return convert(rhyme, rhymeDao);
    }

    public static Iterable<Key<Song>> getSongs(List<com.rt.data.RhymeData> dataList){
        return Iterables.transform(dataList, new Function<com.rt.data.RhymeData, Key<Song>>(){
            public Key<Song> apply(com.rt.data.RhymeData rhymeData) {
                return rhymeData.getSong();
            }
        });
    }
}

