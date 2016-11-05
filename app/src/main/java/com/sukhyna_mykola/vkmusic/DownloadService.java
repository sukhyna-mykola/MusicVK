package com.sukhyna_mykola.vkmusic;

import android.app.Notification;
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
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends Service {
    public  DownloadService(){

    }
    public final static String URL_DOWNLOAD = " com.sukhyna_mykola.vkmusic.URL_DOWNLOAD";
    public final static String TITLE_DOWNLOAD = " com.sukhyna_mykola.vkmusic.TITLE_DOWNLOAD";
    String title;
    String urdDownloading;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    Integer notificationID = 100;
    int incr=0;
    int lenghtOfFile=0;
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

        new DownloadFile().execute();

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title)
                .setContentText("завантаження . . .")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        while (incr<100) {

                            mBuilder.setProgress(100, incr, false);

                            mNotifyManager.notify(notificationID, mBuilder.build());

                            try {

                                Thread.sleep(2*1000);
                            } catch (InterruptedException e) {
                              ;
                            }
                        }

                        mBuilder.setContentText("Завантаження завершене")

                                .setProgress(0,0,false);
                        mNotifyManager.notify(notificationID, mBuilder.build());
                    }
                }

        ).start();

        return START_NOT_STICKY;
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urlParams) {
            int count;
            try {
                URL url = new URL(urdDownloading);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                 lenghtOfFile = conexion.getContentLength();


                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory()+"/VKMusicPlayer/" +title+".mp3");

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
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stopSelf();
            mNotifyManager.cancel(notificationID);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            incr=values[0];
            super.onProgressUpdate(values);
        }
    }



}