package com.sukhyna_mykola.vkmusic;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.UUID;

import static android.R.attr.id;
import static com.sukhyna_mykola.vkmusic.PlayerActivity.ARG_ID_SOUND;


/**
 * Created by mikola on 25.10.2016.
 */

public class PlayerFragment extends Fragment {
    ImageButton playBtn;
    Button hideBtn;
    TextView nameMusic;
    TextView timeMusic;
    SeekBar progresMusic;

    int  id;

    public static final String ARG_ID_SOUND = "com.sukhyna_mykola.vkmusic.ARG_ID_SOUND";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id =  getArguments().getInt(ARG_ID_SOUND);
        }

    static PlayerFragment newInstance(int id) {
        PlayerFragment pageFragment = new PlayerFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_ID_SOUND, id);
        pageFragment.setArguments(arguments);
        return pageFragment;

    }
  /*  private void startMusicService() {

        for (Sound sound:SoundLab.get(getActivity()).getSounds()) {
            if(!sound.id.equals(mSound.getId())){
                sound.setState(false);
            }
        }
        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        musicIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        musicIntent.putExtra(PARAM_POS, mSound.getId());
        Log.d(TAG, "mSound.getId() "+mSound.getId());
        getActivity().startService(musicIntent);
    }



    @Nullable*/
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.player_fragment, container, false);
        Sound sound = SoundLab.get(getActivity()).getSound(id);
        ( (TextView)v.findViewById(R.id.title_sound_player)).setText(sound.getTitle());
        ( (TextView)v.findViewById(R.id.artist_sound_player)).setText(sound.getArtist());
        return v;
    }


/*
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Player");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause Player");
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


*/
}
