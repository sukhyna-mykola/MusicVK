package com.sukhyna_mykola.vkmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;

public class SettingActivity extends AppCompatActivity  {
    Spinner mSpinnerSort;
    CheckBox mCheckBoxAutoComplete;
    CheckBox mCheckBoxPerformerOnly;

    public static final String SORT_TYPE_KEY = "SORT_TYPE_KEY";
    public static final String AUTO_COMPLETE_KEY = "AUTO_COMPLETE_KEY";
    public static final String PERFORMER_ONLY_KEY = "PERFORMER_ONLY_KEY";
    public static final String LOGINED_KEY = "LOGINED_KEY";


    public static final String POPULAR = "POPULAR";
    public static final String LENGHT = "LENGHT";
    public static final String DATE = "DATE";
    String[] dataSort = {DATE, LENGHT, POPULAR};


    public static int autoComplete;
    public static int performerOnly;
    public static int sortType;
    public static  boolean logined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        mCheckBoxAutoComplete = (CheckBox) findViewById(R.id.сheck_auto_complete);
        mCheckBoxPerformerOnly = (CheckBox) findViewById(R.id.сheck_performer_only);
        // адаптер
        if (autoComplete == 1)
            mCheckBoxAutoComplete.setChecked(true);
        else mCheckBoxAutoComplete.setChecked(false);
        if (performerOnly == 1)
            mCheckBoxPerformerOnly.setChecked(true);
        else
            mCheckBoxPerformerOnly.setChecked(false);


        mCheckBoxPerformerOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    performerOnly = 1;

                } else performerOnly = 0;


            }
        });
        mCheckBoxAutoComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        ed.putBoolean(LOGINED_KEY,logined);

        ed.commit();


    }


}
