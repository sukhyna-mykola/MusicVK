package com.sukhyna_mykola.musicvk;
import com.vk.sdk.api.model.VKApiAudio;

import java.io.File;
import java.io.Serializable;

/**
 * Created by mikola on 24.10.2016.
 */

public class Sound implements Serializable{

    int duration;
    String url;
    String title;
    String artist;
    int id;
    int  owner;
    String size;
    File file;

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

    public Sound(VKApiAudio apiAudio) {
        this.duration = apiAudio.duration*1000;
        this.url = apiAudio.url;
        this.title = apiAudio.title;
        this.artist = apiAudio.artist;
        this.id = apiAudio.id;
        this.owner = apiAudio.owner_id;
    }


}
