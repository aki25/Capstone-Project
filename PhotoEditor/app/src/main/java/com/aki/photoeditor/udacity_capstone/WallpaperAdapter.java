package com.aki.photoeditor.udacity_capstone;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import static com.aki.photoeditor.udacity_capstone.data.WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH;

public class WallpaperAdapter extends CursorAdapter {

    Cursor mainCursor;
    Context mainContext;

    public WallpaperAdapter(Context context, Cursor c) {
        super(context, c,0);
        mainCursor = c;
        mainContext = context;
    }

    @Override
    public Object getItem(int position) {
        mainCursor.moveToPosition(position);
        String path = mainCursor.getString(mainCursor.getColumnIndexOrThrow(COLUMN_WALLPAPER_PATH));
        return path;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.wallpaper_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.wallpaper_single_item);
        String path = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WALLPAPER_PATH));
        File file = new File(path);
        Uri imageUri = Uri.fromFile(file);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }
}