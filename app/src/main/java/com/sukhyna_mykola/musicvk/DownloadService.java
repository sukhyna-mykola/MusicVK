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

import static com.sukhyna_mykola.musicvk.MusicService.DATA_FROM_SERVICE;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_TYPE;

public class DownloadService extends Service {
    public DownloadService() {

    }

    public final static int DOWNLOADED = 4;
    public final static int DOWNLOADING = 5;
    public final static String PARAM_DOWNLOAD_PROGRESS = " com.sukhyna_mykola.vkmusic.PARAM_DOWNLOAD_PROGRESS";
    public final static String PARAM_DOWNLOADED = " com.sukhyna_mykola.vkmusic.PARAM_DOWNLOADED";
    public final static int DOWNLOAD = 1;
    public final static int SAVE = 2;


    public final static String URL_DOWNLOAD = " com.sukhyna_mykola.vkmusic.URL_DOWNLOAD";
    public final static String TITLE_DOWNLOAD = " com.sukhyna_mykola.vkmusic.TITLE_DOWNLOAD";
    public final static String ID_DOWNLOAD = " com.sukhyna_mykola.vkmusic.ID_DOWNLOAD";
    public final static String ACTION_DOWNLOAD = " com.sukhyna_mykola.vkmusic.ACTION_DOWNLOAD";
    public final static String ACTION_ADD_TO_FAVORITES = " com.sukhyna_mykola.vkmusic.ACTION_ADD_TO_FAVORITES ";
    String title;
    int id;
    String actionType;
    String urdDownloading;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    static int notificationID = 100;
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
        id = intent.getIntExtra(ID_DOWNLOAD, -1);
        actionType = intent.getAction();


        new DownloadFile().execute();

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title);

        if (actionType == ACTION_ADD_TO_FAVORITES) {
            mBuilder.setContentText(getString(R.string.saving))
                    .setSmallIcon(R.drawable.ic_favorite_black_24dp);
        } else {
            mBuilder.setContentText(getString(R.string.downloading))
                    .setSmallIcon(android.R.drawable.stat_sys_download);
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        while (incr < 100) {

                            mBuilder.setProgress(100, incr, false);
                            mBuilder.setContentText(getSizeFileMB(lenghtOfFile * (incr) / 100.0) + "Mb / " + getSizeFileMB(lenghtOfFile) + "Mb");
                            mNotifyManager.notify(notificationID, mBuilder.build());

                            try {

                                Thread.sleep(2 * 1000);
                            } catch (InterruptedException e) {
                                ;
                            }
                        }
                        if (actionType == ACTION_ADD_TO_FAVORITES) {
                            mBuilder.setContentText(getString(R.string.saving_complete))
                                    .setProgress(0, 0, false);
                        } else {
                            mBuilder.setContentText(getString(R.string.downloading_ended))
                                    .setProgress(0, 0, false)
                                    .setSmallIcon(android.R.drawable.stat_sys_download_done);

                        }
                        mNotifyManager.notify(notificationID, mBuilder.build());
                    }
                }

        ).start();

        return START_NOT_STICKY;
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
                if (actionType == ACTION_DOWNLOAD)
                    output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/" + title + ".mp3");
                else {

                    output = new FileOutputStream(tempMp3);
                }
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;

                    publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                return tempMp3;
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(File s) {
            super.onPostExecute(s);
            if (actionType == ACTION_DOWNLOAD) {
                SoundLab.mUser.addDownloadedDound(id);
                sendEndAction(DOWNLOAD);
            }
            if (actionType == ACTION_ADD_TO_FAVORITES) {
                if (s != null) {
                    SoundLab.mUser.getSoundFavorite(id).setFile(s);
                    sendEndAction(SAVE);
                }

            }

            SoundLab.saveObject();
            mNotifyManager.cancel(notificationID);
            stopSelf();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            incr = values[0];
            sendDownloadingProgress(incr);
            super.onProgressUpdate(values);
        }
    }

    public String getSizeFileMB(double lenght) {
        double fileSizeInMB = lenght / (1024.0 * 1024.0);
        if (String.valueOf(fileSizeInMB).length() >= 6)
            return String.valueOf(fileSizeInMB).substring(0, 4);
        else return String.valueOf(fileSizeInMB);
    }


    private void sendEndAction(int what) {
        Intent intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, DOWNLOADED);
        intentPlayer.putExtra(PARAM_DOWNLOADED, what);
        sendBroadcast(intentPlayer);
    }


    private void sendDownloadingProgress(int progress) {
        Intent intentPlayer = new Intent(DATA_FROM_SERVICE);
        intentPlayer.putExtra(PARAM_TYPE, DOWNLOADING);
        intentPlayer.putExtra(PARAM_DOWNLOAD_PROGRESS, progress);
        sendBroadcast(intentPlayer);
    }


}