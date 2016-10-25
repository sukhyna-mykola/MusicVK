package com.sukhyna_mykola.vkmusic;


import java.io.IOException;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "TAG";
    private MediaPlayer mediaPlayer;
    PendingIntent pnextIntent;
    PendingIntent pplayIntent;
    PendingIntent pendingIntent;
    PendingIntent ppreviousIntent;
    PendingIntent pcloseIntent;
    private int position;
    private SoundLab mSoundLab;
    private Sound curentSound;
    RemoteViews notificationView;
    private final Handler handler = new Handler();
    private UpdateInfo threadUpdating;


    public final static String PARAM_PLAY = "play_play";
    public final static String BROADCAST_ACTION = "ru.startandroid.develop.p0961servicebackbroadcast";
    BroadcastReceiver br;

    @Override
    public void onCreate() {
        position = -1;
        mSoundLab = SoundLab.get(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        Log.d(TAG, "mSoundLab.size = " + mSoundLab.getSounds().size());
        Log.d(TAG, "onCreate");
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean statusMediaPlayer = intent.getBooleanExtra(PARAM_PLAY, false);
                if (statusMediaPlayer)
                    mediaPlayer.start();
                else mediaPlayer.pause();
                updateNotification();
                sendMyBroadcast(statusMediaPlayer);

            }
        };
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

            if (position != -1)
                curentSound.setState(false);
            position = intent.getExtras().getInt("Link");
            curentSound = mSoundLab.getSounds().get(position);
            initPlayer(curentSound.url);


            Log.i(TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

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

            IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
            registerReceiver(br, intFilt);


        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {

            prevSound();
            updateNotification();
            Log.i(TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                sendMyBroadcast(mediaPlayer.isPlaying());
            } else {
                mediaPlayer.start();
                sendMyBroadcast(mediaPlayer.isPlaying());
            }
            updateNotification();
            Log.i(TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            nextSound();
            updateNotification();
            Log.i(TAG, "Clicked Next");
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            curentSound.setState(false);
            threadUpdating.setRunning(false);
            sendMyBroadcast(true);
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
        notificationView.setTextViewText(R.id.status_bar_track_name, curentSound.title);
        notificationView.setTextViewText(R.id.status_bar_artist_name, curentSound.artist);
        notificationView.setTextViewText(R.id.status_bar_time_name, Constants.getTimeString(mediaPlayer.getCurrentPosition())+" / "+curentSound.getDuration());
        // notificationView.setImageViewBitmap(R.id.status_bar_album_art, podcast.getImage());

        notificationView.setImageViewResource(R.id.status_bar_collapse, android.R.drawable.ic_menu_close_clear_cancel);
        notificationView.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_plrev, ppreviousIntent);
        notificationView.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        return notificationView;

    }

    protected NotificationCompat.Builder buildNotification() {


        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)

                .setAutoCancel(true)
                // Set PendingIntent into Notification
                .setContentIntent(pendingIntent);
        if (mediaPlayer.isPlaying())
            builder.setSmallIcon(android.R.drawable.ic_media_play);
        else
            builder.setSmallIcon(android.R.drawable.ic_media_pause);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // build a complex notification, with buttons and such
            //
            builder = builder.setCustomBigContentView(getComplexNotificationView());
        } else {
            // Build a simpler notification, without buttons
            //
            builder = builder.setContentTitle(curentSound.title)
                    .setContentText(curentSound.artist)
                    .setSmallIcon(android.R.drawable.ic_menu_gallery);
        }
        return builder;
    }


    public void updateNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, buildNotification().build());
    }

    private void updateNotificationOld() {
/*NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
       NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(curentSound.sound.title)
                .setContentText(curentSound.sound.artist);
        Notification notification =mNotifyBuilder.build();
                manager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,notification);*/
        /*Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_menu_gallery);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(curentSound.sound.title)
                .setTicker(curentSound.sound.title)
                .setContentText(curentSound.sound.artist)
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

*/
    }

    private void initPlayer(String url) {
        try {

            mediaPlayer.setDataSource(url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            curentSound.setState(true);
            sendMyBroadcast(true);
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
        threadUpdating.setRunning(true);
        unregisterReceiver(br);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    private void sendMyBroadcast(boolean status) {
        Intent intent = new Intent(SoundListFragment.ACTION_FROM_SERVICE);
        intent.putExtra(SoundListFragment.PARAM_POS, position);
        intent.putExtra(SoundListFragment.PARAM_PLAY, status);

        sendBroadcast(intent);
    }

    private void sendTimeBroadcast() {
        Intent intent = new Intent(SoundListFragment.TIME_FROM_SERVICE);
        intent.putExtra(SoundListFragment.PARAM_TIME, Constants.getTimeString(mediaPlayer.getCurrentPosition()));
        sendBroadcast(intent);
    }

    //викликається коли файл готопий то програвння
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "player onPrepared");
        mediaPlayer.start();
        updateNotification();
        threadUpdating = new UpdateInfo();
        Toast.makeText(this, curentSound.title, Toast.LENGTH_SHORT).show();
    }

    //викликається коли файл завершив програвання
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, " onCompletion");
        threadUpdating.setRunning(false);
        nextSound();
    }

    private void nextSound() {
        curentSound.setState(false);
        if (++position < mSoundLab.getSounds().size()) {
            initCurentSound(position);
        } else {
            position = mSoundLab.getSounds().size() - 1;
            initCurentSound(position);
        }
    }


    private void initCurentSound(int pos) {
        mediaPlayer.reset();
        curentSound = mSoundLab.getSounds().get(pos);
        initPlayer(curentSound.url);
    }

    private void prevSound() {
        curentSound.setState(false);
        if (--position > 0) {
            initCurentSound(position);
        } else {
            position = 0;
            initCurentSound(position);
        }
    }

    private class UpdateInfo implements Runnable {
        private boolean running;

        Thread thread;

        public UpdateInfo() {
            running = true;
            thread = new Thread(this, "Поток для примера");
            thread.start();

        }

        public boolean isRunning() {

            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }


        @Override
        public void run() {
            while (running) {



                    try {
                        updateNotification();
                        // sendTimeBroadcast();
                    } catch (Exception e) {

                    }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }



}
