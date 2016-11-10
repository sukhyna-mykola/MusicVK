package com.sukhyna_mykola.musicvk;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.hanks.htextview.HTextView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static com.sukhyna_mykola.musicvk.PlayerActivity.PLAY_LIST;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MenuItemCompat.OnActionExpandListener {
    private String[] scope = new String[]{VKScope.AUDIO, VKScope.FRIENDS, VKScope.PHOTOS};

    ContentValues cv = new ContentValues();
    DBHelper dbHelper;
    TextView nav_user;
    ImageView image_user;
    HTextView categoryShow;
    public static HashSet<String> soundHelp = new HashSet<>();
    String nameUser;
    Bitmap photoUser;
    TextView containerHint;

    public  static final int MY_SOUNDS = 0;
    public static final  int FAVORITE_SOUNDS = 1;
    public static final int RECOMENDET_SOUNDS = 2;
    public static final int POPULAR_SOUNDS = 3;
    public static final int SEARCH_SOUNDS = 4;
    public static final int PLAY_LIST_SOUNDS = 5;
    public static final int ERROR = 6;
    SharedPreferences sPref;
    Fragment listFragment;

    private static final String TAG = "TAG";


    void saveStatusLogined() {
        sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(SettingActivity.LOGINED_KEY, SettingActivity.logined);
        ed.commit();

    }

    void loadSetting() {
        sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SettingActivity.isRandom = sPref.getBoolean(SettingActivity.RANDOM_KEY,false);
        SettingActivity.isLooping = sPref.getBoolean(SettingActivity.LOOPING_KEY,false);
        SettingActivity.sortType = sPref.getInt(SettingActivity.SORT_TYPE_KEY, 0);
        SettingActivity.performerOnly = sPref.getInt(SettingActivity.PERFORMER_ONLY_KEY, 0);
        SettingActivity.autoComplete = sPref.getInt(SettingActivity.AUTO_COMPLETE_KEY, 0);
        SettingActivity.logined = sPref.getBoolean(SettingActivity.LOGINED_KEY, false);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Constants.TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex(DBHelper.NAME);
            do {
                soundHelp.add(c.getString(nameColIndex));
            } while (c.moveToNext());
        } else
            c.close();
        db.close();
    }


    private static String[] SUGGESTIONS;


    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSuggestionsAdapter(mAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                populateAdapter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                categoryShow.animateText("Результаты поиска - "+query);
                if (!soundHelp.contains(query)) {
                    soundHelp.add(query);
                    cv.put(DBHelper.NAME, query);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.insert(Constants.TABLE_NAME, null, cv);
                    db.close();
                }
                final VKRequest request = VKApi.audio().search(VKParameters.from(VKApiConst.Q,
                        query, VKApiConst.AUTO_COMPLETE, SettingActivity.autoComplete,
                        VKApiConst.COUNT, 100, VKApiConst.SORT, SettingActivity.sortType, Constants.PERFORMER_ONLY, SettingActivity.performerOnly));
                setListContent(request,SEARCH_SOUNDS);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                ;
                mAdapter.getCursor().moveToPosition(position);
                int id = mAdapter.getCursor().getColumnIndex(BaseColumns._ID);
                searchView.setQuery(SUGGESTIONS[mAdapter.getCursor().getInt(id)], true);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        return true;

    }

    private void populateAdapter(String query) {

        final MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "cityName"});

        SUGGESTIONS = soundHelp.toArray(new String[soundHelp.size()]);

        for (int i = 0; i < SUGGESTIONS.length; i++) {
            if (SUGGESTIONS[i].toLowerCase().startsWith(query.toLowerCase())) {
                c.addRow(new Object[]{i, SUGGESTIONS[i]});

            }
        }
        mAdapter.changeCursor(c);
    }


    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        dbHelper = new DBHelper(this);
        final String[] from = new String[]{"cityName"};
        final int[] to = new int[]{R.id.help_sounds_id};
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.help_sound_item,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        categoryShow = (HTextView) findViewById(R.id.category_show);

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

        containerHint = (TextView) findViewById(R.id.container_hint);
        containerHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("finger","finger "+Arrays.asList(fingerprints));
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        nav_user = (TextView) hView.findViewById(R.id.name_user);
        image_user = (ImageView) hView.findViewById(R.id.imageView);

        loadSetting();
        if (!SettingActivity.logined) {

            VKSdk.login(this, scope);
        } else {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "VKMusicPlayer");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            loadSerializedObject(new File(Environment.getExternalStorageDirectory() + "/VKMusicPlayer/.user"));
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {

                getInfo();
                SettingActivity.logined = true;
                saveStatusLogined();
            }

            @Override
            public void onError(VKError error) {
                SettingActivity.logined = false;
                saveStatusLogined();
                finish();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_current_play_list) {
            if(SoundLab.get().getCurentPlayList()!=null){
                setHintBackGround(false,PLAY_LIST_SOUNDS);
                SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.get().getCurentPlayList());
            }
            else {setHintBackGround(true,PLAY_LIST_SOUNDS);
            SoundLab.get().addAllSound(new ArrayList<Sound>());
           }
            ((SoundListFragment) listFragment).update();
            categoryShow.animateText(getResources().getString(R.string.current_play_list));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_music) {
            categoryShow.animateText(getResources().getString(R.string.my_music));
            final VKRequest request = VKApi.audio().get();
            setListContent(request,MY_SOUNDS);
        } else if (id == R.id.nav_popular) {
            categoryShow.animateText(getResources().getString(R.string.popular));
            final VKRequest request = VKApi.audio().getPopular();
            setListContent(request,POPULAR_SOUNDS);
        } else if (id == R.id.nav_recomend) {
            categoryShow.animateText(getResources().getString(R.string.recomend));
            final VKRequest request = VKApi.audio().getRecommendations();
            setListContent(request,RECOMENDET_SOUNDS);
        } else if (id == R.id.nav_exit) {
            SettingActivity.logined = false;
            saveStatusLogined();
            System.exit(0);
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        }
        else if (id == R.id.nav_favorites) {
            categoryShow.animateText(getResources().getString(R.string.like));
            if( SoundLab.mUser.favoritesSounds.size()==0)
                setHintBackGround(true,FAVORITE_SOUNDS);
            else  setHintBackGround(false,FAVORITE_SOUNDS);
            SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.mUser.favoritesSounds);

            ((SoundListFragment) listFragment).update();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ArrayList<Sound> sounds;

    private void setHintBackGround(boolean state,int type){
        if(!state){
            containerHint.setVisibility(View.GONE);
        }else
        switch (type){
            case  MY_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("У вас еще нет аудиозаписей");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_music_video_black_128dp);
                break;
            }
            case  FAVORITE_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("У вас нет сохраненных");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_favorite_black_24dp);
                break;
            }
            case  POPULAR_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("..........");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_people_black_24dp);
                break;
            }
            case  RECOMENDET_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("У вас нет рекомендованых");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0,  R.drawable.ic_mood_bad_black_24dp);;
                break;
            }
            case  PLAY_LIST_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("Плейлист пустой");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_queue_music_black_128dp);
                break;
            }
            case  SEARCH_SOUNDS :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("Нечего не найдено( ");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_sad_face);
                break;
            }
            case  ERROR :{
                containerHint.setVisibility(View.VISIBLE);
                containerHint.setText("ОШИБКА");
                containerHint.setCompoundDrawablesWithIntrinsicBounds( 0, 0, 0, R.drawable.ic_sad_face);
                break;
            }
        }
    }

    private void setListContent(VKRequest request, final int type) {
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                sounds = new ArrayList<Sound>();
                Log.d(TAG,"onComplete");
                VKList<VKApiAudio> list = (VKList) response.parsedModel;

                for (VKApiAudio audio : list) {
                    sounds.add(new Sound(audio));

                }
                SoundLab.get().addAllSound(sounds);

                if (sounds.size() == 0) {
                    setHintBackGround(true,type);
                } else {
                    setHintBackGround(false,type);

                }


                ((SoundListFragment) listFragment).update();
               // new SizeFile().execute();

            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                containerHint.setVisibility(View.VISIBLE);
                Log.d(TAG,(double)bytesLoaded/(double) bytesTotal*100+"%");
                containerHint.setText((double)bytesLoaded/(double) bytesTotal*100+"%");
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                setHintBackGround(true,ERROR);
                SoundLab.get().addAllSound(new ArrayList<Sound>());
                ((SoundListFragment) listFragment).update();
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
            SoundLab.mUser = new User(byteArray, nameUser, 0);
            saveObject(SoundLab.mUser);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
            SoundLab.mUser = (User) ois.readObject();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(SoundLab.mUser.getPhoto(), 0, SoundLab.mUser.getPhoto().length, options);
            photoUser = bmp;

            nameUser = SoundLab.mUser.getName();
            nav_user.setText(nameUser);
            image_user.setImageBitmap(photoUser);
            final VKRequest request = VKApi.audio().get();

            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    sounds = new ArrayList<Sound>();

                    VKList<VKApiAudio> list = (VKList) response.parsedModel;
                    for (VKApiAudio audio : list) {
                        sounds.add(new Sound(audio));
                    }

                }
            });
            SoundLab.mUser.setMyMusic(sounds);
            categoryShow.animateText("Привет "+nameUser);
            if (SoundLab.get().getCurentPlayList().equals(SoundLab.get().getSounds()))
                categoryShow.animateText(getResources().getString(R.string.current_play_list));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private class SizeFile extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... urlParams) {
            URL url;
            URLConnection conetion;
            for (int i = 0; i < sounds.size(); i++) {
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
