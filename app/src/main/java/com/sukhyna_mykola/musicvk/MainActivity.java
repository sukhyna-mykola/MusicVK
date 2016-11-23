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
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
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
import android.widget.Toast;

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
        implements NavigationView.OnNavigationItemSelectedListener {

    private ContentValues cv = new ContentValues();
    private DbHelper dbHelper;

    private Fragment listFragment;
    private TextView nameUserView;
    private SearchView searchView;
    private RoundedImageView imageUserView;
    private HTextView categoryShow;
    private TextView containerHintView;

    public static HashSet<String> soundHelp = new HashSet<>();
    private static String[] soundHelpArray;
    private ArrayList<Sound> sounds;

    public static String textCategory = "";

    public static final int MY_SOUNDS = 0;
    public static final int FAVORITE_SOUNDS = 1;
    public static final int RECOMENDET_SOUNDS = 2;
    public static final int POPULAR_SOUNDS = 3;
    public static final int SEARCH_SOUNDS = 4;
    public static final int PLAY_LIST_SOUNDS = 5;
    public static final int ERROR = 6;
    public static final int REQUEST_CODE = 14;

    public static final String USER_FOLDER = "/.user_";
    private static final String TAG = "TAG";


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
                textCategory = getString(R.string.search_result) + query;
                categoryShow.animateText(textCategory);
                if (!soundHelp.contains(query)) {
                    soundHelp.add(query);
                    cv.put(DbHelper.NAME, query);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.insert(Constants.TABLE_NAME, null, cv);
                    db.close();
                }
                final VKRequest request = VKApi.audio().search(VKParameters.from(VKApiConst.Q,
                        query, VKApiConst.AUTO_COMPLETE, SettingActivity.autoComplete,
                        VKApiConst.COUNT, 100, VKApiConst.SORT, SettingActivity.sortType, Constants.PERFORMER_ONLY, SettingActivity.performerOnly));
                setListContent(request, SEARCH_SOUNDS);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                ;
                mAdapter.getCursor().moveToPosition(position);
                int id = mAdapter.getCursor().getColumnIndex(BaseColumns._ID);
                searchView.setQuery(soundHelpArray[mAdapter.getCursor().getInt(id)], true);
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }
        });

        return true;

    }


    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DbHelper(this);
        final String[] from = new String[]{"fild"};
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

        containerHintView = (TextView) findViewById(R.id.container_hint);
        containerHintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        File folder = new File(SettingActivity.FOLDER_DOWNLOAD_DEFAULT);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        //Log.d("finger", "finger " + Arrays.asList(fingerprints));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        nameUserView = (TextView) hView.findViewById(R.id.name_user);
        imageUserView = (RoundedImageView) hView.findViewById(R.id.imageView);

        if (SoundLab.mUser != null) {
            setHintBackGround(false, PLAY_LIST_SOUNDS);
        } else {
            textCategory = getResources().getString(R.string.current_play_list);
        }
        loadSetting();

        folder = new File(SettingActivity.FOLDER_DOWNLOAD);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!SettingActivity.logined) {
            Intent startActivity = new Intent(this, StartActivity.class);
            startActivityForResult(startActivity, REQUEST_CODE);
        } else {
            loadUser(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + USER_FOLDER + SettingActivity.id));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        categoryShow.animateText(textCategory);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            nameUserView.setText(SoundLab.mUser.getName().replace(" ", "\n"));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(SoundLab.mUser.getPhoto(), 0, SoundLab.mUser.getPhoto().length, options);
            imageUserView.setImageBitmap(bmp);
        }
        if (resultCode == RESULT_CANCELED) {
            finish();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_current_play_list) {
            if (SoundLab.get().getCurentPlayList() != null) {
                setHintBackGround(false, PLAY_LIST_SOUNDS);
                SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.get().getCurentPlayList());
            } else {
                setHintBackGround(true, PLAY_LIST_SOUNDS);
                SoundLab.get().addAllSound(new ArrayList<Sound>());
            }
            ((SoundListFragment) listFragment).update();
            textCategory = getResources().getString(R.string.current_play_list);
            categoryShow.animateText(textCategory);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_music) {
            textCategory = getResources().getString(R.string.my_music);
            categoryShow.animateText(textCategory);
            final VKRequest request = VKApi.audio().get();
            setListContent(request, MY_SOUNDS);
        } else if (id == R.id.nav_popular) {
            textCategory = getResources().getString(R.string.popular);
            categoryShow.animateText(textCategory);
            final VKRequest request = VKApi.audio().getPopular();
            setListContent(request, POPULAR_SOUNDS);
        } else if (id == R.id.nav_recomend) {
            textCategory = getResources().getString(R.string.recomend);
            categoryShow.animateText(textCategory);
            final VKRequest request = VKApi.audio().getRecommendations();
            setListContent(request, RECOMENDET_SOUNDS);
        } else if (id == R.id.nav_exit) {
            SettingActivity.logined = false;
            saveStatusLogined();
            Intent startActivity = new Intent(this, StartActivity.class);
            startActivityForResult(startActivity, REQUEST_CODE);
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (id == R.id.nav_favorites) {
            textCategory = getResources().getString(R.string.like);
            categoryShow.animateText(textCategory);
            if (SoundLab.mUser.favoritesSounds.size() == 0)
                setHintBackGround(true, FAVORITE_SOUNDS);
            else setHintBackGround(false, FAVORITE_SOUNDS);
            SoundLab.get().addAllSound((ArrayList<Sound>) SoundLab.mUser.favoritesSounds);
            ((SoundListFragment) listFragment).update();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setHintBackGround(boolean state, int type) {
        if (!state) {
            containerHintView.setVisibility(View.GONE);
        } else
            switch (type) {
                case MY_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText(R.string.nothing_music);
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_active_search);
                    break;
                }
                case FAVORITE_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText(R.string.nothing_save);
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_favorite_black_24dp);
                    break;
                }
                case POPULAR_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText("");
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_sad_face);
                    break;
                }
                case RECOMENDET_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText("");
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_sad_face);
                    ;
                    break;
                }
                case PLAY_LIST_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText(R.string.empty_playlist);
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_queue_music_black_128dp);
                    break;
                }
                case SEARCH_SOUNDS: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText(R.string.nothing_search);
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_sad_face);
                    break;
                }
                case ERROR: {
                    containerHintView.setVisibility(View.VISIBLE);
                    containerHintView.setText(R.string.eror);
                    containerHintView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_sad_face);
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
                Log.d(TAG, "onComplete");
                VKList<VKApiAudio> list = (VKList) response.parsedModel;

                for (VKApiAudio audio : list) {
                    sounds.add(new Sound(audio));

                }
                SoundLab.get().addAllSound(sounds);

                if (sounds.size() == 0) {
                    setHintBackGround(true, type);
                } else {
                    setHintBackGround(false, type);
                }
                ((SoundListFragment) listFragment).update();

            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                containerHintView.setVisibility(View.VISIBLE);
                Log.d(TAG, (double) bytesLoaded / (double) bytesTotal * 100 + "%");
                containerHintView.setText((double) bytesLoaded / (double) bytesTotal * 100 + "%");
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                setHintBackGround(true, ERROR);
                SoundLab.get().addAllSound(new ArrayList<Sound>());
                ((SoundListFragment) listFragment).update();
            }
        });
    }


    private void saveStatusLogined() {
        SharedPreferences sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(SettingActivity.LOGINED_KEY, SettingActivity.logined);
        ed.putInt(SettingActivity.ID_KEY, SettingActivity.id);
        ed.commit();
    }

    private void loadSetting() {
        SharedPreferences sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SettingActivity.isRandom = sPref.getBoolean(SettingActivity.RANDOM_KEY, false);
        SettingActivity.isLooping = sPref.getBoolean(SettingActivity.LOOPING_KEY, false);
        SettingActivity.sortType = sPref.getInt(SettingActivity.SORT_TYPE_KEY, 0);
        SettingActivity.performerOnly = sPref.getInt(SettingActivity.PERFORMER_ONLY_KEY, 0);
        SettingActivity.autoComplete = sPref.getInt(SettingActivity.AUTO_COMPLETE_KEY, 0);
        SettingActivity.logined = sPref.getBoolean(SettingActivity.LOGINED_KEY, false);
        SettingActivity.id = sPref.getInt(SettingActivity.ID_KEY, 0);
        SettingActivity.FOLDER_DOWNLOAD = sPref.getString(SettingActivity.FOLDER_KEY, SettingActivity.FOLDER_DOWNLOAD_DEFAULT);


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Constants.TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int nameColIndex = c.getColumnIndex(DbHelper.NAME);
            do {
                soundHelp.add(c.getString(nameColIndex));
            } while (c.moveToNext());
        } else
            c.close();
        db.close();
    }

    public void loadUser(File f) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));

            SoundLab.mUser = (User) ois.readObject();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(SoundLab.mUser.getPhoto(), 0, SoundLab.mUser.getPhoto().length, options);
            imageUserView.setImageBitmap(bmp);
            nameUserView.setText(SoundLab.mUser.getName().replace(" ", "\n"));

            final VKRequest request = VKApi.audio().get();

            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    VKList<VKApiAudio> list = (VKList) response.parsedModel;
                    SoundLab.mUser.myMusic.clear();
                    for (VKApiAudio audio : list) {
                        SoundLab.mUser.addMyMusic(new Sound(audio));
                    }
                }
            });

        } catch (Exception ex) {
            SoundLab.mUser = new User();
            Toast.makeText(this, R.string.user_load_failed, Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }

    }

    private void populateAdapter(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "fild"});
        soundHelpArray = soundHelp.toArray(new String[soundHelp.size()]);
        for (int i = 0; i < soundHelpArray.length; i++) {
            if (soundHelpArray[i].toLowerCase().startsWith(query.toLowerCase())) {
                c.addRow(new Object[]{i, soundHelpArray[i]});
            }
        }
        mAdapter.changeCursor(c);
    }

}
