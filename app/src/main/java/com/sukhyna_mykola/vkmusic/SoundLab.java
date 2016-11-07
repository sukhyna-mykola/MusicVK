package com.sukhyna_mykola.vkmusic;

import android.content.Context;
import android.widget.ListView;

import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundLab {
    public static SoundLab sSoundLab;
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

        for (Sound clone:mSounds) {
            mCurentPlayList.add(clone);
        }

    }

}
