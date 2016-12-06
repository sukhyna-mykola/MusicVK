package com.sukhyna_mykola.musicvk;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by mikola on 24.10.2016.
 */

public class Sound implements Serializable {

    int duration;
    String url;
    String title;
    String artist;
    int id;
    int owner;
    String size;
    File file;
    int id_lyriks;
    String genre;
    String durationString;


    public String getDurationString() {
        return durationString;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getOwner() {
        return owner;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    public int getId_lyriks() {
        return id_lyriks;
    }


    public String getGenre() {
        return genre;
    }


    public Sound(VKApiAudio apiAudio) {
        this.duration = apiAudio.duration * 1000;
        this.durationString = Constants.getTimeString(duration);
        this.url = apiAudio.url;
        this.title = apiAudio.title;
        this.artist = apiAudio.artist;
        this.id = apiAudio.id;
        this.owner = apiAudio.owner_id;
        this.id_lyriks = apiAudio.lyrics_id;
        if(Constants.genre.containsKey(apiAudio.genre))
        this.genre = Constants.genre.get(apiAudio.genre);
        else  this.genre = Constants.genre.get(18);

    }


}
