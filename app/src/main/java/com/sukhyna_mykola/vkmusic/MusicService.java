package com.sukhyna_mykola.vkmusic;


import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;

import static com.sukhyna_mykola.vkmusic.SoundListFragment.IDSOUND;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "TAG";
    private MediaPlayer mediaPlayer;
    PendingIntent pnextIntent;
    PendingIntent pplayIntent;
    PendingIntent pendingIntent;
    PendingIntent ppreviousIntent;
    PendingIntent pcloseIntent;

    private int position;

    UpdateInfo threadUpdating;

    private Sound currentSound;

    private RemoteViews notificationView;
    private Intent intentPlayer;

    private boolean randomPos;
    private boolean repeatPos;

    Context mContext;

    public final static String PARAM_TYPE = "com.sukhyna_mykola.vkmusic.play_play.PARAM_TYPE";
    public static final int INIT = 0;
    public static final int UPDATING = 1;
    public final static int LOOPRAND = 2;

    public final static String DATA_FROM_SERVICE = "com.sukhyna_mykola.vkmusic.DATE_FROM_SERVICE";
    public final static String DATA_TO_SERVICE = " com.sukhyna_mykola.vkmusic.DATA_TO_SERVICE";

    public final static String PARAM_PLAY_PAUSE = "com.sukhyna_mykola.vkmusic.play_play.PARAM_PLAY_PAUSE";
    public final static String PARAM_NEXT_SOUND = "com.sukhyna_mykola.vkmusic.play_play.PARAM_NEXT_SOUND";
    public final static String PARAM_PREV_SOUND = "com.sukhyna_mykola.vkmusic.play_play.PARAM_PREV_SOUND";
    public final static String PARAM_NULL = "com.sukhyna_mykola.vkmusic.play_play.PARAM_NULL";
    public final static String PARAM_SEEK_TO = "com.sukhyna_mykola.vkmusic.play_play.PARAM_SEEK_TO";
    public final static String PARAM_LOOP = "com.sukhyna_mykola.vkmusic.play_play.PARAM_LOOP";
    public final static String PARAM_RAND = "com.sukhyna_mykola.vkmusic.play_play.PARAM_RAND";
    public final static String PARAM_PLAY_SOUND_POSITION = "com.sukhyna_mykola.vkmusic.play_play.PARAM_PLAY_SOUND_POSITION";

    public final static String PARAM_PROGRESS = "com.sukhyna_mykola.vkmusic.PARAM_PROGRESS ";
    public static final String PARAM_PLAY = "com.sukhyna_mykola.vkmusic.PARAM_PLAY";
    public final static String PARAM_POS = "com.sukhyna_mykola.vkmusic.PARAM_POS";


    BroadcastReceiver br;

    @Override
    public void onCreate() {
        position = -1;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mContext = this;
        randomPos = SettingActivity.isRandom;
        repeatPos = SettingActivity.isLooping;
        mediaPlayer.setLooping(repeatPos);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra(PARAM_TYPE);

                if (type.equals(PARAM_PLAY_PAUSE)) {
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
                    repeatPos = !repeatPos;
                    mediaPlayer.setLooping(repeatPos);
                }
                if (type.equals(PARAM_RAND)) {
                    randomPos = !randomPos;
                }
                if (type.equals(PARAM_PLAY_SOUND_POSITION)) {
                    int newID =  intent.getIntExtra(PARAM_POS,-1);
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
        Log.d(TAG, "onStartCommand");
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            if (position != -1)
                currentSound.setUsing(false);

            currentSound = SoundLab.get().getSound(intent.getIntExtra(PARAM_POS,-1));
            currentSound.setUsing(true);

            position =  SoundLab.get().getCurentPlayList().indexOf(currentSound);
            initPlayer(currentSound.url);

            Log.i(TAG, "Received Start Foreground Intent ");


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
            Log.i(TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                sendUpdate();
            } else {
                mediaPlayer.start();
                sendUpdate();
            }
            updateNotification();
            Log.i(TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            nextSound();
            updateNotification();
            Log.i(TAG, "Clicked Next");
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            currentSound.setUsing(false);
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
            notificationView.setImageViewResource(R.id.status_bar_play, android.R.drawable.ic_media_pause);
        else
            notificationView.setImageViewResource(R.id.status_bar_play, android.R.drawable.ic_media_play);


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

        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.putExtra(IDSOUND, currentSound.getId());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (mediaPlayer.isPlaying())
            builder.setSmallIcon(android.R.drawable.ic_media_play);
        else
            builder.setSmallIcon(android.R.drawable.ic_media_pause);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setCustomBigContentView(getComplexNotificationView());
        } else {
            builder = builder.setContentTitle(currentSound.title)
                    .setContentText(currentSound.artist)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery);
        }
        return builder;
    }


    public void updateNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification().build());
    }


    private void initPlayer(String url) {
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
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
        currentSound.setUsing(false);
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
        Log.d(TAG, "player onPrepared");
        mediaPlayer.start();
        updateNotification();
        threadUpdating = new UpdateInfo();
    }

    //викликається коли файл завершив програвання
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!repeatPos)
            nextSound();
    }

    private void nextSound() {
        if (!repeatPos) {
            currentSound.setUsing(false);

            if (randomPos)
                position = new Random().nextInt(SoundLab.get().getCurentPlayList().size());
            else position++;

            if (position < SoundLab.get().getCurentPlayList().size()) {
                initcurrentSound(position);
            } else {
                position = SoundLab.get().getCurentPlayList().size() - 1;
                initcurrentSound(position);
            }
            sendPos();
        }
    }


    private void initcurrentSound(int pos) {
        mediaPlayer.reset();
        currentSound = SoundLab.get().getCurentPlayList().get(pos);
        currentSound.setUsing(true);
        initPlayer(currentSound.url);
    }

    private void playSoundPosition(int newPosition) {
        position = newPosition;
        currentSound.setUsing(false);
        if (position > 0 && position < SoundLab.get().getCurentPlayList().size()) {
            initcurrentSound(position);
        } else {
            position = 0;
            initcurrentSound(position);
        }
        sendPos();
    }

    private void prevSound() {
        if (!repeatPos) {
            currentSound.setUsing(false);

            if (randomPos)
                position = new Random().nextInt(SoundLab.get().getCurentPlayList().size());
            else position--;

            if (position > 0) {
                initcurrentSound(position);
            } else {
                position = 0;
                initcurrentSound(position);
            }
            sendPos();
        }
    }

    private class UpdateInfo implements Runnable {
        Thread thread;

        public UpdateInfo() {

            thread = new Thread(this);
            thread.start();

        }

        @Override
        public void run() {
            while (currentSound.isUsing()) {
                try {
                    sendUpdate();
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "stop Thread");

        }
    }
}
