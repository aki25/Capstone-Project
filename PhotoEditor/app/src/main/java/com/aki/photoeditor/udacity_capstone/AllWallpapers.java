package com.aki.photoeditor.udacity_capstone;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.aki.photoeditor.udacity_capstone.data.WallpaperContract;
import com.aki.photoeditor.udacity_capstone.data.WallpaperDbHelper;

public class AllWallpapers extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int WALLPAPER_LOADER_ID = 7;
    private WallpaperAdapter adapter;
    Cursor todoCursor;
    public static String wallPaperLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_wallpapers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportLoaderManager().initLoader(WALLPAPER_LOADER_ID,null,this);
        GridView gridView = (GridView)findViewById(R.id.wallpaperGrid);

        // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
        WallpaperDbHelper handler = new WallpaperDbHelper(this);
// Get access to the underlying writeable database
        SQLiteDatabase db = handler.getWritableDatabase();
// Query for items from the database and get a cursor back
        todoCursor = db.rawQuery("SELECT  * FROM "+WallpaperContract.WallpaperEntry.TABLE_NAME, null);
        adapter = new WallpaperAdapter(getApplicationContext(),todoCursor);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                wallPaperLocation = (String) adapter.getItem(position);
                Intent manage = new Intent(AllWallpapers.this,ManageWallpaper.class);
                startActivity(manage);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,WallpaperContract.WallpaperEntry.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        todoCursor = data;
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
