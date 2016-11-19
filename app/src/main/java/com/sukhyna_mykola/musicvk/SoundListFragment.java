package com.sukhyna_mykola.musicvk;

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
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import static com.sukhyna_mykola.musicvk.LikeService.DOWNLOADED;
import static com.sukhyna_mykola.musicvk.MusicService.BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.FINISH;
import static com.sukhyna_mykola.musicvk.MusicService.INIT;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_NULL;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PLAY;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PLAY_PAUSE;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PLAY_SOUND_POSITION;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_POS;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PROGRESS;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_TYPE;
import static com.sukhyna_mykola.musicvk.MusicService.UPDATING;

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundListFragment extends Fragment {
    private static final String TAG = "SoundListFragment";
    public static final String ID_SOUND = "ID_SOUND";

    private RecyclerView mSoundRecyclerView;
    private FloatingActionButton fab;

    private ListAdapter mAdapter;
    private Intent musicIntent;
    private BroadcastReceiver br;

    private boolean isPlay;
    private int progres;
    private int curentID = -1;

    private boolean buffer;


    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(MusicService.PARAM_TYPE, -1);
                if (type == UPDATING) {
                    isPlay = intent.getBooleanExtra(MusicService.PARAM_PLAY, false);
                    progres = intent.getIntExtra(MusicService.PARAM_PROGRESS, progres);
                    update();
                } else if (type == INIT) {
                    curentID = intent.getIntExtra(MusicService.PARAM_POS, curentID);
                    update();
                } else if (type == FINISH) {
                    fab.setVisibility(View.GONE);
                    curentID = -1;
                    isPlay = false;
                    update();
                } else if (type == BUFFERING) {
                    buffer = intent.getBooleanExtra(PARAM_BUFFERING, true);
                } else if (type == DOWNLOADED) {
                    update();
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
                intent.putExtra(ID_SOUND, curentID);
                intent.putExtra(PARAM_PLAY, isPlay);
                intent.putExtra(PARAM_PROGRESS, progres);
                intent.putExtra(PARAM_BUFFERING, buffer);
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
        CardView mContainer;

        private Sound mSound;

        public SoundHolder(final View itemView) {
            super(itemView);

            mContainer = (CardView) itemView.findViewById(R.id.item_container);
            mImageButtonPlayPause = (ImageButton) itemView.findViewById(R.id.play_pause_item_button);
            mDownload = (ImageButton) itemView.findViewById(R.id.download_item_button);
            mTitle = (TextView) itemView.findViewById(R.id.title_item_text);
            mArtist = (TextView) itemView.findViewById(R.id.artist_item_text);
            mDuration = (TextView) itemView.findViewById(R.id.duration_item_text);

            mContainer.setOnClickListener(this);
            mImageButtonPlayPause.setOnClickListener(this);

            mDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DownloadSound(getActivity(), mSound);
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

                    if (!SoundLab.get().containtSound(mSound.getId())) {
                        SoundLab.get().setPlayList();
                    }
                    Intent intent = sendActionToService(PARAM_PLAY_SOUND_POSITION);
                    intent.putExtra(PARAM_POS, curentID);
                    getActivity().sendBroadcast(intent);
                } else {
                    if (!SoundLab.get().getCurentPlayList().equals(SoundLab.get().getSounds())) {
                        SoundLab.get().setPlayList();
                    }

                    if (!isPlay) {
                        mImageButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        Intent intent = sendActionToService(PARAM_PLAY_PAUSE);
                        getActivity().sendBroadcast(intent);
                    } else {

                        mImageButtonPlayPause.setImageResource(R.drawable.ic_pause_white_24dp);
                        Intent intent = sendActionToService(PARAM_PLAY_PAUSE);
                        getActivity().sendBroadcast(intent);
                    }

                }
            } else {
                startMusicService();
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
            holder.mDuration.setText(sound.durationString);

            if (SoundLab.mUser.containtSoundDown(sound.getId()))
                holder.mDownload.setImageResource(R.drawable.ic_file_download_complete_black_24dp);
            else
                holder.mDownload.setImageResource(R.drawable.ic_file_download_black_24dp);

            if (sound.id == curentID) {
                //  holder.mImageButtonPlayPause.setVisibility(View.VISIBLE);
                holder.mContainer.setCardBackgroundColor(getResources().getColor(R.color.vk_light_color));
                if (isPlay) {
                    holder.mImageButtonPlayPause.setImageResource(R.drawable.ic_pause_white_24dp);
                } else
                    holder.mImageButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            } else {
                holder.mImageButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                //  holder.mImageButtonPlayPause.setVisibility(View.GONE);
                holder.mContainer.setCardBackgroundColor(Color.WHITE);
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
