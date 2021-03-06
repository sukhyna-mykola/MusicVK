package com.sukhyna_mykola.musicvk;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;


/**
 * Created by mikola on 25.10.2016.
 */

public class PlayerFragment extends Fragment {

    int id;

    public static final String ARG_ID_SOUND = "com.sukhyna_mykola.vkmusic.ARG_ID_SOUND";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments().getInt(ARG_ID_SOUND);
    }

    static PlayerFragment newInstance(int id) {
        PlayerFragment pageFragment = new PlayerFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_ID_SOUND, id);
        pageFragment.setArguments(arguments);
        return pageFragment;

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_fragment, container, false);
        Sound sound = SoundLab.get().getSound(id);
        ((TextView) v.findViewById(R.id.title_sound_player)).setText(sound.getTitle());
        ((TextView) v.findViewById(R.id.artist_sound_player)).setText(sound.getArtist());
        ImageView imageView = (ImageView) v.findViewById(R.id.music_note);

        if (SoundLab.get().albumArts.containsKey(sound.getUrl()))
            if (SoundLab.get().albumArts.get(sound.getUrl()) != null)
                imageView.setImageBitmap(SoundLab.get().albumArts.get(sound.getUrl()));

        return v;
    }


}