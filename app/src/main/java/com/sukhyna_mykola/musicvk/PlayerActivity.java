package com.sukhyna_mykola.musicvk;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;


import static com.sukhyna_mykola.musicvk.LikeService.DOWNLOADED;

import static com.sukhyna_mykola.musicvk.MusicService.ALBUM_ART;
import static com.sukhyna_mykola.musicvk.MusicService.BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.DATA_FROM_SERVICE;
import static com.sukhyna_mykola.musicvk.MusicService.FINISH;
import static com.sukhyna_mykola.musicvk.MusicService.INIT;


import static com.sukhyna_mykola.musicvk.MusicService.PARAM_BUFFERING;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_LOOP;

import static com.sukhyna_mykola.musicvk.MusicService.PARAM_NEXT_SOUND;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PLAY;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PREV_SOUND;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PROGRESS;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_PROGRESS_LOADING;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_SEEK_TO;
import static com.sukhyna_mykola.musicvk.MusicService.UPDATING;
import static com.sukhyna_mykola.musicvk.StartActivity.PERMISSION_REQUEST_CODE;


public class PlayerActivity extends FragmentActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {


    public static final String PLAY_LIST = "com.sukhyna_mykola.vkmusic.com.sukhyna_mykola.vkmusic.PLAY_LIST";
    private ImageButton playBtn;
    private ImageButton playerRepeatOne;
    private ImageButton playerRandomPos;
    private SeekBar secondaryProgress;
    private SeekBar progressMusic;
    private ViewPager pager;
    private TextView bufferText;
    private TextView currentTime;
    private TextView endTime;
    private ImageButton mLikeButton;
    private ImageButton mDownloadButton;
    private ImageButton mAddMusic;
    private ImageButton mShowInfo;
    private SoundInfoFragment mSoundInfoFragment;
    private FrameLayout infoView;

    private int percentProgresLoading;
    private int progress;
    private boolean isPlay;
    private boolean buffer;


    private int id;
    private Sound mSound;
    private int curentPos = -1;

    BroadcastReceiver mBroadcastReceiver;


    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        id = getIntent().getIntExtra(SoundListFragment.ID_SOUND, -1);
        progress = getIntent().getIntExtra(PARAM_PROGRESS, 0);
        isPlay = getIntent().getBooleanExtra(PARAM_PLAY, false);
        buffer = getIntent().getBooleanExtra(PARAM_BUFFERING, false);

        mSound = SoundLab.get().getSound(id);
        curentPos = SoundLab.get().getCurentPlayList().indexOf(mSound);

        mLikeButton = (ImageButton) findViewById(R.id.player_favorite);
        mDownloadButton = (ImageButton) findViewById(R.id.player_download);
        mAddMusic = (ImageButton) findViewById(R.id.player_add_sound);
        currentTime = (TextView) findViewById(R.id.player_cur_time);
        endTime = (TextView) findViewById(R.id.player_end_time);
        secondaryProgress = (SeekBar) findViewById(R.id.player_secondary_progress);
        playBtn = (ImageButton) findViewById(R.id.player_button_play_pause);
        playerRepeatOne = (ImageButton) findViewById(R.id.player_repeat_one);
        playerRandomPos = (ImageButton) findViewById(R.id.player_shuffle);
        bufferText = (TextView) findViewById(R.id.text_h);
        progressMusic = (SeekBar) findViewById(R.id.seekBar);
        pager = (ViewPager) findViewById(R.id.viewPager);
        infoView = (FrameLayout) findViewById(R.id.view_sound_info);
        mShowInfo = (ImageButton) findViewById(R.id.player_info);


        progressMusic.setMax(mSound.getDuration());
        progressMusic.setOnSeekBarChangeListener(this);

        setRandom(SettingActivity.isRandom);
        setLooping(SettingActivity.isLooping);
        updateButtons();

        if (buffer) {
            updateUI(progress, isPlay, 0);
            bufferText.setText(R.string.buffering);
        } else {
            updateUI(progress, isPlay, 100);
            bufferText.setText("# " + (curentPos + 1) + " / " + SoundLab.get().getCurentPlayList().size());
        }

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

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
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

