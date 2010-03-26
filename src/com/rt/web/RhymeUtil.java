package com.rt.web;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.googlecode.objectify.Key;
import com.rt.data.*;
import com.rt.dto.DataMapper;
import com.rt.indexing.RhymeLines;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RhymeUtil {

    public static final class RhymeData {
        public RhymeData(String title, String artist, List<String> lines) {
            this.title = title;
            this.artist = artist;
            this.lines = lines;
        }

        private final String title;
        private final String artist;
        private final List<String> lines;

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

    /**
     * FIXME can do this because we know only the last words will rhyme
     */
    public static String wrapLastWordInCss(String lineRaw) {
        //<span class="rhymeWord">word</span>
        String line = lineRaw.trim();
        int lastSpace = line.lastIndexOf(" ");
        if (lastSpace == -1) return line;

        return line.substring(0, lastSpace) + "<span class=\"rhymeWord\">" + line.substring(lastSpace) + "</span>";
    }

    private static Map<String, List<RhymeLines>> getRhymes() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("indexes/index.ser");
        try {
            DataMapper d = new DataMapper();
            return d.read(resource.openStream());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

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
            res.add(new RhymeData(song.getTitle(), album.getArtist(), d.getRhymes()));
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

    public static List<RhymeData> findRhymesOld(String word) {
        Map<String, List<RhymeLines>> rhymes = getRhymes();

        List<RhymeLines> res = rhymes.get(word);
        System.out.println("RhymeUtil.findRhymes got rhymes: " + res + " for " + word);
        if (res != null) {
            List<RhymeData> rhymeData = new ArrayList<RhymeData>(res.size());
            for (RhymeLines r : res) {
                rhymeData.add(new RhymeData(r.song().title(), r.song().artist(), r.linesAsJavaList()));
            }
            return rhymeData;
        }
        return null;
        //new DataMapper()
    }
}
