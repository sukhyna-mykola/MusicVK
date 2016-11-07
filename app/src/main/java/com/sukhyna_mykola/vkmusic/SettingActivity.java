package com.sukhyna_mykola.vkmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.HashMap;
import java.util.List;

public class SettingActivity extends AppCompatActivity  implements View.OnClickListener{
    Spinner mSpinnerSort;
    SwitchCompat mSwitchAutoComplete;
    SwitchCompat mSwitchPerformerOnly;

    public static final String SORT_TYPE_KEY = "SORT_TYPE_KEY";
    public static final String AUTO_COMPLETE_KEY = "AUTO_COMPLETE_KEY";
    public static final String PERFORMER_ONLY_KEY = "PERFORMER_ONLY_KEY";
    public static final String LOGINED_KEY = "LOGINED_KEY";

    String[] dataSort ;


    public static int autoComplete;
    public static int performerOnly;
    public static int sortType;
    public static  boolean logined;
    public static  boolean isLooping;
    public static  boolean isRandom;

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
                Snackbar.make(view,getResources().getString(R.string.sort_by)+": " +dataSort[sortType],Snackbar.LENGTH_SHORT).show();
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
        ed.putBoolean(LOGINED_KEY,logined);

        ed.commit();


    }


    @Override
    public void onClick(View v) {
        MainActivity.soundHelp.clear();
        SQLiteDatabase db = new DBHelper(SettingActivity.this).getWritableDatabase();
        db.delete(Constants.TABLE_NAME,null,null);
        Snackbar.make(v, R.string.cleared_histoty,Snackbar.LENGTH_SHORT).show();
    }
}
