package com.rt.data;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.helper.DAOBase;

import java.util.*;

public class RhymeDao extends DAOBase {

    //public static final int MAX_RHYME_ENTRIES = 100;
    public static final int MAX_RHYME_ENTRIES = 40;

    static {
        ObjectifyService.register(Rhyme.class);
        ObjectifyService.register(Album.class);
        ObjectifyService.register(Song.class);

        ObjectifyService.setDatastoreTimeoutRetryCount(3);
    }

    private Key<Rhyme> rhymeKey(String val) {
        return new Key<Rhyme>(Rhyme.class, val);
    }

    /**
     * adds songs from list newRhymes to list existing, assumes
     */
    private List<Rhyme> mergeRhymeListsOnWords(Map<Key<Rhyme>, Rhyme> existing, List<Rhyme> newRhymes) {
        if (existing.size() == 0) {
            return newRhymes;
        }

        List<Rhyme> combindedList = new ArrayList<Rhyme>(existing.size());

        for (Rhyme newRhyme : newRhymes) {
            if (existing.containsKey(rhymeKey(newRhyme.getWord()))) {
                Iterable<RhymePartData> merged = Iterables.concat(newRhyme.getRhymes(), existing.get(rhymeKey(newRhyme.getWord())).getRhymes());
                combindedList.add(Rhyme.createRhyme(newRhyme.getWord(), Lists.newArrayList(merged)));
            } else {
                combindedList.add(newRhyme);
            }
        }

        return combindedList;
    }

    private void printCalculateLargestRhymes(List<Rhyme> rhymes) {
        //int rhymesCount = 0;
        int largestLinesCharCount = 0;
        Rhyme largestRhyme = null;
        for (Rhyme rhyme : rhymes) {
            //int totalRhymes = rhyme.getRhymes().size();
            int totalLineChars = 0;
            for (RhymePartData d : rhyme.getRhymes()) {
                List<String> stringList = d.getRhymeLines();
                for (String s : stringList) {
                    totalLineChars += s.length();
                }
            }
            if (totalLineChars > largestLinesCharCount) {
                largestLinesCharCount = totalLineChars;
                largestRhyme = rhyme;
            }
        }

        System.out.println("RhymeDao.printCalculateLargestRhymes largest rhymes was of size " + largestLinesCharCount + " for rhyme " + largestRhyme.getWord() + " with " + largestRhyme.getRhymes().size() + " rhyme entries");
    }

    /**
     * limit rhyme entries to 100
     *
     * @return
     */
    private List<Rhyme> removeExcessiveRhymeEntries(List<Rhyme> rhymesRaw) {
        List<Rhyme> rhymes = Lists.newArrayList(rhymesRaw);
        List<Rhyme> modifiedRhymes = Lists.newArrayList();
        for (Iterator<Rhyme> iter = rhymes.iterator(); iter.hasNext();) {
            Rhyme rhyme = iter.next();

            if (rhyme.getRhymes().size() > MAX_RHYME_ENTRIES) {
                //System.out.println("RhymeDao.removeExcessiveRhymeEntries "+rhyme.getWord()+" has "+rhyme.getRhymes().size()+" entries");
                iter.remove();
                List<RhymePartData> dataList = Lists.newArrayList(rhyme.getRhymes());
                
                Collections.sort(dataList, new Comparator<RhymePartData>() {
                    public int compare(RhymePartData o1, RhymePartData o2) {
                        // sort in decending order
                        return o2.getRating() - o1.getRating();
                    }
                });

                //System.out.println("RhymeDao.removeExcessiveRhymeEntries best was "+dataList.get(0)+" removing "+dataList.subList(100, dataList.size()));
                modifiedRhymes.add(new Rhyme(rhyme.getWord(), Lists.<RhymePartData>newArrayList(dataList.subList(0, 100)), rhyme.getRhymeWordRating()));
            }
        }
        for (Rhyme modifiedRhyme : modifiedRhymes) {
            rhymes.add(modifiedRhyme);
        }
        return rhymes;
    }

    public void addRhymes(List<Rhyme> newRhymes) {
        Map<Key<Rhyme>, Rhyme> existingRhymes = getExistingRhymesUsingNew(newRhymes);
        List<Rhyme> toInsert = removeExcessiveRhymeEntries(mergeRhymeListsOnWords(existingRhymes, newRhymes));

        //System.out.println("RhymeDao.addRhymes will insert " + toInsert.size() + " rhymes");
        int batchSize = 10;
        if (toInsert.size() > 0) {
            for (List<Rhyme> rhymeList : Iterables.partition(toInsert, batchSize)) {
                try {
                    //System.out.println("RhymeDao.addRhymes will insert " + rhymeList);
                    ofy().put(rhymeList);
                } catch (ApiProxy.RequestTooLargeException e) {
                    System.err.println("RhymeDao.addRhymes failed to add rhyme list: " + e.getMessage());
                    printCalculateLargestRhymes(toInsert);
                }
            }
        }
    }

    private Iterable<Key<Rhyme>> getKeys(List<Rhyme> rhymes) {
        return Iterables.transform(rhymes, new Function<Rhyme, Key<Rhyme>>() {
            public Key<Rhyme> apply(Rhyme rhyme) {
                return rhymeKey(rhyme.getWord());
            }
        });
    }

