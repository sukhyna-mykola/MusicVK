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
    private  List<Sound> mSounds;

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
    public  Sound getSound(UUID id){
        for (Sound sound:mSounds) {
           if(sound.id.equals(id))
               return sound;
        }
        return null;
    }

}
