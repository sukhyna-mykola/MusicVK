package com.sukhyna_mykola.musicvk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import static com.sukhyna_mykola.musicvk.MusicService.BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.FINISH;
import static com.sukhyna_mykola.musicvk.MusicService.INIT;


import static com.sukhyna_mykola.musicvk.MusicService.PARAM_BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_LOOP;

import static com.sukhyna_mykola.musicvk.MusicService.PARAM_NEXT_SOUND;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PLAY;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PREV_SOUND;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PROGRESS;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PROGRESS_LOADING;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_RAND;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_SEEK_TO;
import static com.sukhyna_mykola.musicvk.MusicService.UPDATING;


public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {


    public static final String PLAY_LIST ="com.sukhyna_mykola.vkmusic.com.sukhyna_mykola.vkmusic.PLAY_LIST" ;
    ImageButton playBtn;
    ImageButton playerRepeatOne;
    ImageButton playerRandomPos;
    SeekBar secondaryProgres;

    int percentProgresLoading;
    private float x1, x2;

    SeekBar progresMusic;
    ViewPager pager;


    public final static String DATA_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.DATE_FROM_SERVICE";
    TextView hTextView;
    private int progres;
    private TextView currentTime;
    private TextView endTime;
    private boolean isPlay;
    int id;
    private Sound mSound;
    private int curentPos = -1;
    ImageButton mLikeButton;
    LinearLayout bufferingInfoLayout;
    private boolean buffer;
    BroadcastReceiver mBroadcastReceiver;


    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        id = getIntent().getIntExtra(SoundListFragment.ID_SOUND, -1);
        mSound = SoundLab.get().getSound(id);
        progres = getIntent().getIntExtra(PARAM_PROGRESS, 0);
        isPlay = getIntent().getBooleanExtra(PARAM_PLAY, false);

        mLikeButton = (ImageButton) findViewById(R.id.player_favorite);
        currentTime = (TextView) findViewById(R.id.player_cur_time);
        endTime = (TextView) findViewById(R.id.player_end_time);
        secondaryProgres = (SeekBar) findViewById(R.id.player_secondary_progress);
        buffer = getIntent().getBooleanExtra(PARAM_BUFFERING, false);
        Log.d(TAG, "buffer  = " + buffer);
        updateFavoriteButton();

        curentPos = SoundLab.get().getCurentPlayList().indexOf(mSound);

        hTextView = (TextView) findViewById(R.id.text_h);
        if (buffer) {
            hTextView.setText("...буферизація...");
        } else {
            hTextView.setText("# "+(curentPos +1) +" / " + SoundLab.get().getCurentPlayList().size());
        }
        bufferingInfoLayout = (LinearLayout) findViewById(R.id.player_buffer);

        playBtn = (ImageButton) findViewById(R.id.player_button_play_pause);
        playerRepeatOne = (ImageButton) findViewById(R.id.player_repeat_one);
        playerRandomPos = (ImageButton) findViewById(R.id.player_shuffle);

        setRandom(SettingActivity.isRandom);
        setLooping(SettingActivity.isLooping);
        progresMusic = (SeekBar) findViewById(R.id.seekBar);
        progresMusic.setMax(mSound.getDuration());
        progresMusic.setOnSeekBarChangeListener(this);
        if (buffer)
            updateUI(progres, isPlay, 0);
        else
            updateUI(progres, isPlay, 100);
        pager = (ViewPager) findViewById(R.id.viewPager);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

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
                    percentProgresLoading = intent.getIntExtra(PARAM_PROGRESS_LOADING, percentProgresLoading);
                    updateUI(progres, isPlay, percentProgresLoading);
                }
                if (type == FINISH) {
                    finish();
                }
                if (type == BUFFERING) {
                    buffer = intent.getBooleanExtra(PARAM_BUFFERING, false);
                    Log.d(TAG, "buffer  " + buffer);
                    if (buffer) {
                        hTextView.setText("...буферизація...");
                    } else {
                        hTextView.setText("# "+(curentPos +1)+" / " + SoundLab.get().getCurentPlayList().size());
                    }
                }
                if (type == INIT) {

                    int id = intent.getIntExtra(MusicService.PARAM_POS, -1);
                    mSound = SoundLab.get().getSound(id);
                    updateFavoriteButton();

                   /* if (MainActivity.likeSounds.contains(mSound.id)) {
                        mLikeButton.setImageResource(R.mipmap.ic_like_true);
                        mLikeButton.animate().scaleY((float) 1).setInterpolator(new BounceInterpolator()).start();
                        mLikeButton.animate().scaleX((float) 1).setInterpolator(new BounceInterpolator()).start();
                    } else {
                        mLikeButton.setImageResource(R.mipmap.ic_like_false);
                        mLikeButton.animate().scaleY((float) 0.75).setInterpolator(new BounceInterpolator()).start();
                        mLikeButton.animate().scaleX((float) 0.75).setInterpolator(new BounceInterpolator()).start();
                    }*/
                    secondaryProgres.setProgress(0);
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

    private void updateUI(int progress, boolean play, int percent) {
        if (progresMusic != null) {
            progresMusic.setProgress(progress);

        }

        if (secondaryProgres != null) {
            Log.d(TAG, String.valueOf(percent));
            secondaryProgres.setProgress(percent);
        }

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


    private void updateFavoriteButton() {
        if (SoundLab.mUser.containtSoundFavorite(mSound.getId())) {

            mLikeButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            mLikeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreference();
        saveObject(SoundLab.mUser);
        unregisterReceiver(mBroadcastReceiver);
    }

    private void saveObject(User user) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/.user"))); //Select where you wish to save the file...
            oos.writeObject(user); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        } catch (Exception ex) {

        }
    }

    private void sendActionToService(String type) {
        Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
        intent.putExtra(MusicService.PARAM_TYPE, type);
        sendBroadcast(intent);
    }

    private void sendActionToService(String type, int progres) {
        Intent intent = new Intent(MusicService.DATA_TO_SERVICE);
        intent.putExtra(MusicService.PARAM_TYPE, type);
        intent.putExtra(PARAM_PROGRESS, progres);
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

    private void savePreference() {
        SharedPreferences sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(SettingActivity.LOOPING_KEY, SettingActivity.isLooping);
        ed.putBoolean(SettingActivity.RANDOM_KEY, SettingActivity.isRandom);
        ed.commit();


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
                SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.get().getCurentPlayList());
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                intent.putExtra(PLAY_LIST,true);
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
            case R.id.player_add_sound: {

                final VKRequest addSound = VKApi.audio().add(VKParameters.from(VKApiConst.OWNER_ID, mSound.getOwner(), "audio_id", mSound.getId()));
                addSound.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Toast.makeText(PlayerActivity.this, "Композиція " + mSound.getTitle() + " додана в ваші аудіозаписи", Toast.LENGTH_SHORT).show();
                    }
                });
                SoundLab.mUser.addDownloadedDound(mSound.id);
                break;
            }
            case R.id.player_favorite: {
                if (SoundLab.mUser.containtSoundFavorite(mSound.getId())) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    SoundLab.mUser.removeSoundFromFavorites(mSound.getId());


                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_favorite_black_24dp);
                    SoundLab.mUser.addFavotitesSound(mSound);
                    Intent intentDownload = new Intent(PlayerActivity.this, DownloadService.class);
                    intentDownload.setAction(DownloadService.ACTION_ADD_TO_FAVORITES);
                    intentDownload.putExtra(DownloadService.ID_DOWNLOAD, mSound.getId());
                    intentDownload.putExtra(DownloadService.URL_DOWNLOAD, mSound.getUrl());
                    startService(intentDownload);

                }
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

    int count = 0;
    String[] combo = {"♩♪♬", "♪♬", "♩♬", "♩♪", "♩", "♪", "♬"};


}