    private Map<Key<Rhyme>, Rhyme> getExistingRhymesUsingNew(List<Rhyme> rhymes) {
        Map<Key<Rhyme>, Rhyme> map = ofy().get(getKeys(rhymes));
        Map<Key<Rhyme>, Rhyme> result = Maps.newHashMap();
        for (Key<Rhyme> rhymeKey : map.keySet()) {
            if (map.get(rhymeKey) != null) {
                result.put(rhymeKey, map.get(rhymeKey));
            }
        }
        return result;
    }

//    public Rhyme addRhymes(String word, List<RhymePartData> rhymes){
//        Rhyme r = new Rhyme(word, rhymes);
//        ofy().put(r);
//        return r;
//    }

//    public Rhyme addRhyme(String word, RhymePartData d){
//        Rhyme rhyme = lookUpWord(word);
//        if(rhyme == null){
//            rhyme = new Rhyme(word, Collections.singletonList(d));
//        }else{
//            List<RhymePartData> rhymes = Lists.newArrayList(rhyme.getRhymes());
//            rhymes.add(d);
//            rhyme.setRhymes(rhymes);
//        }
//        ofy().put(rhyme);
//
//        Rhyme rhymeForDebug = lookUpWord(word);
//
//        return rhyme;
//    }

    public Song addSong(String title, int trackNo, String url, Key<Album> album) {
        Song s = new Song(title, trackNo, url, album);
        ofy().put(s);
        return s;
    }

    public Album addAlbum(String title, String artist, int year) {
        Album a = new Album(title, artist, year);
        ofy().put(a);
        //System.out.println("RhymeDao.addAlbum a id = "+a.getId());
        return a;
    }

    public List<Rhyme> getRandomRhymes(int number) {
        List<Rhyme> rhymes = bestRhymes();
        List<Rhyme> result = Lists.newArrayList();

        if(rhymes.size() < number){
            return rhymes;
        }

        for (Iterator<Rhyme> iterator = rhymes.iterator(); iterator.hasNext() && result.size() < number;) {
            iterator.next();// only using this as a count so we don't go over the end

            int rand = (int) ((Math.random() * new Double(rhymes.size()/2)));
            Rhyme next = rhymes.get(rand);
            String nextWord =next.getWord();
            boolean shouldInsert = true;

            // test each entry to see if it is contained in outer loop
            for(Rhyme r:result){
                String existingWord = r.getWord();
                if(nextWord.length() < 4){
                    shouldInsert = false;break;
                }else{
                    String existingEnd = existingWord.substring(existingWord.length()-3, existingWord.length());
                    String nextEnd = nextWord.substring(nextWord.length()-3, nextWord.length());
                    if(existingEnd.equals(nextEnd)){
                        System.out.println("RhymeDao.getRandomRhymes NOT returning word: "+nextWord);
                        shouldInsert = false;break;
                    }

                    System.out.println("RhymeDao.getRandomRhymes end of "+existingWord+" is "+existingEnd);
                    System.out.println("RhymeDao.getRandomRhymes nextEnd of "+nextWord+" is "+nextEnd+" should insert is "+shouldInsert);
                }
            }
            if(shouldInsert){
                result.add(next);
            }
        }
        return result;
        //return rhymes.get((int) ((Math.random() * new Double(rhymes.size()/2))));
    }

    public void deleteAll() {
        deleteType(Album.class);
        deleteType(Song.class);
        deleteType(Rhyme.class);
    }

    public <T> void deleteType(Class<T> type) {
        QueryResultIterable<Key<T>> resultIterable = ofy().query(type).fetchKeys();
        //QueryResultIterator<? extends Key<? extends Object>> queryResultIterator = resultIterable.iterator();

        //Iterable<List<Key>> partitioned =
        Iterable<List<Key<T>>> iterable = Iterables.partition(resultIterable, 100);
        for (List<? extends Key<? extends Object>> keys : iterable) {
            ofy().delete(keys);
        }
    }


    public ArrayList<Rhyme> allRhymes() {
        Query<Rhyme> rhymeQuery = ofy().query(Rhyme.class).order("-rhymeWordRating");
        return Lists.newArrayList(rhymeQuery.iterator());
    }

    public boolean containsArtist(String artist) {
        return ofy().query(Album.class).filter("artist =", artist).countAll() > 0;
    }

    public Rhyme lookUpWord(String wordRaw) {
        String word = wordRaw.toUpperCase();
        return ofy().query(Rhyme.class).filter("word =", word).order("-rhymeWordRating").get();
    }

    public List<Rhyme> bestRhymes() {
        Query<Rhyme> rhymeQuery = ofy().query(Rhyme.class).order("-rhymeWordRating").limit(200);
        System.out.println("RhymeDao.bestRhymes " + Lists.newArrayList(rhymeQuery.iterator()).get(0).getRhymeWordRating());
        return Lists.newArrayList(rhymeQuery.iterator());
    }

    public Album lookUpAlbum(Key<Album> key) {
        try {
            return ofy().get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Song lookUpSong(Key<Song> key) {
        try {
            return ofy().get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Key<Song>> addSongBatch(List<Song> songBatch) {
        if (songBatch.size() == 0) {
            return Collections.emptyList();
        } else {
            return ofy().put(songBatch);
        }
    }
}
