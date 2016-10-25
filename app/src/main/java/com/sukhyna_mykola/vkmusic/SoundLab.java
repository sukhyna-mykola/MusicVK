package com.sukhyna_mykola.vkmusic;

import android.content.Context;
import android.widget.ListView;

import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundLab {
    public static SoundLab sSoundLab;
    private List<Sound> mSounds;
    private int curentPosition;

    public int getCurentPosition() {
        return curentPosition;
    }

    public void setCurentPosition(int curentPosition) {
        this.curentPosition = curentPosition;
    }

    public static SoundLab get(Context context) {
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
    public void addAllSound(ArrayList<Sound> newSounds){
        mSounds.clear();
        mSounds.addAll(newSounds);
    }

}
