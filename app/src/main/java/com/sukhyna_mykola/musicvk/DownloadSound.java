package com.sukhyna_mykola.musicvk;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;

import static com.sukhyna_mykola.musicvk.LikeService.DOWNLOADED;
import static com.sukhyna_mykola.musicvk.MusicService.DATA_FROM_SERVICE;
import static com.sukhyna_mykola.musicvk.MusicService.PARAM_TYPE;

/**
 * Created by mikola on 14.11.2016.
 */

public class DownloadSound {

    private DownloadManager mDownloadManager;
    private long myDownloadRefference;
    private BroadcastReceiver receiverDownloadComplete;

    private Context mContext;

    private String downloadURL;
    private int id;

    public DownloadSound(Context context, final Sound sound) {


        Toast.makeText(context, context.getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();
        id = sound.id;
        downloadURL = sound.url;
        mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadURL);
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!new File(SettingActivity.FOLDER_DOWNLOAD).exists())
                try {
                    new File(SettingActivity.FOLDER_DOWNLOAD).mkdir();

                } catch (Exception e) {
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sound.title + ".mp3");
                    Toast.makeText(mContext, context.getString(R.string.change_downloads_folder) + Environment.DIRECTORY_DOWNLOADS.toString(), Toast.LENGTH_SHORT).show();
                }
            if (new File(SettingActivity.FOLDER_DOWNLOAD).exists())
                request.setDestinationInExternalPublicDir(SettingActivity.FOLDER_DOWNLOAD.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), ""), sound.title + " - " + sound.getArtist() + ".mp3");

        } else {
            return;
        }
        //обчислення розміру файла
        new Thread(new Runnable() {
            @Override
            public void run() {
                request.setDescription(mContext.getString(R.string.size) + Constants.SizeFile(downloadURL) + "Mb");
                myDownloadRefference = mDownloadManager.enqueue(request);
            }
        }).start();


        request.setTitle(sound.getTitle()+" - "+sound.getArtist())
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


        request.setVisibleInDownloadsUi(true);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);


        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadRefference == reference) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);

                    Cursor cursor = mDownloadManager.query(query);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL: {
                            SoundLab.mUser.addDownloadedDound(id);
                            Intent intentPlayer = new Intent(DATA_FROM_SERVICE);
                            intentPlayer.putExtra(PARAM_TYPE, DOWNLOADED);
                            mContext.sendBroadcast(intentPlayer);
                            SoundLab.saveObject();
                            break;
                        }

                    }
                }
            }

        };
        mContext.registerReceiver(receiverDownloadComplete, intentFilter);
    }

}



