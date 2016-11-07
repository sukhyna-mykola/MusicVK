package com.sukhyna_mykola.vkmusic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mikola on 07.11.2016.
 */

class DBHelper extends SQLiteOpenHelper {
    public static final String NAME = "name";

    public DBHelper(Context context) {
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