package com.example.neteasecloudmusictext.model;

import com.example.neteasecloudmusictext.view.ViewConstants;
import com.example.player.entity.Album;
import com.example.player.entity.Artist;
import com.example.player.entity.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Formatter {
    public static List<Song> songLists(String json){
        int code = 0;
//        int size = curPage* ViewConstants.PAGE_MAX;
//        int limit  = size+20;
        List<Song> songList = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);
            code = root.getInt(ViewConstants.CODE_JSON);
            if(code == ViewConstants.SUCCESS_CODE_JSON){
                JSONObject result = root.getJSONObject(ViewConstants.RESULT_JSON);
                JSONArray songs = result.getJSONArray(ViewConstants.SONG_JSON);
                for (int i = 0; i < songs.length(); i++) {
                    JSONObject song = songs.getJSONObject(i);

                    Song song1 = new Song();

                    //歌曲id和名字的记录
                    song1.setId(song.getLong(ViewConstants.ID_JSON));
                    song1.setName(song.getString(ViewConstants.NAME_JSON));

                    //艺术家的解析与记录
                    JSONArray artistsObject = song.getJSONArray(ViewConstants.ARTIST_JSON);
                    List<Artist> artists = new ArrayList<>();

                    for (int j = 0; j < artistsObject.length(); j++) {
                        Artist artist = new Artist();
                        JSONObject art = artistsObject.getJSONObject(j);
                        artist.setId(art.getLong(ViewConstants.ID_JSON));
                        artist.setName(art.getString(ViewConstants.NAME_JSON));
                        artist.setAlias(art.getString(ViewConstants.ALIAS_JSON));
                        artists.add(artist);
                    }
                    song1.setArtists(artists);

                    //专辑的解析与记录
                    JSONObject albumObject = song.getJSONObject(ViewConstants.ALBUM_JSON);
                    Album album = new Album();
                    album.setId(albumObject.getLong(ViewConstants.ID_JSON));
                    album.setName(albumObject.getString(ViewConstants.NAME_JSON));
                    album.setPicUrl(albumObject.getString(ViewConstants.PIC_URL_JSON));
                    song1.setAlbum(album);
                    songList.add(song1);

                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return songList;
    }

}
