package com.sukhyna_mykola.vkmusic;

import com.vk.sdk.api.model.VKApiAudio;

/**
 * Created by mikola on 24.10.2016.
 */

public class Sound {

    boolean state;
    String duration;
    String url;
    String title;
    String artist;


    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Sound(boolean state, String duration, String url, String title, String artist) {

        this.state = state;
        this.duration = duration;
        this.url = url;
        this.title = title;
        this.artist = artist;
    }

    public String getDuration() {

        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
