package com.aki.photoeditor.udacity_capstone.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class WallpaperProvider extends ContentProvider {

    private static final int WALLPAPERS = 100;
    private static final int SINGLE_WALLPAPER_ID = 101;
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private WallpaperDbHelper wallpaperDatabase = null;

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //"com.aki.photoeditor.udacity_capstone"
        final String authority = WallpaperContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority,WallpaperContract.PATH_WALLPAPER,WALLPAPERS);
        uriMatcher.addURI(authority,WallpaperContract.PATH_WALLPAPER + "/#",SINGLE_WALLPAPER_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        wallpaperDatabase = new WallpaperDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)){
            case WALLPAPERS:
                retCursor = wallpaperDatabase.getReadableDatabase().query(WallpaperContract.WallpaperEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SINGLE_WALLPAPER_ID:
                String id = uri.getPathSegments().get(1);
                selection = WallpaperContract.WallpaperEntry._ID;
                selectionArgs = new String[]{id};
                retCursor = wallpaperDatabase.getReadableDatabase().query(WallpaperContract.WallpaperEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case WALLPAPERS:
                return WallpaperContract.WallpaperEntry.CONTENT_TYPE;
            case SINGLE_WALLPAPER_ID:
                return WallpaperContract.WallpaperEntry.CONTENT_ITEM_TYPE;
            default:
                throw  new UnsupportedOperationException("Unknown Uri: "+ uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = wallpaperDatabase.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case WALLPAPERS: {
                long _id = db.insert(WallpaperContract.WallpaperEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WallpaperContract.WallpaperEntry.buildWallpaperUriWithID(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = wallpaperDatabase.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection) selection = "1";
        switch (match) {
            case WALLPAPERS:
                rowsDeleted = db.delete(WallpaperContract.WallpaperEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = wallpaperDatabase.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case WALLPAPERS:
                rowsUpdated = db.update(WallpaperContract.WallpaperEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
