package com.sukhyna_mykola.vkmusic;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.vk.sdk.api.model.VKApiAudio;

import java.util.List;
import java.util.UUID;

import static com.sukhyna_mykola.vkmusic.MusicService.INIT;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_NULL;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_PLAY_PAUSE;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_PLAY_SOUND_POSITION;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_POS;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_TYPE;
import static com.sukhyna_mykola.vkmusic.MusicService.UPDATING;

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundListFragment extends Fragment {
    private static final String TAG = "TAG";
    private RecyclerView mSoundRecyclerView;
    private ListAdapter mAdapter;
    Intent musicIntent;
    BroadcastReceiver br;

    public static final String IDSOUND = "idSound";
    private FloatingActionButton fab;
    private boolean isPlay;
    private int curentID = -1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(MusicService.PARAM_TYPE, -1);
                if (type == UPDATING) {
                    isPlay = intent.getBooleanExtra(MusicService.PARAM_PLAY, false);
                    update();
                }
                if (type == INIT) {

                    curentID = intent.getIntExtra(MusicService.PARAM_POS, curentID);

                }
            }
        };
        IntentFilter intFilt = new IntentFilter(MusicService.DATA_FROM_SERVICE);
        getActivity().registerReceiver(br, intFilt);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sound_list_fragment, container, false);
        mSoundRecyclerView = (RecyclerView) v.findViewById(R.id.sound_recycler_view);
        mSoundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(IDSOUND, curentID);
                startActivity(intent);
            }
        });
        //якщо сервіс запущений отримати id поточної композиціїї
        if (isMyServiceRunning(MusicService.class)) {
            Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
            intent.putExtra(PARAM_TYPE, PARAM_NULL);
            getActivity().sendBroadcast(intent);
            fab.setVisibility(View.VISIBLE);
        }
        updateUI();

        return v;
    }

    private void updateUI() {
        SoundLab soundLab = SoundLab.get();
        List<Sound> sounds = soundLab.getSounds();
        mAdapter = new ListAdapter(sounds);
        mSoundRecyclerView.setAdapter(mAdapter);
    }

    public void update() {
        mAdapter.notifyDataSetChanged();
    }


    private class SoundHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton mImageButtonPlayPause;
        ImageButton mDownload;
        TextView mTitle;
        TextView mArtist;
        TextView mDuration;
        TextView mSize;
        RelativeLayout mContainer;

        private Sound mSound;

        public SoundHolder(final View itemView) {
            super(itemView);
            mContainer = (RelativeLayout) itemView.findViewById(R.id.item_container);
            mImageButtonPlayPause = (ImageButton) itemView.findViewById(R.id.play_pause_item_button);
            mDownload = (ImageButton) itemView.findViewById(R.id.download_item_button);
            mTitle = (TextView) itemView.findViewById(R.id.title_item_text);
            mArtist = (TextView) itemView.findViewById(R.id.artist_item_text);
            mDuration = (TextView) itemView.findViewById(R.id.duration_item_text);
            mSize = (TextView) itemView.findViewById(R.id.size_item_text);

            mContainer.setOnClickListener(this);
            itemView.setOnClickListener(this);
            mImageButtonPlayPause.setOnClickListener(this);

            mDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentDownload = new Intent(getActivity(), DownloadService.class);
                    intentDownload.putExtra(DownloadService.TITLE_DOWNLOAD, mSound.getTitle() + "-" + mSound.getArtist());
                    intentDownload.putExtra(DownloadService.URL_DOWNLOAD, mSound.getUrl());
                    getActivity().startService(intentDownload);
                }
            });

        }


        private Intent sendActionToService(String type) {
            Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
            intent.putExtra(MusicService.PARAM_TYPE, type);
            return intent;
        }

        private void startMusicService() {
            curentID = mSound.getId();
            musicIntent = new Intent(getActivity(), MusicService.class);
            musicIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            musicIntent.putExtra(PARAM_POS, mSound.getId());
            SoundLab.get().setPlayList();
            getActivity().startService(musicIntent);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void bindSound(Sound sound) {
            mSound = sound;
        }

        @Override
        public void onClick(View v) {

            if (isMyServiceRunning(MusicService.class)) {
                if (curentID != mSound.getId()) {
                    curentID = mSound.getId();

                    if(!SoundLab.get().getCurentPlayList().contains(mSound))
                        SoundLab.get().setPlayList();
                    Intent intent = sendActionToService(PARAM_PLAY_SOUND_POSITION);
                    intent.putExtra(PARAM_POS, curentID);
                    getActivity().sendBroadcast(intent);
                } else {

                    if (!isPlay) {
                        mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        Intent intent = sendActionToService(PARAM_PLAY_PAUSE);
                        getActivity().sendBroadcast(intent);
                    } else {
                        mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        Intent intent = sendActionToService(PARAM_PLAY_PAUSE);
                        getActivity().sendBroadcast(intent);
                    }

                }
            } else {
                startMusicService();
                mContainer.setBackgroundColor(getResources().getColor(R.color.vk_light_color));
                fab.setVisibility(View.VISIBLE);
            }
        }

    }

    private class ListAdapter extends RecyclerView.Adapter<SoundHolder> {
        private List<Sound> mSounds;

        public ListAdapter(List<Sound> sounds) {
            mSounds = sounds;
        }

        @Override
        public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_sounds_item, parent, false);
            return new SoundHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(SoundHolder holder, int position) {
            Sound sound = mSounds.get(position);
            holder.bindSound(sound);

            holder.mTitle.setText(sound.title);
            holder.mArtist.setText(sound.artist);
            holder.mDuration.setText(Constants.getTimeString(sound.getDuration()));
            if (sound.getSize() != null)
                holder.mSize.setText(sound.getSize() + "Mb.");
            else holder.mSize.setText("...");


            if (sound.isUsing()) {
                holder.mContainer.setBackgroundColor(getResources().getColor(R.color.vk_light_color));
                if (isPlay) {
                    holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else
                    holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
                holder.mContainer.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return mSounds.size();
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(br);
    }
}
