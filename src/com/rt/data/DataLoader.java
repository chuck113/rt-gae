package com.rt.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.Key;
import com.rt.dto.DataMapper;
import com.rt.indexing.*;

import static com.rt.CollectionsUtils.*;

import java.net.URL;
import java.util.*;

public class DataLoader {

    /**
     * Loads the list of all the rhyme parts founds while creating rhymes. Used to build
     * links from lines to other lines.
     *
     * @return
     */
    private List<String> getAllKnownRhymes() {
        String cpResource = "indexes/allparts.ser";
        URL resource = Thread.currentThread().getContextClassLoader().getResource(cpResource);
        try {
            DataMapper d = new DataMapper();
            return toJList((scala.List) d.read(resource.openStream()), String.class);
        } catch (Exception e) {
            System.out.println("DataLoader.loadIndexHierarchy no file for " + cpResource + " due to " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private AlbumNode loadIndexHierarchy(String prefix) {
        String cpResource = "indexes/hierarcy-index-" + prefix + ".ser";
        URL resource = Thread.currentThread().getContextClassLoader().getResource(cpResource);
        try {
            DataMapper d = new DataMapper();
            return (AlbumNode) d.read(resource.openStream());
        } catch (Exception e) {
            System.out.println("DataLoader.loadIndexHierarchy no file for " + cpResource + " due to " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public void loadAlbumFile(RhymeDao dao, String albumName) {
        System.out.println("DataLoader.loadFromFime loading from albumName: " + albumName);
        //if(!dao.containsArtist(albumName)){
        load(dao, loadIndexHierarchy(albumName), getAllKnownRhymes());
        //}else{
        //    System.out.println("DataLoader.loadFromFime datastore already contains "+ albumName);
        //}
    }

    public static class AlbumHolder {
        private final Album album;
        private final LinkedHashMap<Song, List<RhymeLeaf>> songs;

        public AlbumHolder(String title, String artist, int year) {
            this(new Album(title, artist, year));
        }


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

    private InsertResult insertAlbum(AlbumHolder albumHolder, RhymeDao dao, List<String> allRhymesParts) {
        //System.out.println("DataLoader.insertAlbum started at " + System.currentTimeMillis());
        Multimap<String, RhymePartData> allRhymes = ArrayListMultimap.create();
        int songCount = 0;

        Album album = dao.addAlbum(albumHolder.getAlbum().getTitle(), albumHolder.getAlbum().getArtist(), albumHolder.album.getYear());
        List<Song> songBatch = new ArrayList<Song>(albumHolder.getSongs().keySet());
        songCount += songBatch.size();
        setAlbumOnSongBatch(songBatch, album);
        List<Key<Song>> songKeys = dao.addSongBatch(songBatch);
        List<List<RhymeLeaf>> rhymes = Lists.newArrayList(albumHolder.getSongs().values());
        allRhymes.putAll(setSongsOnRhymeBatches(rhymes, songKeys, allRhymesParts));

        MakeRhymeDataResult rhymeDataResult = makeRhymedata(allRhymes);
        dao.addRhymes(rhymeDataResult.rhymes);
        //System.out.println("DataLoader.insertAlbum adding rhymes ended at " + System.currentTimeMillis());
        return new InsertResult(rhymeDataResult.rhymesFound, songCount, 1);
    }


    private static class MakeRhymeDataResult {
        public final List<Rhyme> rhymes;
        public final int rhymesFound;

        private MakeRhymeDataResult(List<Rhyme> rhymes, int rhymesFound) {
            this.rhymes = rhymes;
            this.rhymesFound = rhymesFound;
        }
    }


    private MakeRhymeDataResult makeRhymedata(Multimap<String, RhymePartData> rhymeMap) {
        List<Rhyme> res = new ArrayList<Rhyme>();
        int rhymesFound = 0;
        for (String key : rhymeMap.keySet()) {
            rhymesFound += rhymeMap.get(key).size();
            res.add(Rhyme.createRhyme(key, new ArrayList<RhymePartData>(rhymeMap.get(key))));
        }
        return new MakeRhymeDataResult(res, rhymesFound);
    }

    // a song with no rhymes must return an emtpy list

    private Multimap<String, RhymePartData> setSongsOnRhymeBatches(List<List<RhymeLeaf>> rhymeBatch, List<Key<Song>> songs, List<String> allRhymesParts) {
        Iterator<List<RhymeLeaf>> rhmyeIter = rhymeBatch.iterator();
        Iterator<Key<Song>> songIter = songs.iterator();
        Multimap<String, RhymePartData> rhymes = ArrayListMultimap.create();

        while (rhmyeIter.hasNext() && songIter.hasNext()) {
            Key<Song> songKey = songIter.next();
            List<RhymeLeaf> rhymeLeafList = rhmyeIter.next();
            rhymes.putAll(setSongOnRhymeBatch(rhymeLeafList, songKey, allRhymesParts));
        }
        return rhymes;
    }

    public void deleteAll() {
        new RhymeDao().deleteAll();
    }

    private Multimap<String, RhymePartData> setSongOnRhymeBatch(List<RhymeLeaf> rhymeBatch, Key<Song> song, List<String> allRhymesParts) {
        //System.out.println("DataLoader.setSongOnRhymeBatch setting song key " + song);
        Multimap<String, RhymePartData> res = ArrayListMultimap.create();
        for (RhymeLeaf r : rhymeBatch) {
            res.get(r.word()).add(RhymePartData.createRhymeData(song, r, allRhymesParts));
        }
        return res;
    }

    private void setAlbumOnSongBatch(List<Song> songBatch, Album album) {
        for (Song s : songBatch) {
            s.setAlbum(new Key<Album>(Album.class, album.getId()));
        }
    }

    private void load(RhymeDao dao, AlbumNode albumNode, List<String> allRhymeParts) {
        AlbumHolder albumHolder = new AlbumHolder(albumNode.title(), albumNode.artist(), albumNode.year());
        List<SongNode> songNodes = toJList(albumNode.children());
        for (SongNode s : songNodes) {
            albumHolder.addSong(new Song(s.title(), s.trackNo(), "", null), toJList(s.rhymes(), RhymeLeaf.class));
        }

        insertAlbum(albumHolder, dao, allRhymeParts);
    }
}

