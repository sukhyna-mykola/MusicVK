package com.sukhyna_mykola.musicvk;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.*;
import com.vk.sdk.BuildConfig;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Date;

import static com.sukhyna_mykola.musicvk.MainActivity.USER_FOLDER;
import static com.sukhyna_mykola.musicvk.MainActivity.date;


public class StartActivity extends Activity implements View.OnClickListener {

    public static final int PERMISSION_REQUEST_CODE = 1;

    private String nameUser;
    private int idUser;
    private Bitmap photoUser;

    private ProgressBar wait;
    private Button enter;
    private TextView info;


    private String[] scope = new String[]{VKScope.AUDIO, VKScope.FRIENDS, VKScope.PHOTOS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        wait = (ProgressBar) findViewById(R.id.progres_wait_start_activity);
        enter = (Button) findViewById(R.id.enter_start_activity);
        info = (TextView) findViewById(R.id.start_activity_info);
        info.setText("version : " + com.sukhyna_mykola.musicvk.BuildConfig.VERSION_NAME.toString());

    }

    @Override
    public void onClick(View v) {
        checkPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                getInfo();
            }

            @Override
            public void onError(VKError error) {
                SettingActivity.logined = false;
                saveStatusLogined();
                setResult(RESULT_CANCELED);
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void getInfo() {
        final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name,last_name,photo_200"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList<VKApiUser> user = (VKList<VKApiUser>) response.parsedModel;

                nameUser = user.get(0).first_name + ' ' + user.get(0).last_name;
                idUser = user.get(0).getId();
                SettingActivity.id =idUser;
                SettingActivity.logined = true;

                saveStatusLogined();
                File userTmp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + USER_FOLDER + idUser);
                if (userTmp.exists())
                    loadUser(userTmp);
                else
                    new DownloadImageTask().execute(user.get(0).photo_200);
            }
        });

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            photoUser = result;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photoUser.compress(Bitmap.CompressFormat.PNG, 100, stream);

            byte[] byteArray = stream.toByteArray();
            SoundLab.mUser = new User(byteArray, nameUser, idUser);
            if(!date) {
                final VKRequest request = VKApi.audio().get();

                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        VKList<VKApiAudio> list = (VKList) response.parsedModel;
                        for (VKApiAudio audio : list) {
                            SoundLab.mUser.addMyMusic(new Sound(audio));
                        }
                        SoundLab.saveObject();
                        setResult(RESULT_OK);
                        finish();
                    }
                });

            } else Toast.makeText(StartActivity.this, getResources().getString(R.string.eror), Toast.LENGTH_SHORT).show();
        }
    }

    public void loadUser(File f) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));

            SoundLab.mUser = (User) ois.readObject();
             if(!date) {
                 final VKRequest request = VKApi.audio().get();

                 request.executeWithListener(new VKRequest.VKRequestListener() {
                     @Override
                     public void onComplete(VKResponse response) {
                         super.onComplete(response);
                         VKList<VKApiAudio> list = (VKList) response.parsedModel;
                         for (VKApiAudio audio : list) {
                             SoundLab.mUser.addMyMusic(new Sound(audio));
                         }
                         setResult(RESULT_OK);
                         finish();
                     }
                 });

             }
        } catch (Exception ex) {
            SoundLab.mUser = new User();
            Toast.makeText(this, R.string.user_load_failed, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            ex.printStackTrace();
        }

    }

    private void saveStatusLogined() {
        SharedPreferences sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(SettingActivity.LOGINED_KEY, SettingActivity.logined);
        ed.putInt(SettingActivity.ID_KEY, SettingActivity.id);
        ed.commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                VKSdk.login(this, scope);
                wait.setVisibility(View.VISIBLE);
                enter.setEnabled(false);
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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


    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    @Nullable
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
               showNoStoragePermissionSnackbar(enter);
            else {
                requestMultiplePermissions();
            }
        } else {
            VKSdk.login(this, scope);
            wait.setVisibility(View.VISIBLE);
            enter.setEnabled(false);
        }

    }

}
