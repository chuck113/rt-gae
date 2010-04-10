package com.rt.data;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.helper.DAOBase;

import java.util.*;

public class RhymeDao extends DAOBase {
    static {
        ObjectifyService.register(Rhyme.class);
        ObjectifyService.register(Album.class);
        ObjectifyService.register(Song.class);

        ObjectifyService.setDatastoreTimeoutRetryCount(3);
    }

    private Key<Rhyme> rhymeKey(String val){
        return new Key<Rhyme>(Rhyme.class, val);
    }

    /** adds songs from list newRhymes to list existing, assumes  */
    private List<Rhyme> mergeRhymeListsOnWords(Map<Key<Rhyme>, Rhyme> existing, List<Rhyme> newRhymes){
        if(existing.size() == 0){
            return newRhymes;
        }
        
        List<Rhyme> combindedList = new ArrayList<Rhyme>(existing.size());

        for (Rhyme newRhyme : newRhymes) {
            if(existing.containsKey(rhymeKey(newRhyme.getWord()))){
                Iterable<RhymeData> merged = Iterables.concat(newRhyme.getRhymes(), existing.get(rhymeKey(newRhyme.getWord())).getRhymes());
                combindedList.add(new Rhyme(newRhyme.getWord(),Lists.newArrayList(merged)));
            }else{
                combindedList.add(newRhyme);
            }
        }

        return combindedList;
    }

    public void addRhymes(List<Rhyme> newRhymes){
        Map<Key<Rhyme>, Rhyme> existingRhymes = getExistingRhymesUsingNew(newRhymes);
        List<Rhyme> toInsert = mergeRhymeListsOnWords(existingRhymes, newRhymes);

        System.out.println("RhymeDao.addRhymes will insert "+toInsert.size()+" rhymes");
        int batchSize = 100;
        if(toInsert.size() > 0){
            for (List<Rhyme> rhymeList : Iterables.partition(toInsert, batchSize)) {
                ofy().put(rhymeList);
            }
        }
    }

    private Iterable<Key<Rhyme>> getKeys(List<Rhyme> rhymes){
        return Iterables.transform(rhymes, new Function<Rhyme, Key<Rhyme>>(){
            public Key<Rhyme> apply(Rhyme rhyme) {
                return rhymeKey(rhyme.getWord());
            }
        });
    }

    private Map<Key<Rhyme>, Rhyme> getExistingRhymesUsingNew(List<Rhyme> rhymes){
        Map<Key<Rhyme>, Rhyme> map = ofy().get(getKeys(rhymes));
        Map<Key<Rhyme>, Rhyme> result = Maps.newHashMap();
        for (Key<Rhyme> rhymeKey : map.keySet()) {
            if(map.get(rhymeKey) != null){
                result.put(rhymeKey, map.get(rhymeKey));
            }
        }
        return result;
    }

    public Rhyme addRhymes(String word, List<RhymeData> rhymes){
        Rhyme r = new Rhyme(word, rhymes);
        ofy().put(r);
        return r;
    }

    public Rhyme addRhyme(String word, RhymeData d){
        Rhyme rhyme = lookUpWord(word);
        if(rhyme == null){
            rhyme = new Rhyme(word, Collections.singletonList(d));
        }else{
            List<RhymeData> rhymes = Lists.newArrayList(rhyme.getRhymes());
            rhymes.add(d);
            rhyme.setRhymes(rhymes);
        }
        ofy().put(rhyme);

        Rhyme rhymeForDebug = lookUpWord(word);

        return rhyme;
    }

    public Song addSong(String title, int trackNo, String url, Key<Album> album){
        Song s = new Song(title, trackNo, url, album);
        ofy().put(s);
        return s;
    }

    public Album addAlbum(String title, String artist, int year){
        Album a = new Album(title, artist, year);
        ofy().put(a);
        //System.out.println("RhymeDao.addAlbum a id = "+a.getId());
        return a;
    }

    //FIXME - may not work very well - use memcache for this
    public Rhyme getRandomRhyme(){
        //int count = ofy().query(Rhyme.class).countAll();
        QueryResultIterable<Key<Rhyme>> resultIterable = ofy().query(Rhyme.class).fetchKeys();
        Key<Rhyme> rhymeKey = resultIterable.iterator().next();
        return ofy().find(rhymeKey);
    }

    public void deleteAll(){
        deleteType(Album.class);
        deleteType(Song.class);
        deleteType(Rhyme.class);
    }

    public <T> void deleteType(Class<T> type){
        QueryResultIterable<Key<T>> resultIterable = ofy().query(type).fetchKeys();
        //QueryResultIterator<? extends Key<? extends Object>> queryResultIterator = resultIterable.iterator();

        //Iterable<List<Key>> partitioned =
        Iterable<List<Key<T>>> iterable = Iterables.partition(resultIterable, 100);
        for (List<? extends Key<? extends Object>> keys : iterable) {
            ofy().delete(keys);
        }
    }

    public boolean containsArtist(String artist){
        return ofy().query(Album.class).filter("artist =", artist).countAll() > 0;
    }

    public Rhyme lookUpWord(String wordRaw){
        String word = wordRaw.toUpperCase();
        return ofy().query(Rhyme.class).filter("word =", word).get();
    }

    public Album lookUpAlbum(Key<Album> key){
        try {
            return ofy().get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Song lookUpSong(Key<Song> key){
        try {
            return ofy().get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Key<Song>> addSongBatch(List<Song> songBatch) {
        if(songBatch.size() == 0){
            return Collections.emptyList();
        }else{
            return ofy().put(songBatch);
        }
    }
}
