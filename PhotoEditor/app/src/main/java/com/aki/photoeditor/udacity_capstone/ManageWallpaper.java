package com.aki.photoeditor.udacity_capstone;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.aki.photoeditor.udacity_capstone.data.WallpaperContract;
import com.aki.photoeditor.udacity_capstone.widget.WallpaperWidget;

import java.io.File;
import java.io.IOException;

import static com.aki.photoeditor.udacity_capstone.AllWallpapers.wallPaperLocation;

public class ManageWallpaper extends AppCompatActivity {
    private Uri imageUri;
    private final int REQUEST_ID_SET_AS_WALLPAPER = 1001;
    public static final String ACTION_DATA_DELETED = "WALLPAPER_DELETED";
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallpaper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = (ImageView) findViewById(R.id.setWallpaperView);
        file = new File(wallPaperLocation);
        imageUri = Uri.fromFile(file);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    public void setAsWallpaper(View v){
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("jpg", "image/*");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.set_as)), REQUEST_ID_SET_AS_WALLPAPER);
    }

    public void deleteWallpaper(View v){
        file.delete();
        getContentResolver().delete(WallpaperContract.WallpaperEntry.CONTENT_URI, WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH + " = ?", new String[]{wallPaperLocation});
        Intent intent = new Intent(this,WallpaperWidget.class);
        intent.setAction(ACTION_DATA_DELETED);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = {R.xml.wallpaper_widget_info};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
        finish();
    }

    public void shareImage(View v){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent,getString(R.string.share_via)));
    }

}
