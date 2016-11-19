package com.sukhyna_mykola.musicvk;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundLab {
    public static SoundLab sSoundLab;
    public static User mUser;

    private List<Sound> mSounds;
    private List<Sound> mCurentPlayList;

    public static SoundLab get() {
        if (sSoundLab == null)
            sSoundLab = new SoundLab();
        return sSoundLab;
    }

    private SoundLab() {
        mSounds = new ArrayList<>();
    }

    public List<Sound> getSounds() {
        return mSounds;
    }

    public void addAllSound(ArrayList<Sound> newSounds) {
        mSounds.clear();
        mSounds.addAll(newSounds);
    }

    public Sound getSound(int id) {
        for (Sound sound : mCurentPlayList) {
            if (sound.id == id)
                return sound;
        }
        return null;
    }

    public List<Sound> getCurentPlayList() {
        return mCurentPlayList;
    }

    public void setCurentPlayList(List<Sound> curentPlayList) {
        mCurentPlayList = curentPlayList;
    }


    void setPlayList() {
        mCurentPlayList = new ArrayList<>();

        for (Sound clone : mSounds) {
            mCurentPlayList.add(clone);
        }

    }


    public boolean containtSound(int idSound) {

        for (int i = 0; i < mCurentPlayList.size(); i++) {
            if (mCurentPlayList.get(i).getId() == idSound)
                return true;
        }

        return false;
    }

    public static void saveObject() {
        if (!mUser.isTmpUser())
            try {

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                        new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ MainActivity.USER_FOLDER + mUser.getId()))); //Select where you wish to save the file...
                oos.writeObject(mUser); // write the class as an 'object'
                oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
                oos.close();// close the stream
            } catch (Exception ex) {

            }
    }

}