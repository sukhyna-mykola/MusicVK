package com.sukhyna_mykola.musicvk;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import static com.sukhyna_mykola.musicvk.MusicService.DATA_FROM_SERVICE;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_TYPE;

public class LikeService extends Service {

    public final static int DOWNLOADED = 4;

    public final static String URL_DOWNLOAD = " com.sukhyna_mykola.vkmusic.URL_DOWNLOAD";
    public final static String TITLE_DOWNLOAD = " com.sukhyna_mykola.vkmusic.TITLE_DOWNLOAD";
    public final static String ID_DOWNLOAD = " com.sukhyna_mykola.vkmusic.ID_DOWNLOAD";


    private String title;
    private int id;
    private String urdDownloading;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    int incr = 0;
    int lenghtOfFile = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        urdDownloading = intent.getStringExtra(URL_DOWNLOAD);
        title = intent.getStringExtra(TITLE_DOWNLOAD);
        id = intent.getIntExtra(ID_DOWNLOAD, 0);
        new DownloadFile().execute();
        return START_NOT_STICKY;
    }

    private void showNotification(String text, boolean error) {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        if (error)
            mBuilder.setContentTitle(text)
                    .setContentText(getString(R.string.eror))
                    .setSmallIcon(R.drawable.ic_favorite_border_black_24dp);
        else {
            mBuilder.setContentTitle(text)
                    .setContentText(getString(R.string.saved))
                    .setSmallIcon(R.drawable.ic_favorite_black_24dp);
        }


        mNotifyManager.notify(id, mBuilder.build());
    }


    private class DownloadFile extends AsyncTask<String, Integer, File> {

        @Override
        protected File doInBackground(String... urlParams) {
            int count;
            try {
                URL url = new URL(urdDownloading);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                lenghtOfFile = conexion.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output;
                File tempMp3 = File.createTempFile("tmp", "mp3", getCacheDir());


                output = new FileOutputStream(tempMp3);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                    if(SoundLab.mUser.getSoundFavorite(id)==null)
                        return null;
                }

                output.flush();
                output.close();
                input.close();
                return tempMp3;
            } catch (Exception e) {
                incr = 666;
            }
            return null;
        }

        @Override
        protected void onPostExecute(File s) {
            super.onPostExecute(s);
            if (incr != 666) {
                 if(SoundLab.mUser.getSoundFavorite(id)!=null){
                SoundLab.mUser.getSoundFavorite(id).setFile(s);
                Intent intentPlayer = new Intent(DATA_FROM_SERVICE);
                intentPlayer.putExtra(PARAM_TYPE, DOWNLOADED);
                sendBroadcast(intentPlayer);
                showNotification(title,false);}
            } else {
                SoundLab.mUser.removeSoundFromFavorites(id);
                showNotification(title,true);
            }
            SoundLab.saveObject();
            stopSelf();

        }


    }
}