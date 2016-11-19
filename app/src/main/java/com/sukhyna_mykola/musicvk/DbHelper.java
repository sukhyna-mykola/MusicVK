package com.sukhyna_mykola.musicvk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mikola on 07.11.2016.
 */

class DbHelper extends SQLiteOpenHelper {
    public static final String NAME = "name";

    public DbHelper(Context context) {
        super(context, Constants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + Constants.TABLE_NAME + " ( "
                + NAME + " text );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}