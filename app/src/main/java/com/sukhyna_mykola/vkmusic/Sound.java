package com.sukhyna_mykola.vkmusic;

/**
 * Created by mikola on 23.10.2016.
 */

public class Sound  {
    private String mTitle;
    private String mArtist;
    private String mURL;
    private int mDuration;

    public Sound(String title, String artist, String URL, int duration) {
        mTitle = title;
        mArtist = artist;
        mURL = URL;
        mDuration = duration;
    }

    public String getTitle() {

        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getURL() {
        return mURL;
    }

    public int getDuration() {
        return mDuration;
    }
}
