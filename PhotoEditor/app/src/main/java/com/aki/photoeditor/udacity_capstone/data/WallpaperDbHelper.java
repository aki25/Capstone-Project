package com.aki.photoeditor.udacity_capstone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class WallpaperDbHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "wallpaper.db";

    public WallpaperDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_WALLPAPER_TABLE = "CREATE TABLE " + WallpaperContract.WallpaperEntry.TABLE_NAME + " (" +
                WallpaperContract.WallpaperEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH + " TEXT NOT NULL " +
                "ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_WALLPAPER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + WallpaperContract.WallpaperEntry.TABLE_NAME);
        onCreate(db);
    }
}
