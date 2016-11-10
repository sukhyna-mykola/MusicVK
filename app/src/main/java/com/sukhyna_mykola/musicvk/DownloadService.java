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

public class DownloadService extends Service {
    public DownloadService() {

    }
    public final static int DOWNLOADED=4 ;
    public final static String URL_DOWNLOAD = " com.sukhyna_mykola.vkmusic.URL_DOWNLOAD";
    public final static String TITLE_DOWNLOAD = " com.sukhyna_mykola.vkmusic.TITLE_DOWNLOAD";
    public final static String ID_DOWNLOAD = " com.sukhyna_mykola.vkmusic.ID_DOWNLOAD";
    public final static String ACTION_DOWNLOAD = " com.sukhyna_mykola.vkmusic.ACTION_DOWNLOAD";
    public final static String ACTION_ADD_TO_FAVORITES = " com.sukhyna_mykola.vkmusic.ACTION_ADD_TO_FAVORITES ";
    String title;
    int id;
    String urdDownloading;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    Integer notificationID = 100;
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
        id =intent.getIntExtra(ID_DOWNLOAD,-1);

        if (intent.getAction() == ACTION_DOWNLOAD) {
            new DownloadFile().execute();

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(title)
                    .setContentText(getString(R.string.downloading))
                    .setSmallIcon(android.R.drawable.stat_sys_download);

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

                            mBuilder.setContentText(getString(R.string.downloading_ended))

                                    .setProgress(0, 0, false)
                                    .setSmallIcon(android.R.drawable.stat_sys_download_done);
                            mNotifyManager.notify(notificationID, mBuilder.build());
                        }
                    }

            ).start();
        } else if (intent.getAction() == ACTION_ADD_TO_FAVORITES) {
            id = intent.getIntExtra(ID_DOWNLOAD,-1);
            new addToFavorites().execute();
        }
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
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/" + title + ".mp3");

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
            SoundLab.mUser.addDownloadedDound(id);
            sendEndDownloading();
            saveObject(SoundLab.mUser);
            mNotifyManager.cancel(notificationID);
            stopSelf();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            incr = values[0];
            super.onProgressUpdate(values);
        }
    }

    public String getSizeFileMB(double lenght) {
        double fileSizeInMB = lenght / (1024.0 * 1024.0);
        if (String.valueOf(fileSizeInMB).length() >= 6)
            return String.valueOf(fileSizeInMB).substring(0, 4);
        else return String.valueOf(fileSizeInMB);
    }

    private class addToFavorites extends AsyncTask<String, Integer, File> {
        @Override
        protected File doInBackground(String... urlParams) {
            int count;
            try {
                URL url = new URL(urdDownloading);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                byte data[] = new byte[1024];
                long total = 0;
                File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
                OutputStream output = new FileOutputStream(tempMp3);

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishProgress((int) (total * 100 / lenghtOfFile));
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
            if(s!=null)
            SoundLab.mUser.getSoundFavorite(id).setFile(s);
            stopSelf();

        }


    }
    private void sendEndDownloading() {
        Intent intentPlayer = new Intent(DATA_FROM_SERVICE);
        sendBroadcast(intentPlayer);
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
}