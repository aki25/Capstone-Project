package com.aki.photoeditor.udacity_capstone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;

public class EditingComplete extends AppCompatActivity {

    private Uri imageUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_complete);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView imageView = (ImageView) findViewById(R.id.editing_done_display);
        imageUri = getIntent().getParcelableExtra("imageLocation");
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        AdView mAdView = (AdView) findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    public void setAsWallpaper(View v){
        int REQUEST_ID_SET_AS_WALLPAPER = 100;
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("jpg", "image/*");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.set_as)), REQUEST_ID_SET_AS_WALLPAPER);
    }

    public void editNewImage(View v){
        Intent startFresh = new Intent(this, MainActivity.class);
        startFresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startFresh.putExtra("secondRun",true);
        startActivity(startFresh);
    }

    public void shareImage(View v){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent,getString(R.string.share_via)));
    }
}
