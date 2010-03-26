package com.rt.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.Key;
import com.rt.dto.DataMapper;
import com.rt.indexing.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DataLoader {

    private ArtistNode loadIndexHierarchy(String prefix) {
        String cpResource = "indexes/hierarcy-index-"+prefix+".ser";
         URL resource = Thread.currentThread().getContextClassLoader().getResource(cpResource);
        try {
            DataMapper d = new DataMapper();
            return (ArtistNode) d.read(resource.openStream());
        } catch (Exception e) {
            System.out.println("DataLoader.loadIndexHierarchy no file for "+cpResource+" due to "+e.getMessage());
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

//    private void asynchGet(String urlString) {
//        {
//            // TODO: try and get rid of these two heap allocations
//            URL url = new URL(urlString);
//            HTTPRequest request = new HTTPRequest(url, HTTPMethod.GET, FetchOptions.Builder.followRedirects());
//            URLFetchServiceFactory.getURLFetchService().fetchAsync(request);
//        }
//        for (Future<HTTPResponse> futureResponse : responses) {
//            parseResponse(new String(futureResponse.get().getContent()));
//        }
//    }

//    public InsertResult loadInBatches(RhymeDao dao) {
//        boolean shouldContinue = true;
//        int count = 0;
//        while(shouldContinue){
//            List<ArtistNode> list = loadIndexHierarchy("" + count++);
//            if(list == null){
//                shouldContinue=false;
//                break;
//            }
//            if(!dao.containsArtist(((AlbumNode)MapUtils.toJavaList(list.get(0).children()).get(0)).artist())){
//                System.out.println("DataLoader.loadInBatches loading from "+count);
//                return load(dao, list);
//            }
//        }
//        System.out.println("DataLoader.loadInBatches nothing to insert, count is "+count);
//        return new InsertResult(0,0,0);
//    }

    public InsertResult load(RhymeDao dao) {
        //return loadInBatches(dao);
        //return load(dao, loadIndexHierarchy());
        return null;
    }

    public void loadArtist(RhymeDao dao, String artistName) {
        System.out.println("DataLoader.loadFromFime loading from artistName: "+artistName);
        if(!dao.containsArtist(artistName)){
            load(dao, loadIndexHierarchy(artistName));
        }else{
            System.out.println("DataLoader.loadFromFime datastore already contains "+artistName);
        }
    }

    public static class AlbumHolder {
        private final Album album;
        private final LinkedHashMap<Song, List<RhymeLeaf>> songs;

        public AlbumHolder(Album album) {
            this.album = album;
            this.songs = new LinkedHashMap<Song, List<RhymeLeaf>>();
        }

        public void addSong(Song song, List<RhymeLeaf> rhymes) {
            songs.put(song, rhymes);
        }

        public Album getAlbum() {
            return album;
        }

        public LinkedHashMap<Song, List<RhymeLeaf>> getSongs() {
            return songs;
        }
    }

    public static class InsertResult {
        private final int rhymes;
        private final int songs;
        private final int albums;

        private InsertResult(int rhymes, int songs, int albums) {
            this.rhymes = rhymes;
            this.songs = songs;
            this.albums = albums;
        }

        public int getRhymes() {
            return rhymes;
        }

        public int getSongs() {
            return songs;
        }

        public int getAlbums() {
            return albums;
        }
    }

    private InsertResult insertAll(List<AlbumHolder> albumHolders, RhymeDao dao) {
        System.out.println("DataLoader.insertAll started at " + System.currentTimeMillis());
        Multimap<String, RhymeData> allRhymes = ArrayListMultimap.create();
        int songCount = 0;

        for (AlbumHolder albumHolder : albumHolders) {
            Album album = dao.addAlbum(albumHolder.getAlbum().getTitle(), albumHolder.getAlbum().getArtist(), albumHolder.album.getYear());
            List<Song> songBatch = new ArrayList<Song>(albumHolder.getSongs().keySet());
            songCount += songBatch.size();
            setAlbumOnSongBatch(songBatch, album);
            List<Key<Song>> songKeys = dao.addSongBatch(songBatch);
            ArrayList<List<RhymeLeaf>> rhymes = new ArrayList<List<RhymeLeaf>>(albumHolder.getSongs().values());
            allRhymes.putAll(setSongsOnRhymeBatches(rhymes, songKeys));
            //System.out.println("DataLoader.insertAll adding rhymes started at " + System.currentTimeMillis());
            //Multimap<String, RhymeData> rhymeDatas = setSongsOnRhymeBatches(rhymes, songKeys);
            //rhymeCount += rhymeDatas.entries().size();
            //dao.addRhymes(makeRhymedata(rhymeDatas));
           // System.out.println("DataLoader.insertAll adding rhymes ended at " + System.currentTimeMillis());
        }

        System.out.println("DataLoader.insertAll adding rhymes started at " + System.currentTimeMillis());
        MakeRhymeDataResult rhymeDataResult = makeRhymedata(allRhymes);
        dao.addRhymes(rhymeDataResult.rhymes);
        System.out.println("DataLoader.insertAll adding rhymes ended at " + System.currentTimeMillis());
        return new InsertResult(rhymeDataResult.rhymesFound, songCount, albumHolders.size());
    }


    private static class MakeRhymeDataResult {
        public final List<Rhyme> rhymes;
        public final int rhymesFound;

        private MakeRhymeDataResult(List<Rhyme> rhymes, int rhymesFound) {
            this.rhymes = rhymes;
            this.rhymesFound = rhymesFound;
        }
    }

    private MakeRhymeDataResult makeRhymedata(Multimap<String, RhymeData> rhymeMap) {
        List<Rhyme> res = new ArrayList<Rhyme>();
        int rhymesFound = 0;
        for (String key : rhymeMap.keySet()) {
            rhymesFound += rhymeMap.get(key).size();
            res.add(new Rhyme(key, new ArrayList<RhymeData>(rhymeMap.get(key))));
        }
        return new MakeRhymeDataResult(res, rhymesFound);
    }

    // a song with no rhymes must return an emtpy list
    private Multimap<String, RhymeData> setSongsOnRhymeBatches(ArrayList<List<RhymeLeaf>> rhymeBatch, List<Key<Song>> songs) {
        Iterator<List<RhymeLeaf>> rhmyeIter = rhymeBatch.iterator();
        Iterator<Key<Song>> songIter = songs.iterator();
        Multimap<String, RhymeData> rhymes = ArrayListMultimap.create();

        while (rhmyeIter.hasNext() && songIter.hasNext()) {
            Key<Song> songKey = songIter.next();
            List<RhymeLeaf> rhymeLeafList = rhmyeIter.next();
            rhymes.putAll(setSongOnRhymeBatch(rhymeLeafList, songKey));
        }
        return rhymes;
    }

    public void deleteAll() {
        new RhymeDao().deleteAll();
    }

    private Multimap<String, RhymeData> setSongOnRhymeBatch(List<RhymeLeaf> rhymeBatch, Key<Song> song) {
        //System.out.println("DataLoader.setSongOnRhymeBatch setting song key " + song);
        Multimap<String, RhymeData> res = ArrayListMultimap.create();
        for (RhymeLeaf r : rhymeBatch) {
            res.get(r.word()).add(new RhymeData(song, toJList(r.lines(), String.class)));
        }
        return res;
    }

    private void setAlbumOnSongBatch(List<Song> songBatch, Album album) {
        for (Song s : songBatch) {
            s.setAlbum(new Key<Album>(Album.class, album.getId()));
        }
    }

    private void load(RhymeDao dao, Map<String, ArtistNode> artistNodes) {
        for (ArtistNode artist : artistNodes.values()) {
            load(dao, artist);
        }
    }

    private void load(RhymeDao dao, ArtistNode artist) {
        System.out.println("DataLoader.load started at " + System.currentTimeMillis());
        //List<Album> albums = new ArrayList<Album>();
        //List<Song> songs = new ArrayList<Song>();
        List<AlbumHolder> albumHolders = new ArrayList<AlbumHolder>();
            List<AlbumNode> albumNodes = toJList(artist.children());
            for (AlbumNode albumNode : albumNodes) {
                //System.out.println("DataLoader.load node: "+ albumNode);
                Album album = new Album(albumNode.title(), albumNode.artist(), albumNode.year());
                AlbumHolder albumHolder = new AlbumHolder(album);
                albumHolders.add(albumHolder);
                //albums.add(album);
                //Album albumData = dao.addAlbum(albumNode.title(), albumNode.artist(), albumNode.year());
                List<SongNode> songNodes = toJList(albumNode.children());
                for (SongNode s : songNodes) {
                    //Song song = dao.addSong(s.title(), s.trackNo(), "", new Key<Album>(Album.class, albumData.getId()));
                    //Song song = dao.addSong(s.title(), s.trackNo(), "",null);
                    Song song = new Song(s.title(), s.trackNo(), "", null);
                    List<RhymeLeaf> rhymeLeaves = toJList(s.rhymes());
                    albumHolder.addSong(song, rhymeLeaves);
                }
            }
        
        insertAll(albumHolders, dao);
    }


    private <K,V> Map<K,V> toJMap(scala.collection.immutable.Map scalaMap) {
        return (Map<K,V>) MapUtils.toJavaMap(scalaMap);
    }
    
     private <K,V> Map<K,V> toJMap(scala.collection.immutable.Map scalaMap, Class<K> kClass, Class<V> vClass) {
        return (Map<K,V>) MapUtils.toJavaMap(scalaMap);
    }

    private <T> List<T> toJList(scala.List scalaList) {
        return (List<T>) MapUtils.toJavaList(scalaList);
    }

    private <T> List<T> toJList(scala.List scalaList, Class<T> clazz) {
        return (List<T>) MapUtils.toJavaList(scalaList);
    }
}

