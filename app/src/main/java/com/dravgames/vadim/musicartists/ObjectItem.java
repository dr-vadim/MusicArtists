package com.dravgames.vadim.musicartists;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Vadim on 06.04.2016.
 */
public class ObjectItem {

    private String title, description;
    private JSONObject images;
    private List<String> genres;
    private int albums, tracks;

    public ObjectItem(String ttl, String descr, JSONObject images, List<String> genres, int albums, int tracks){
        this.title = ttl;
        this.description = descr;
        this.images = images;
        this.genres = genres;
        this.albums = albums;
        this.tracks = tracks;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String ttl){
        title = ttl;
    }

    public String getGenres(){
        return TextUtils.join(",",genres);
    }

    public String getGenres(String delimeter){
        return TextUtils.join(delimeter,genres);
    }

    public void setGenres(List<String> genres){
        this.genres = genres;
    }

    public int getAlbums(){
        return albums;
    }

    public void setAlbums(int albums){
        this.albums = albums;
    }

    public int getTracks(){
        return tracks;
    }

    public void setTracks(int songs){
        this.tracks = songs;
    }

    public String getImage() throws JSONException {
        return images.getString("small");
    }

    public String getImage(String name) throws JSONException{
        return images.getString(name);
    }

    public void setImage(JSONObject images){
        this.images = images;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String descr){
        this.description = descr;
    }
}
