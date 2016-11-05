package com.sukhyna_mykola.vkmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
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
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MenuItemCompat.OnActionExpandListener {
    private String[] scope = new String[]{
            VKScope.AUDIO, VKScope.FRIENDS, VKScope.PHOTOS};

    Button search;
    boolean logined;
    EditText textSearch;
    TextView nav_user;
    ImageView image_user;
    String nameUser;
    Bitmap photoUser;

    SharedPreferences sPref;
    Fragment listFragment;
    User curUser;
    public static final String LOGINED = "LOGINEG";

    void saveText() {
        sPref = getSharedPreferences("Mypref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(LOGINED, logined);
        ed.commit();

    }

    void loadText() {
        sPref = getSharedPreferences("Mypref", MODE_PRIVATE);
        logined = sPref.getBoolean(LOGINED, false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        sPref = getSharedPreferences("MyPref", MODE_PRIVATE);

       /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerControl.getAlpha()==1) {
                    fab.animate().scaleY(1f).setInterpolator(new LinearInterpolator()).start();
                    fab.animate().scaleX(1f).setInterpolator(new LinearInterpolator()).start();
                    fab.animate().translationY(0.0f).setInterpolator(new BounceInterpolator()).start();
                    fab.animate().rotation(0).setInterpolator(new BounceInterpolator()).start();
                   // playerControl.setVisibility(View.INVISIBLE);
                    playerControl.animate().alpha(0).setInterpolator(new LinearInterpolator()).start();;
                    playerControl.animate().translationY(playerControl.getHeight()).start();
                } else {
                    // fab.hide();
                    playerControl.animate().alpha(1).setInterpolator(new LinearInterpolator()).start();;

                    fab.animate().scaleY(0.75f).setInterpolator(new LinearInterpolator()).start();
                    fab.animate().scaleX(0.75f).setInterpolator(new LinearInterpolator()).start();
                    fab.animate().rotation(-180).setInterpolator(new LinearInterpolator()).start();
                    fab.animate().translationY((float) (-(float) playerControl.getHeight() * 1.1)).start();
                    playerControl.animate().translationY(0).start();
                  //  playerControl.setVisibility(View.VISIBLE);
                }
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        FragmentManager fm = getSupportFragmentManager();
        listFragment = fm.findFragmentById(R.id.list_container);
        if (listFragment == null) {
            listFragment = new SoundListFragment();
            fm.beginTransaction()
                    .add(R.id.list_container, listFragment)
                    .commit();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        nav_user = (TextView) hView.findViewById(R.id.name_user);
        image_user = (ImageView) hView.findViewById(R.id.imageView);
       /*
*/
        loadText();

        if (!logined) {
            logined = true;
            saveText();
            VKSdk.login(this, scope);
        } else {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "VKMusicPlayer");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            loadSerializedObject(new File(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/.user"));

        }

        search = (Button) findViewById(R.id.search);

        textSearch = (EditText) findViewById(R.id.text_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VKRequest request = VKApi.audio().search(VKParameters.from(VKApiConst.Q, textSearch.getText(), VKApiConst.AUTO_COMPLETE, "1", VKApiConst.COUNT, 100, VKApiConst.SORT, 0));
                setListContent(request);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                //якщо не вказано поле user_ids, то повертаэться поточний користувач
                getInfo();
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


    void getInfo() {
        final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name,last_name,photo_100"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKList<VKApiUser> user = (VKList<VKApiUser>) response.parsedModel;
                nameUser = (user.get(0).first_name + ' ' + user.get(0).last_name);
                nav_user.setText(nameUser);
                new DownloadImageTask(image_user).execute(user.get(0).photo_100);
            }
        });

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_music) {
            final VKRequest request = VKApi.audio().get();
            setListContent(request);
        } else if (id == R.id.nav_popular) {
            final VKRequest request = VKApi.audio().getPopular();
            setListContent(request);
        } else if (id == R.id.nav_recomend) {
            final VKRequest request = VKApi.audio().getRecommendations();
            setListContent(request);
        } else if (id == R.id.nav_update) {
            getInfo();

        } else if (id == R.id.nav_exit) {
            logined = false;
            saveText();
            System.exit(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ArrayList<Sound> sounds;

    private void setListContent(VKRequest request) {
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                sounds = null;
                sounds = new ArrayList<Sound>();
                VKList<VKApiAudio> list = (VKList) response.parsedModel;
                for (VKApiAudio audio : list) {
                    sounds.add(new Sound(false, (audio.duration * 1000), audio.url, audio.title, audio.artist));
                }
                SoundLab.get(MainActivity.this).addAllSound(sounds);
                ((SoundListFragment) listFragment).update();
                new SizeFile().execute();

            }
        });
    }


    public String getSizeFileMB(int lenght) {
        double fileSizeInMB = ((double) lenght / (1024.0 * 1024.0));
        return String.valueOf(fileSizeInMB).substring(0, 4);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {

        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
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
            photoUser = result;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photoUser.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            saveObject(new User(byteArray, nameUser, 0));
        }
    }

    public void saveObject(User user) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/.user"))); //Select where you wish to save the file...
            oos.writeObject(user); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        } catch (Exception ex) {

        }
    }

    public void loadSerializedObject(File f) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            curUser = (User) ois.readObject();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(curUser.getPhoto(), 0, curUser.getPhoto().length, options);
            photoUser = bmp;

            nameUser = curUser.getName();
            nav_user.setText(nameUser);
            image_user.setImageBitmap(photoUser);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private class SizeFile extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... urlParams) {
            URL url;
            URLConnection conetion;
            for (int i = 0; i < sounds.size(); i++)

            {
                try {
                    url = new URL(sounds.get(i).getUrl());
                    conetion = url.openConnection();
                    conetion.connect();
                    sounds.get(i).setSize(getSizeFileMB(conetion.getContentLength()));

                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            ((SoundListFragment) listFragment).update();
        }


    }
}
