package com.sukhyna_mykola.vkmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.util.VKUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MediaPlayer.OnPreparedListener {
    private String[] scope = new String[]{
            VKScope.AUDIO, VKScope.FRIENDS, VKScope.PHOTOS};
    ListView audioList;
    VKList<VKApiAudio> list;
    Button search;
    boolean logined;
    Button playBtn;
    Button hideBtn;
    SeekBar progresMusic;
    MediaPlayer mediaPlayer;
    EditText textSearch;
    TextView nav_user;
    ImageView image_user;
    FrameLayout playerControl;
    TextView nameMusic;
    TextView timeMusic;
    SharedPreferences sPref;
    public static final String LOGINED="LOGINEG";

    void saveText() {
        sPref = getSharedPreferences("Mypref",MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(LOGINED, logined);
        ed.commit();

    }

    void loadText() {
        sPref = getSharedPreferences("Mypref",MODE_PRIVATE);
        logined = sPref.getBoolean(LOGINED, false);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerControl.getVisibility() == View.VISIBLE) {

                    playerControl.setVisibility(View.GONE);
                } else {
                    fab.hide();
                    playerControl.setVisibility(View.VISIBLE);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        nav_user = (TextView) hView.findViewById(R.id.name_user);
        image_user = (ImageView) hView.findViewById(R.id.imageView);
        playerControl = (FrameLayout) findViewById(R.id.player_layout);
        playBtn = (Button) findViewById(R.id.button_play);
        progresMusic = (SeekBar) findViewById(R.id.seekBar);
        hideBtn = (Button) findViewById(R.id.button_hide);
        nameMusic = (TextView) findViewById(R.id.name_music);
        timeMusic = (TextView) findViewById(R.id.time_music);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.show();
                playerControl.setVisibility(View.GONE);
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    if (mediaPlayer.isPlaying()) {
                        playBtn.setText("Play");
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                        playBtn.setText("Pause");
                    }
            }
        });

         loadText();
        if(!logined){
        VKSdk.login(this, scope);
        logined = true;
            saveText();
        }
        search = (Button) findViewById(R.id.search);
        audioList = (ListView) findViewById(R.id.lv);
        textSearch = (EditText) findViewById(R.id.text_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VKRequest request = VKApi.audio().search(VKParameters.from(VKApiConst.Q, textSearch.getText(), VKApiConst.AUTO_COMPLETE, "1", VKApiConst.COUNT, 100));
                setListContent(request);
            }
        });

        audioList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                releaseMP();
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(list.get(position).url);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setOnPreparedListener(MainActivity.this);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                nameMusic.setText(" " + list.get(position).title + " - " + list.get(position).artist + " ");
                playBtn.setText("Pause");
            }
        });

        final Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mediaPlayer != null) {

                    int mCurrentPosition = mediaPlayer.getCurrentPosition();

                    progresMusic.setProgress(mCurrentPosition);
                    timeMusic.setText(getTimeString(mediaPlayer.getCurrentPosition()) + " / " + getTimeString(mediaPlayer.getDuration()));
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        progresMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                //якщо не вказано поле user_ids, то повертаэться поточний користувач
                final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name,last_name,photo_100"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKList<VKApiUser> user = (VKList<VKApiUser>) response.parsedModel;
                        nav_user.setText(user.get(0).first_name + ' ' + user.get(0).last_name);
                        new DownloadImageTask(image_user)
                                .execute(user.get(0).photo_100);
                    }
                });

            }

            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_music) {
            final VKRequest request = VKApi.audio().get();
            setListContent(request);
            // Handle the camera action
        } else if (id == R.id.nav_popular) {

            final VKRequest request = VKApi.audio().getPopular();
            setListContent(request);

        } else if (id == R.id.nav_recomend) {
            final VKRequest request = VKApi.audio().getRecommendations();
            setListContent(request);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setListContent(VKRequest request) {
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                list = (VKList) response.parsedModel;
                ArrayList<String> audioName = new ArrayList<String>();
                for (VKApiAudio audio : list) {
                    audioName.add(audio.title + " - " + audio.artist);

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, audioName);
                audioList.setAdapter(adapter);
            }
        });
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        progresMusic.setMax(mediaPlayer.getDuration());
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

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
            bmImage.setImageBitmap(result);
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

}
