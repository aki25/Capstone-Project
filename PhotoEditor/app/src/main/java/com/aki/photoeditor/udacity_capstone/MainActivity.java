package com.aki.photoeditor.udacity_capstone;

import android.*;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aki.photoeditor.udacity_capstone.data.WallpaperContract;
import com.aki.photoeditor.udacity_capstone.widget.WallpaperWidget;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    public final static String DIRECTORY_LOCATION = "/PhotoEditorUdacity";
    public final static String DIRECTORY_LOCATION_WALLPAPERS = "/PEWallpapers";
    public final static String ACTION_DATA_UPDATED = "NEW_WALLPAPER_ADDED";
    public static String IMAGE_FILENAME_CAMERA = null;
    public static String IMAGE_FILENAME_SD;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CAMERA_REQUEST = 200;
    final int requestCodeWriteSD = 1001;
    private Uri imageURI;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private Bitmap wallpaper;
    boolean oldPref = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = (ProgressBar) findViewById(R.id.wallpaperProgress);
        fab = (FloatingActionButton) findViewById(R.id.downloadWallpaper);
        fab.setVisibility(View.INVISIBLE);
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        methodRequiresTwoPermission();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean getWallpaper = sharedPreferences.getBoolean(getString(R.string.wallpaper_pref_key),true);
        TextView textView = (TextView) findViewById(R.id.notConnected);
        boolean secondRun = getIntent().getBooleanExtra("secondRun",false);
        if (getWallpaper) {
            if (isConnected) {
                textView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                if (!secondRun)
                new FetchWallpaper().execute();
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setContentDescription(getString(R.string.notConnected));
            }
        }
        else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getString(R.string.enable_wallpaper_download));
            textView.setContentDescription(getString(R.string.enable_wallpaper_download));
        }
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd--HH_mm_ss", Locale.getDefault()).format(new Date());
        String imageFileName = DIRECTORY_LOCATION+"_"+timeStamp+".jpeg";
        IMAGE_FILENAME_SD = imageFileName;
        Log.v("file_name",IMAGE_FILENAME_SD);
    }


    @AfterPermissionGranted(requestCodeWriteSD)
    private void methodRequiresTwoPermission() {
        String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.write_sd_rationale), requestCodeWriteSD, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Handle negative button on click listener. Pass null if you don't want to handle it.
        DialogInterface.OnClickListener cancelButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Let's show a toast
                Toast.makeText(getApplicationContext(), R.string.settings_dialog_canceled, Toast.LENGTH_SHORT)
                        .show();
            }
        };

        // (Optional) Check whether the user denied permissions and checked NEVER ASK AGAIN.
        // This will display a dialog directing them to enable the permission in app settings.
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(
                this,
                getString(R.string.rationale_ask_again),
                R.string.setting,
                R.string.cancel,
                cancelButtonListener,
                perms);
    }

    public void downloadWallpaper(View v){
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd--HH_mm_ss", Locale.getDefault()).format(new Date());
        String imageFileName = DIRECTORY_LOCATION_WALLPAPERS+"_"+timeStamp+".jpeg";
        File file = SaveToDisk.writeToDisk(wallpaper,imageFileName,getApplicationContext());
        final ContentValues values = new ContentValues();
        values.put(WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH,file.toString());
        Toast.makeText(this, getString(R.string.wallpaper_saved), Toast.LENGTH_SHORT).show();
        getContentResolver().insert(WallpaperContract.WallpaperEntry.CONTENT_URI, values);
        Intent intent = new Intent(this,WallpaperWidget.class);
        intent.setAction(ACTION_DATA_UPDATED);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
        int[] ids = {R.xml.wallpaper_widget_info};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }

    public void loadImageFromGallery(View v){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        //where to find the data
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        // finally get URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set data and type (look where and what?) Get all image types
        photoPickerIntent.setDataAndType(data, "image/*");

        //Invoke this activity and get something back from it
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    public void loadImageFromCamera(View v){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        if (photoFile != null) {
            // if image is given a place in SD Card after it has been clicked, get its address to pass on to next activity.
            imageURI = Uri.fromFile(photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_GALLERY_REQUEST:
                // if here, image gallery opened.
                if (resultCode == RESULT_OK) {
                    // if here, everything processed successfully.
                    // address of image on the SD card.
                    imageURI = data.getData();
                    //Send the image address to next screen to load the image for processing
                    Intent imageLoadedIntent = new Intent(this, EditImage.class);
                    imageLoadedIntent.putExtra("imageLocation", imageURI);
                    startActivity(imageLoadedIntent);
                }
                break;

            case CAMERA_REQUEST:
                // if here, image gallery opened.
                if (resultCode == RESULT_OK) {
                    // if here, everything processed successfully.
                    //Send the image address to next screen to load the image for processing
                    Intent imageLoadedIntent = new Intent(this, EditImage.class);
                    imageLoadedIntent.putExtra("imageLocation",imageURI);
                    startActivity(imageLoadedIntent);
                }
                break;
        }
    }


    private class FetchWallpaper extends AsyncTask<Void,String,String>{

        Random random = new Random();

        private String getWallpaperDataFromJson(String wallpaperJsonStr)
                throws JSONException {
            if (wallpaperJsonStr == null)
                return null;
            // These are the names of the JSON objects that need to be extracted.
            final String RESULTS_LIST = "hits";

            final String IMAGE_URL = "webformatURL";

            JSONObject wallpaperJson = new JSONObject(wallpaperJsonStr);
            JSONArray wallpaperArray = wallpaperJson.getJSONArray(RESULTS_LIST);
            JSONObject wallpaperInfo = wallpaperArray.getJSONObject(random.nextInt(15));
            String wallpaperURL = wallpaperInfo.getString(IMAGE_URL);
            System.out.println("wallaperp url =="+wallpaperURL);
            return wallpaperURL;
        }


        @Override
        protected String doInBackground(Void... voids) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the MovieDB query

                final String MOVIE_BASE_URL =
                        "https://pixabay.com/api/";
                final String APPID_PARAM = "key";
                final String IMAGE_TYPE = "image_type";
                final String SAFE_SEARCH = "safesearch";
                final String CATEGORY = "category";
                String[] categories = new String[]{"nature", "backgrounds", "places", "travel", "buildings"};
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.PIXABAY_API_KEY)
                        .appendQueryParameter(IMAGE_TYPE,"photo")
                        .appendQueryParameter(SAFE_SEARCH,"true")
                        .appendQueryParameter(CATEGORY,categories[random.nextInt(categories.length)])
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    movieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    movieJsonStr = null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                movieJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWallpaperDataFromJson(movieJsonStr);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String url) {
            Picasso.with(getApplicationContext())
                    .load(url)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            ImageView imageView = (ImageView) findViewById(R.id.greetingWallpaper);
                            imageView.setImageBitmap(bitmap);
                            wallpaper = bitmap;
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
            progressBar.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
        }
    }


    private String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd--HH_mm_ss", Locale.getDefault()).format(new Date());
        String imageFileName = DIRECTORY_LOCATION+"_"+timeStamp;
        IMAGE_FILENAME_CAMERA = imageFileName+".jpeg";
        File Root = Environment.getExternalStorageDirectory();
        File storageDir = new File(Root.getAbsolutePath()+DIRECTORY_LOCATION);
        if(!storageDir.exists()){
            if(!storageDir.mkdir()){
                Toast.makeText(MainActivity.this, getString(R.string.storage_access_fail), Toast.LENGTH_SHORT).show();
            }
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_manageWallpapers) {
            startActivity(new Intent(getApplicationContext(),AllWallpapers.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
