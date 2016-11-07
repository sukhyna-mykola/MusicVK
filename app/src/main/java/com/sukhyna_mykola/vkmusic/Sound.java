package com.sukhyna_mykola.vkmusic;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by mikola on 24.10.2016.
 */

public class Sound implements Serializable{
    String size;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    boolean using;
    boolean playing;

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    int duration;
    String url;
    String title;
    String artist;
    int id;


    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public String getUrl() {
        return url;
    }
    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }
    public int getDuration() {
        return duration;
    }

    public int getId() {
        return id;
    }

    public Sound(int duration, String url, String title, String artist,int id) {
        this.duration = duration;
        this.url = url;
        this.title = title;
        this.artist = artist;
        this.id = id;
    }
}
