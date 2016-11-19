package com.sukhyna_mykola.musicvk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.security.cert.CertPath;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private Spinner mSpinnerSort;
    private SwitchCompat mSwitchAutoComplete;
    private SwitchCompat mSwitchPerformerOnly;

    public static final String SORT_TYPE_KEY = "SORT_TYPE_KEY";
    public static final String AUTO_COMPLETE_KEY = "AUTO_COMPLETE_KEY";
    public static final String PERFORMER_ONLY_KEY = "PERFORMER_ONLY_KEY";
    public static final String LOGINED_KEY = "LOGINED_KEY";
    public static final String RANDOM_KEY = "RANDOM_KEY";
    public static final String LOOPING_KEY = "LOOPING_KEY";
    public static final String ID_KEY = "ID_KEY";
    public static final String FOLDER_KEY = "FOLDER_KEY";

    private static final int RESULT_CODE = 14;

    private String[] dataSort;

    public static int autoComplete;
    public static int performerOnly;
    public static int sortType;
    public static int id;
    public static boolean logined;
    public static boolean isLooping;
    public static boolean isRandom;

    private Button selectFolderDownload;
    private Button clearHistoryDownload;

    public static final String FOLDER_DOWNLOAD_DEFAULT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VKMusicPlayer";
    public static String FOLDER_DOWNLOAD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setTitle(R.string.setting);
        dataSort = getResources().getStringArray(R.array.list_of_type_sort);
        mSwitchAutoComplete = (SwitchCompat) findViewById(R.id.switch_auto_complete);
        mSwitchPerformerOnly = (SwitchCompat) findViewById(R.id.switch_performer_only);
        // адаптер
        if (autoComplete == 1)
            mSwitchAutoComplete.setChecked(true);
        else mSwitchAutoComplete.setChecked(false);
        if (performerOnly == 1)
            mSwitchPerformerOnly.setChecked(true);
        else
            mSwitchPerformerOnly.setChecked(false);

        selectFolderDownload = (Button) findViewById(R.id.select_folder);
        selectFolderDownload.setText(FOLDER_DOWNLOAD);
        clearHistoryDownload = (Button) findViewById(R.id.clear_download_history);
        if (SoundLab.mUser != null) {
            if (SoundLab.mUser.getDownloadedSounds().size() != 0)
                clearHistoryDownload.setEnabled(true);
            else clearHistoryDownload.setEnabled(false);
        }
        mSwitchPerformerOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    performerOnly = 1;

                } else performerOnly = 0;

            }
        });
        mSwitchAutoComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autoComplete = 1;

                } else autoComplete = 0;

            }
        });
        mSpinnerSort = (Spinner) findViewById(R.id.spinner_sort);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataSort);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSort.setAdapter(adapter);
        mSpinnerSort.setPrompt(dataSort[sortType]);
        mSpinnerSort.setSelection(sortType);
        mSpinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sortType = 0;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreference();
    }

    private void savePreference() {
        SharedPreferences sPref = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putInt(SORT_TYPE_KEY, sortType);
        ed.putInt(AUTO_COMPLETE_KEY, autoComplete);
        ed.putInt(PERFORMER_ONLY_KEY, performerOnly);
        ed.putBoolean(LOGINED_KEY, logined);
        ed.putInt(ID_KEY, id);
        ed.putString(FOLDER_KEY, FOLDER_DOWNLOAD);

        ed.commit();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_history: {

                MainActivity.soundHelp.clear();
                SQLiteDatabase db = new DbHelper(SettingActivity.this).getWritableDatabase();
                db.delete(Constants.TABLE_NAME, null, null);
                Snackbar.make(v, R.string.cleared_histoty, Snackbar.LENGTH_SHORT).show();
                v.setEnabled(false);
                break;
            }
            case R.id.select_folder: {
                final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

                final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                        .newDirectoryName("DirChooserSample")
                        .allowReadOnlyDirectory(true)
                        .allowNewDirectoryNameModification(true)
                        .build();

                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

// REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
                startActivityForResult(chooserIntent, RESULT_CODE);
                break;
            }
            case R.id.clear_download_history:{
                SoundLab.mUser.clearHistoryDownload();
                clearHistoryDownload.setEnabled(false);
                break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CODE) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                FOLDER_DOWNLOAD = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                selectFolderDownload.setText(FOLDER_DOWNLOAD);

            } else {
                // Nothing selected
            }
        }
    }


}
