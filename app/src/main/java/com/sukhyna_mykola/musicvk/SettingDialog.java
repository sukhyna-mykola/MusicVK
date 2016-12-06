package com.sukhyna_mykola.musicvk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import static android.content.Context.MODE_PRIVATE;
import static com.sukhyna_mykola.musicvk.SettingActivity.AUTO_COMPLETE_KEY;
import static com.sukhyna_mykola.musicvk.SettingActivity.PERFORMER_ONLY_KEY;
import static com.sukhyna_mykola.musicvk.SettingActivity.SORT_TYPE_KEY;
import static com.sukhyna_mykola.musicvk.SettingActivity.autoComplete;
import static com.sukhyna_mykola.musicvk.SettingActivity.performerOnly;
import static com.sukhyna_mykola.musicvk.SettingActivity.sortType;

/**
 * Created by mikola on 24.11.2016.
 */

public class SettingDialog extends DialogFragment {
    private Spinner mSpinnerSort;
    private SwitchCompat mSwitchAutoComplete;
    private SwitchCompat mSwitchPerformerOnly;
    private String[] dataSort;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.setting_search_menu,null);
        dataSort = getResources().getStringArray(R.array.list_of_type_sort);
        mSwitchAutoComplete = (SwitchCompat) v.findViewById(R.id.switch_auto_complete);
        mSwitchPerformerOnly = (SwitchCompat) v.findViewById(R.id.switch_performer_only);
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
        mSpinnerSort = (Spinner) v.findViewById(R.id.spinner_sort);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataSort);
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
        return new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.pref_search))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePreference();
                    }
                })
                .setView(v)
                .create();
    }

    private void savePreference() {
        SharedPreferences sPref = getActivity().getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putInt(SORT_TYPE_KEY, sortType);
        ed.putInt(AUTO_COMPLETE_KEY, autoComplete);
        ed.putInt(PERFORMER_ONLY_KEY, performerOnly);


        ed.commit();

    }

}
