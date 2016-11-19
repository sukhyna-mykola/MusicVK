package com.sukhyna_mykola.musicvk;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.Toast;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG_MusicService = "MusicService";
    private MediaPlayer mediaPlayer;

    PendingIntent pnextIntent;
    PendingIntent pplayIntent;
    PendingIntent pendingIntent;
    PendingIntent ppreviousIntent;
    PendingIntent pcloseIntent;

    private int position;

    private boolean nextSound;

    UpdateInfo threadUpdating;

    private Sound currentSound;

    private RemoteViews notificationView;
    private Intent intentPlayer;

    private boolean playing;
    private int percendLoadingSound;

    Context mContext;

    public final static String PARAM_TYPE = "com.sukhyna_mykola.vkmusic.PARAM_TYPE";
    public static final int INIT = 0;
    public static final int UPDATING = 1;
    public final static int FINISH = 2;
    public final static int BUFFERING = 3;

    public final static String DATA_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.DATE_FROM_SERVICE";
    public final static String DATA_TO_SERVICE = " com.sukhyna_mykola.vkmusic.DATA_TO_SERVICE";

    public final static String PARAM_PLAY_PAUSE = "com.sukhyna_mykola.vkmusic.PARAM_PLAY_PAUSE";
    public final static String PARAM_BUFFERING = "com.sukhyna_mykola.vkmusic.PARAM_BUFFERING";
    public final static String PARAM_NEXT_SOUND = "com.sukhyna_mykola.vkmusic.PARAM_NEXT_SOUND";
    public final static String PARAM_PREV_SOUND = "com.sukhyna_mykola.vkmusic.PARAM_PREV_SOUND";
    public final static String PARAM_NULL = "com.sukhyna_mykola.vkmusic.PARAM_NULL";
    public final static String PARAM_SEEK_TO = "com.sukhyna_mykola.vkmusic.PARAM_SEEK_TO";
    public final static String PARAM_LOOP = "com.sukhyna_mykola.vkmusic.PARAM_LOOP";
    public final static String PARAM_PLAY_SOUND_POSITION = "com.sukhyna_mykola.vkmusic.PARAM_PLAY_SOUND_POSITION";


    public final static String PARAM_PROGRESS = "com.sukhyna_mykola.vkmusic.PARAM_PROGRESS ";
    public final static String PARAM_PROGRESS_LOADING = "com.sukhyna_mykola.vkmusic.PARAM_PROGRESS_LOADING";
    public static final String PARAM_PLAY = "com.sukhyna_mykola.vkmusic.PARAM_PLAY";
    public final static String PARAM_POS = "com.sukhyna_mykola.vkmusic.PARAM_POS";
    Intent notificationIntent;

    BroadcastReceiver br;

    @Override
    public void onCreate() {
        position = -1;
        Log.i(TAG_MusicService, "onCreate");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnErrorListener(this);
        mContext = this;

        mediaPlayer.setLooping(SettingActivity.isLooping);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra(PARAM_TYPE);

                if (type.equals(PARAM_PLAY_PAUSE)) {
                    Log.i(TAG_MusicService, "PARAM_PLAY_PAUSE");
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    else mediaPlayer.start();
                }
                if (type.equals(PARAM_NEXT_SOUND)) {
                    nextSound();
                }
                if (type.equals(PARAM_PREV_SOUND)) {
                    prevSound();
                }
                if (type.equals(PARAM_NULL)) {

                }
                if (type.equals(PARAM_SEEK_TO)) {
                    int progres = intent.getIntExtra(PARAM_PROGRESS, mediaPlayer.getDuration());
                    mediaPlayer.seekTo(progres);
                }
                if (type.equals(PARAM_LOOP)) {
                    mediaPlayer.setLooping(SettingActivity.isLooping);
                }

                if (type.equals(PARAM_PLAY_SOUND_POSITION)) {
                    int newID = intent.getIntExtra(PARAM_POS, -1);
                    int newPosition = SoundLab.get().getCurentPlayList().indexOf(SoundLab.get().getSound(newID));
                    playSoundPosition(newPosition);
                }
                updateNotification();
                sendPos();
                sendUpdate();


            }
        };
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG_MusicService, "Constants.ACTION.STARTFOREGROUND_ACTION");

            currentSound = SoundLab.get().getSound(intent.getIntExtra(PARAM_POS, -1));
            position = SoundLab.get().getCurentPlayList().indexOf(currentSound);

            sendPos();
            sendBuffering(!playing);

            initPlayer(currentSound);

            setNotificationIntent();
            Intent previousIntent = new Intent(this, MusicService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            ppreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0);

            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(this, MusicService.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0);

            Intent closeIntent = new Intent(this, MusicService.class);
            closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            pcloseIntent = PendingIntent.getService(this, 0,
                    closeIntent, 0);


            showNotification();

            IntentFilter intFilt = new IntentFilter(DATA_TO_SERVICE);
            registerReceiver(br, intFilt);


        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            prevSound();
            updateNotification();

        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                sendUpdate();
            } else {
                mediaPlayer.start();
                sendUpdate();
            }
            updateNotification();

        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            nextSound();
            updateNotification();
            Log.i(TAG_MusicService, "Clicked Next");
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG_MusicService, "Received Stop Foreground Intent");

            sendFinish();
            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    void showNotification() {
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification().build());
    }

    private RemoteViews getComplexNotificationView() {
        // Using RemoteViews to bind custom layouts into Notification
        notificationView = new RemoteViews(this.getPackageName(), R.layout.notification_view);
        // Locate and set the Image into customnotificationtext.xml ImageViews
        if (mediaPlayer.isPlaying())
            notificationView.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause_white_24dp);
        else
            notificationView.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play_arrow_white_24dp);

        // Locate and set the Text into customnotificationtext.xml TextViews

        notificationView.setTextViewText(R.id.status_bar_track_name, currentSound.title);

        notificationView.setTextViewText(R.id.status_bar_artist_name, currentSound.artist);
        notificationView.setImageViewResource(R.id.status_bar_collapse, android.R.drawable.ic_menu_close_clear_cancel);

        notificationView.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_plrev, ppreviousIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        return notificationView;

    }

    protected NotificationCompat.Builder buildNotification() {
        setNotificationIntent();
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (mediaPlayer.isPlaying())
            builder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp);
        else
            builder.setSmallIcon(R.drawable.ic_pause_white_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setCustomBigContentView(getComplexNotificationView());
        } else {
            builder = builder.setContentTitle(currentSound.title)
                    .setContentText(currentSound.artist)
                    .setSmallIcon(R.drawable.ic_play_arrow_white_24dp);
        }
        return builder;
    }


    public void updateNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification().build());
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initPlayer(Sound sound) {
        try {
            if (sound.getFile() == null) {
                mediaPlayer.setDataSource(sound.getUrl());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();

            } else playMp3(sound.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        unregisterReceiver(br);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    private void sendUpdate() {
        intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, UPDATING);
        intentPlayer.putExtra(PARAM_PROGRESS, (mediaPlayer.getCurrentPosition()));
        intentPlayer.putExtra(PARAM_PLAY, mediaPlayer.isPlaying());
        intentPlayer.putExtra(PARAM_PROGRESS_LOADING, percendLoadingSound);
        sendBroadcast(intentPlayer);
    }

    private void sendFinish() {
        intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, FINISH);
        sendBroadcast(intentPlayer);
    }

    private void sendBuffering(boolean show) {
        intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, BUFFERING);
        intentPlayer.putExtra(PARAM_BUFFERING, show);
        sendBroadcast(intentPlayer);
    }

    private void sendPos() {
        intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, INIT);
        intentPlayer.putExtra(PARAM_POS, currentSound.getId());
        sendBroadcast(intentPlayer);
    }


    //викликається коли файл готопий то програвння
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG_MusicService, "onPrepared");
        mediaPlayer.start();
        playing = true;
        mediaPlayer.setLooping(SettingActivity.isLooping);
        updateNotification();
        threadUpdating = new UpdateInfo();
        nextSound = true;
        sendBuffering(!playing);
    }

    //викликається коли файл завершив програвання
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG_MusicService, "onCompletion");
        if (nextSound)
            nextSound();

    }

    private void nextSound() {
        playing = false;
        sendBuffering(!playing);
        nextSound = false;

        if (SettingActivity.isRandom) {
            position = new Random().nextInt(SoundLab.get().getCurentPlayList().size());
        } else position++;

        if (position < SoundLab.get().getCurentPlayList().size()) {
            initcurrentSound(position);
        } else {
            position = 0;
            initcurrentSound(position);
        }
        sendPos();
    }


    private void playSoundPosition(int newPosition) {
        playing = false;
        sendBuffering(!playing);
        nextSound = false;
        position = newPosition;

        if (position >= 0 && position < SoundLab.get().getCurentPlayList().size()) {
            initcurrentSound(position);
        } else {
            position = 0;
            initcurrentSound(position);
        }
        sendPos();
    }


    private void initcurrentSound(int pos) {

        mediaPlayer.reset();
        currentSound = SoundLab.get().getCurentPlayList().get(pos);
        initPlayer(currentSound);

    }

    private void prevSound() {
        playing = false;
        sendBuffering(!playing);
        nextSound = false;

        if (SettingActivity.isRandom)
            position = new Random().nextInt(SoundLab.get().getCurentPlayList().size());
        else position--;

        if (position >= 0) {
            initcurrentSound(position);
        } else {
            position = SoundLab.get().getCurentPlayList().size() - 1;
            initcurrentSound(position);
        }
        sendPos();

    }

    private boolean playMp3(File convertedFile) {
        try {
            FileInputStream fis = new FileInputStream(convertedFile);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        percendLoadingSound = percent;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO: {
                Toast.makeText(mContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                break;
            }
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
            case MediaPlayer.MEDIA_ERROR_MALFORMED: {
                Toast.makeText(mContext, R.string.bitsstream_error, Toast.LENGTH_SHORT).show();
                break;
            }
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT: {
                nextSound();
                break;
            }

        }
        return false;
    }

    private class UpdateInfo implements Runnable {
        Thread thread;

        public UpdateInfo() {

            thread = new Thread(this);
            thread.start();

        }

        @Override
        public void run() {
            while (playing) {
                try {
                    updateNotification();
                    sendUpdate();
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG_MusicService, "stop Thread");

        }
    }

    private void setNotificationIntent() {
        Log.d(TAG_MusicService, "setNotificationIntent buffer = " + !playing);
        notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.putExtra(SoundListFragment.ID_SOUND, currentSound.getId());
        notificationIntent.putExtra(PARAM_PROGRESS, mediaPlayer.getCurrentPosition());
        notificationIntent.putExtra(PARAM_PLAY, mediaPlayer.isPlaying());
        notificationIntent.putExtra(PARAM_BUFFERING, !playing);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

}
