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

/**
 * Created by mikola on 23.10.2016.
 */

public class SoundListFragment extends Fragment {
    private static final String TAG = "TAG";
    private RecyclerView mSoundRecyclerView;
    private ListAdapter mAdapter;
    Intent musicIntent;
    BroadcastReceiver br;
    public final static String PARAM_PLAY = "play_status";
    public final static String PARAM_POS = "play_pos";
    public final static String PARAM_TIME = "play_time";
    public final static String ACTION_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.from_service";
    public final static String TIME_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.TIME_service";
    private boolean isPlay;
    private int curentPos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isPlay = intent.getBooleanExtra(PARAM_PLAY, false);

                curentPos = intent.getIntExtra(PARAM_POS, 0);

                update();
            }
        };
        IntentFilter intFilt = new IntentFilter(ACTION_FROM_SERVICE);
        getActivity().registerReceiver(br, intFilt);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sound_list_fragment, container, false);
        mSoundRecyclerView = (RecyclerView) v.findViewById(R.id.sound_recycler_view);
        mSoundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return v;
    }

    private void updateUI() {
        SoundLab soundLab = SoundLab.get(getActivity());
        List<Sound> sounds = soundLab.getSounds();
        mAdapter = new ListAdapter(sounds);
        mSoundRecyclerView.setAdapter(mAdapter);
    }

    public void update() {
        mAdapter.notifyDataSetChanged();
    }

    private class SoundHolder extends RecyclerView.ViewHolder {
        ImageButton mImageButtonPlayPause;
        ImageButton mMenu;
        TextView mTitle;
        TextView mArtist;
        TextView mDuration;
        RelativeLayout v;
        private int mPos;
        private Sound mSound;

        public SoundHolder(final View itemView) {
            super(itemView);
            v = (RelativeLayout) itemView.findViewById(R.id.item_container);
            mImageButtonPlayPause = (ImageButton) itemView.findViewById(R.id.play_pause_item_button);
            mMenu = (ImageButton) itemView.findViewById(R.id.menu_item_button);
            mTitle = (TextView) itemView.findViewById(R.id.title_item_text);
            mArtist = (TextView) itemView.findViewById(R.id.artist_item_text);
            mDuration = (TextView) itemView.findViewById(R.id.duration_item_text);

            mImageButtonPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (curentPos != mPos) {
                        Log.d(TAG, "curentPos = " + curentPos + " -------- mPos = " + mPos);
                        if (isMyServiceRunning(MusicService.class)) {
                            getActivity().stopService(new Intent(getActivity(), MusicService.class));
                            Log.d(TAG, "stop service");
                        }
                       startMusicService();
                    } else {
                        if (isMyServiceRunning(MusicService.class)) {
                            if (!isPlay) {
                                mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
                                Log.d(TAG, "start service");
                                Intent intent = new Intent(MusicService.BROADCAST_ACTION);
                                intent.putExtra(MusicService.PARAM_PLAY, true);
                                getActivity().sendBroadcast(intent);
                            } else {
                                mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                                Log.d(TAG, "pause service");
                                Intent intent = new Intent(MusicService.BROADCAST_ACTION);
                                intent.putExtra(MusicService.PARAM_PLAY, false);
                                getActivity().sendBroadcast(intent);
                            }

                        } else startMusicService();
                    }
                }
            });
            mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* if(isMyServiceRunning(MusicService.class))
                        getActivity().stopService(new Intent(getActivity(),MusicService.class));*/
                    Intent intent = new Intent(MusicService.BROADCAST_ACTION);
                    getActivity().sendBroadcast(intent);
                }
            });
        }

        private void startMusicService() {
            musicIntent = new Intent(getActivity(), MusicService.class);
            musicIntent.putExtra("Link", mPos);
            SoundLab.get(getActivity()).getSounds().get(curentPos).setState(false);
            musicIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            getActivity().startService(musicIntent);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void bindSound(Sound sound, int pos) {
            mSound = sound;
            mPos = pos;

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
            holder.bindSound(sound, position);
            holder.mTitle.setText(sound.title);
            holder.mArtist.setText(sound.artist);
            holder.mDuration.setText(String.valueOf(sound.getDuration()));



            if (sound.isState()) {
                holder.v.setBackgroundColor(R.color.black);
                if (isPlay) {
                    holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                } else
                    holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                holder.mImageButtonPlayPause.setImageResource(android.R.drawable.ic_media_play);
                holder.v.setBackgroundColor(Color.WHITE);
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
}
