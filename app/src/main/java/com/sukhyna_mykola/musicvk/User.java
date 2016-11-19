package com.sukhyna_mykola.musicvk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikola on 05.11.2016.
 */

public class User implements Serializable {

    List<Integer> downloadedSounds;
    List<Sound> favoritesSounds;
    List<Sound> myMusic;

    byte[] photo;
    String name;
    int id;
    boolean tmpUser;

    public boolean isTmpUser() {
        return tmpUser;
    }



    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(byte[] photo, String name, int id) {

        this.photo = photo;
        this.name = name;
        this.id = id;
        downloadedSounds = new ArrayList<>();
        favoritesSounds = new ArrayList<>();
        myMusic = new ArrayList<>();

    }
    public User() {
        tmpUser = true;
        downloadedSounds = new ArrayList<>();
        favoritesSounds = new ArrayList<>();
        myMusic = new ArrayList<>();

    }
    public void addDownloadedDound(int id) {
        downloadedSounds.add(id);
    }

    public void addFavotitesSound(Sound newSound) {
        favoritesSounds.add(newSound);
    }

    public void addMyMusic(Sound newSound) {
        myMusic.add(newSound);
    }

    public List<Integer> getDownloadedSounds() {
        return downloadedSounds;
    }

    public void removeSoundFromFavorites(int idSound) {
        int indexRemove = -1;
        for (int i = 0; i < favoritesSounds.size(); i++) {
            if (favoritesSounds.get(i).getId() == idSound) ;
            indexRemove = i;
            break;
        }
        if (indexRemove != -1)
            favoritesSounds.remove(indexRemove);
    }

    public boolean containtSoundDown(int idSound) {
        for (Integer id : downloadedSounds) {
            if (id.intValue() == idSound)
                return true;

        }
        return false;
    }

    public boolean containtSoundFavorite(int idSound) {

        for (int i = 0; i < favoritesSounds.size(); i++) {
            if (favoritesSounds.get(i).getId() == idSound)
                return true;
        }

        return false;
    }
    public boolean containtMyMusic(int idSound) {
        for (int i = 0; i < myMusic.size(); i++) {
            if (myMusic.get(i).getId() == idSound)
                return true;
        }
        return false;
    }

    public Sound getSoundFavorite(int idSound) {
        for (int i = 0; i < favoritesSounds.size(); i++) {
            if (favoritesSounds.get(i).getId() == idSound)
                return favoritesSounds.get(i);
        }

        return null;
    }
    public  void clearHistoryDownload(){
        downloadedSounds.clear();
        downloadedSounds = new ArrayList<>();
    }

}
