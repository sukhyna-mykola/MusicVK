package com.sukhyna_mykola.vkmusic;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import static com.sukhyna_mykola.vkmusic.MusicService.INIT;

import static com.sukhyna_mykola.vkmusic.MusicService.LOOPRAND;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_LOOP;

import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_NEXT_SOUND;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_PLAY;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_PREV_SOUND;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_RAND;
import static com.sukhyna_mykola.vkmusic.MusicService.PARAM_SEEK_TO;
import static com.sukhyna_mykola.vkmusic.MusicService.UPDATING;
import static com.sukhyna_mykola.vkmusic.SoundListFragment.IDSOUND;


public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {




    ImageButton playBtn;
    ImageButton playerRepeatOne;
    ImageButton playerRandomPos;

    TextView timeMusic;
    private float x1, x2;

    SeekBar progresMusic;
    ViewPager pager;


    private static final String TAG = "TAG_PLAYER_ACTIVITY";
    public static final String ARG_ID_SOUND = "com.sukhyna_mykola.vkmusic.ARG_ID_SOUND";
    public final static String PARAM_PROGRESS = "com.sukhyna_mykola.vkmusic.PARAM_PROGRESS ";


    public final static String DATA_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.DATE_FROM_SERVICE";

    private int progres;
    private TextView currentTime;
    private TextView endTime;
    private String time;
    private boolean isPlay;
    int id;
    private Sound mSound;
    private int curentPos = -1;
    ImageButton mLikeButton;

    BroadcastReceiver mBroadcastReceiver;


    private static final String EXTRA_CRIME_ID = "com.sukhyna_mykola.EXTRA_CRIME_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        id = getIntent().getIntExtra(SoundListFragment.IDSOUND, -1);

        mLikeButton = (ImageButton) findViewById(R.id.like_btn);
        currentTime = (TextView) findViewById(R.id.player_cur_time);
        endTime = (TextView) findViewById(R.id.player_end_time);


        mSound = SoundLab.get().getSound(id);

        curentPos = SoundLab.get().getCurentPlayList().indexOf(mSound);

        playBtn = (ImageButton) findViewById(R.id.player_button_play_pause);
        playerRepeatOne = (ImageButton) findViewById(R.id.player_repeat_one);
        playerRandomPos = (ImageButton) findViewById(R.id.player_shuffle);

        setRandom(SettingActivity.isRandom);
        setLooping(SettingActivity.isLooping);

        progresMusic = (SeekBar) findViewById(R.id.seekBar);
        progresMusic.setMax(mSound.getDuration());
        progresMusic.setOnSeekBarChangeListener(this);
        pager = (ViewPager) findViewById(R.id.viewPager);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "position  " + position);
                Log.d(TAG, "curentPos  " + curentPos);
                if (curentPos > position) {
                    sendActionToService(PARAM_PREV_SOUND);
                } else if (curentPos < position) {
                    sendActionToService(PARAM_NEXT_SOUND);
                }
                curentPos = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Log.d(TAG, "position getItem " + position);
                Sound sound = SoundLab.get().getCurentPlayList().get(position);

                return PlayerFragment.newInstance(sound.id);
            }

            @Override
            public int getCount() {
                return SoundLab.get().getCurentPlayList().size();
            }
        });

        for (int i = 0; i < SoundLab.get().getCurentPlayList().size(); i++) {
            if (SoundLab.get().getCurentPlayList().get(i).getId() == (id)) {
                pager.setCurrentItem(i);
                break;
            }
        }


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(MusicService.PARAM_TYPE, -1);
                if (type == UPDATING) {
                    progres = intent.getIntExtra(PARAM_PROGRESS, progresMusic.getProgress());
                    isPlay = intent.getBooleanExtra(MusicService.PARAM_PLAY, false);
                    updateUI(progres, isPlay);
                }

                if (type == INIT) {
                    int id = intent.getIntExtra(MusicService.PARAM_POS, -1);
                    mSound = SoundLab.get().getSound(id);

                   /* if (MainActivity.likeSounds.contains(mSound.id)) {
                        mLikeButton.setImageResource(R.mipmap.ic_like_true);
                        mLikeButton.animate().scaleY((float) 1).setInterpolator(new BounceInterpolator()).start();
                        mLikeButton.animate().scaleX((float) 1).setInterpolator(new BounceInterpolator()).start();
                    } else {
                        mLikeButton.setImageResource(R.mipmap.ic_like_false);
                        mLikeButton.animate().scaleY((float) 0.75).setInterpolator(new BounceInterpolator()).start();
                        mLikeButton.animate().scaleX((float) 0.75).setInterpolator(new BounceInterpolator()).start();
                    }*/

                    curentPos = SoundLab.get().getCurentPlayList().indexOf(SoundLab.get().getSound(id));
                    progresMusic.setMax(mSound.getDuration());
                    pager.setCurrentItem(curentPos);

                }
            }
        };
        getSupportActionBar().setTitle(R.string.player);
        IntentFilter intFilt = new IntentFilter(DATA_FROM_SERVICE);
        registerReceiver(mBroadcastReceiver, intFilt);
    }

    private void updateUI(int progress, boolean play) {
        if (progresMusic != null)
            progresMusic.setProgress(progress);
        if (playBtn != null)
            if (play)
                playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            else
                playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        if (currentTime != null)
            currentTime.setText(Constants.getTimeString(progress));
        if (endTime != null) {
            endTime.setText('-' + Constants.getTimeString(progresMusic.getMax() - progress));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void sendActionToService(String type) {
        Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
        intent.putExtra(MusicService.PARAM_TYPE, type);
        sendBroadcast(intent);
    }

    private void sendActionToService(String type, int progres) {
        Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
        intent.putExtra(MusicService.PARAM_TYPE, type);
        intent.putExtra(MusicService.PARAM_PROGRESS, progres);
        sendBroadcast(intent);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            sendActionToService(PARAM_SEEK_TO, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_button_play_pause: {
                sendActionToService(MusicService.PARAM_PLAY_PAUSE);
                break;
            }
            case R.id.player_next_sound: {
                sendActionToService(MusicService.PARAM_NEXT_SOUND);
                break;
            }
            case R.id.player_prev_sound: {
                sendActionToService(MusicService.PARAM_PREV_SOUND);
                break;
            }
            case R.id.player_list_sounds_btn: {
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
            case R.id.player_repeat_one: {
                SettingActivity.isLooping = !SettingActivity.isLooping;
                setLooping(SettingActivity.isLooping);
                sendActionToService(PARAM_LOOP);
                break;
            }
            case R.id.player_shuffle: {
                SettingActivity.isRandom = !SettingActivity.isRandom;
                setRandom(SettingActivity.isRandom);
                sendActionToService(PARAM_RAND);
                break;
            }
        }
    }


    private void setLooping(boolean b) {
        if (b)
            playerRepeatOne.setBackground(getResources().getDrawable(R.drawable.photo_background));
            // playerRepeatOne.setBackgroundColor(getResources().getColor(R.color.vk_light_color));

        else
            playerRepeatOne.setBackground(getResources().getDrawable(R.drawable.background_btn_none));
        ;
    }


    private void setRandom(boolean b) {
        if (b)
            playerRandomPos.setBackground(getResources().getDrawable(R.drawable.photo_background));
        else
            playerRandomPos.setBackground(getResources().getDrawable(R.drawable.background_btn_none));
        ;
    }

}