                    progress = intent.getIntExtra(PARAM_PROGRESS, progressMusic.getProgress());
                    isPlay = intent.getBooleanExtra(MusicService.PARAM_PLAY, false);
                    percentProgresLoading = intent.getIntExtra(PARAM_PROGRESS_LOADING, percentProgresLoading);
                    updateUI(progress, isPlay, percentProgresLoading);
                }
                if (type == FINISH) {
                    finish();
                }
                if (type == BUFFERING) {

                    buffer = intent.getBooleanExtra(PARAM_BUFFERING, false);
                    if (buffer) {
                        runAnimation();
                        bufferText.setText(R.string.buffering);
                    } else {
                        stopAnimation();
                        bufferText.setText("# " + (curentPos + 1) + " / " + SoundLab.get().getCurentPlayList().size());
                    }
                }


                if (type == INIT) {

                    id = intent.getIntExtra(MusicService.PARAM_POS, -1);
                    mSound = SoundLab.get().getSound(id);

                    updateButtons();

                    secondaryProgress.setProgress(0);
                    curentPos = SoundLab.get().getCurentPlayList().indexOf(SoundLab.get().getSound(id));
                    progressMusic.setMax(mSound.getDuration());

                    if (mSoundInfoFragment != null) {
                        hideInfo();
                    }
                    pager.setCurrentItem(curentPos);

                }
                if (type == DOWNLOADED) {
                    updateButtons();
                }
                if (type == ALBUM_ART) {
                    pager.getAdapter().notifyDataSetChanged();
                }
            }
        };

        IntentFilter intFilt = new IntentFilter(DATA_FROM_SERVICE);
        registerReceiver(mBroadcastReceiver, intFilt);
    }

    private void runAnimation() {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.anim_buffer);
        bufferText.startAnimation(a);

    }

    private void stopAnimation() {
        bufferText.clearAnimation();
    }

    private void updateUI(int progress, boolean play, int percent) {
        if (progressMusic != null) {
            progressMusic.setProgress(progress);
        }

        if (secondaryProgress != null) {
            Log.d(TAG, String.valueOf(percent));
            secondaryProgress.setProgress(percent);
        }

        if (playBtn != null)
            if (play)
                playBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            else
                playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);

        if (currentTime != null)
            currentTime.setText(Constants.getTimeString(progress));
        if (endTime != null) {
            endTime.setText('-' + Constants.getTimeString(progressMusic.getMax() - progress));
        }
    }


    private void updateButtons() {
        if (SoundLab.mUser.containtSoundFavorite(mSound.getId())) {
            mLikeButton.setImageResource(R.drawable.ic_favorite_black_24dp);
            mLikeButton.animate().rotation(360).start();
        } else {
            mLikeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            mLikeButton.animate().rotation(-(360)).start();
        }

        if (SoundLab.mUser.containtSoundDown(mSound.getId())) {
            mDownloadButton.setImageResource(R.drawable.ic_file_download_complete_black_24dp);
            mDownloadButton.animate().rotation(360).start();
        } else {
            mDownloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
            mDownloadButton.animate().rotation(-(360)).start();
        }

        if (SoundLab.mUser.containtMyMusic(mSound.getId())) {
            mAddMusic.setImageResource(R.drawable.ic_check_black_24dp);
            mAddMusic.animate().rotation(360).start();
        } else {
            mAddMusic.setImageResource(R.drawable.ic_add_black_24dp);
            mAddMusic.animate().rotation(-(360)).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreference();
        SoundLab.saveObject();
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
                MainActivity.textCategory = getResources().getString(R.string.current_play_list);
                SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.get().getCurentPlayList());
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.player_search: {
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
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
                break;
            }
            case R.id.player_info: {
                showInfo();
                break;
            }
            case R.id.player_add_sound: {
                if (!SoundLab.mUser.containtMyMusic(mSound.getId())) {
                    final VKRequest addSound = VKApi.audio().add(VKParameters.from(VKApiConst.OWNER_ID, mSound.getOwner(), "audio_id", mSound.getId()));
                    addSound.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            SoundLab.mUser.addMyMusic(mSound);
                            updateButtons();
                            Toast.makeText(PlayerActivity.this, getResources().getString(R.string.added_to_my_music, mSound.getTitle()), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            Toast.makeText(PlayerActivity.this, getResources().getString(R.string.eror), Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    Toast.makeText(this, getResources().getString(R.string.is_in_my_music), Toast.LENGTH_SHORT).show();
                }

                break;
            }
            case R.id.player_favorite: {

                if (SoundLab.mUser.containtSoundFavorite(mSound.getId())) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    SoundLab.mUser.removeSoundFromFavorites(mSound.getId());

                } else {
                    if (!isMyServiceRunning(LikeService.class)) {
                        ((ImageButton) v).setImageResource(R.drawable.ic_favorite_black_24dp);
                        SoundLab.mUser.addFavotitesSound(mSound);
                        Intent intentDownload = new Intent(PlayerActivity.this, LikeService.class);
                        intentDownload.putExtra(LikeService.ID_DOWNLOAD, mSound.getId());
                        intentDownload.putExtra(LikeService.URL_DOWNLOAD, mSound.getUrl());
                        intentDownload.putExtra(LikeService.TITLE_DOWNLOAD, mSound.getTitle());
                        startService(intentDownload);

                    } else
                        Toast.makeText(this, R.string.wait, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.player_download: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    new DownloadSound(this, mSound);
                } else {
                    showNoStoragePermissionSnackbar(v);
                }
                break;
            }
        }
    }

       public void showNoStoragePermissionSnackbar(View v) {
        Snackbar.make(v, com.sukhyna_mykola.musicvk.R.string.dont_granted, Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings();

                        Toast.makeText(getApplicationContext(),
                                com.sukhyna_mykola.musicvk.R.string.grant_instructions,
                                Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .show();
    }

    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, PERMISSION_REQUEST_CODE);
    }

    private void showInfo() {
        if (mSoundInfoFragment == null) {
            mShowInfo.animate().rotation(180).setInterpolator(new BounceInterpolator()).start();
            pager.animate().translationY(pager.getHeight()).start();
            infoView.setVisibility(View.VISIBLE);
            mSoundInfoFragment = SoundInfoFragment.newInstance(mSound.id);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
            transaction.replace(R.id.view_sound_info, mSoundInfoFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } else {
            hideInfo();
        }
    }

    private void hideInfo() {
        mShowInfo.animate().rotation(0).setInterpolator(new BounceInterpolator()).start();
        pager.animate().translationY(0).start();
        getSupportFragmentManager().beginTransaction()
                .remove(mSoundInfoFragment).commit();
        mSoundInfoFragment = null;
    }

    private void setLooping(boolean b) {
        if (b)
            playerRepeatOne.setBackground(getResources().getDrawable(R.drawable.photo_background));
        else
            playerRepeatOne.setBackground(getResources().getDrawable(R.drawable.background_btn_none));
        ;
    }


    private void setRandom(boolean b) {
        if (b)
            playerRandomPos.setBackground(getResources().getDrawable(R.drawable.photo_background));
        else
            playerRandomPos.setBackground(getResources().getDrawable(R.drawable.background_btn_none));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
