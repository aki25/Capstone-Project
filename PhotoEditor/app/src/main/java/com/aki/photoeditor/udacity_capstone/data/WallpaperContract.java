package com.aki.photoeditor.udacity_capstone.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class WallpaperContract {

    public static final String CONTENT_AUTHORITY = "com.aki.photoeditor.udacity_capstone";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    public static final String PATH_WALLPAPER = "wallpapers";

    public static final class WallpaperEntry implements BaseColumns{
        //content uri is main, its the location of where the uri is in the system.
        //content_uri = content://com.aki.photoeditor.udacity_capstone/wallpapers
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WALLPAPER).build();
        // content_type = vnd.android.cursor.dir/com.aki.photoeditor.udacity_capstone
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_WALLPAPER ;
        //item_type = vnd.android.cursor.item/com.aki.photoeditor.udacity_capstone
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_WALLPAPER;

        //Table columns names
        public static final String TABLE_NAME = "wallpapers";
        public static final String COLUMN_WALLPAPER_PATH = "wallpaper_path";

        public static Uri buildWallpaperUriWithID(long id){
            //fn returns = content://com.aki.photoeditor.udacity_capstone/wallpapers/1024 <- some id
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}
