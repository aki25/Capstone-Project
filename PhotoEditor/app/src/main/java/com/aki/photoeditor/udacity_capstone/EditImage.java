package com.aki.photoeditor.udacity_capstone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.picassotransformations.Kernel;
import com.picassotransformations.jhlabs.BlurTransformation;
import com.github.florent37.viewanimator.ViewAnimator;
import com.picassotransformations.jhlabs.GainTransformation;
import com.picassotransformations.jhlabs.KaleidoscopeTransformation;
import com.picassotransformations.jhlabs.MarbleTransformation;
import com.picassotransformations.jhlabs.PolarTransformation;
import com.picassotransformations.jhlabs.PosterizeTransformation;

import java.io.File;
import java.io.FileNotFoundException;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.aki.photoeditor.udacity_capstone.MainActivity.IMAGE_FILENAME_CAMERA;
import static com.aki.photoeditor.udacity_capstone.MainActivity.IMAGE_FILENAME_SD;
import static com.aki.photoeditor.udacity_capstone.imageProcessors.Filters.applyBrightnessEffect;
import static com.aki.photoeditor.udacity_capstone.imageProcessors.Filters.doGreyScale;

public class EditImage extends AppCompatActivity implements EditImageFragment.CallBack, StrengthFragment.StrengthCallBacks {

    private ImageView imageDisplay;
    private Bitmap inputImage = null ;
    private PhotoViewAttacher imageViewFunctionality;
    private static final int imageWidth = 512;
    private static final int imageHeight = 384;
    private Bitmap undoImage;
    private Bitmap outputImage;
    private String FINAL_IMAGE_FILENAME;
    public static int brightness = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment filterFragment = new EditImageFragment();
        int container = R.id.fragmentContainer;

        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(container,filterFragment);
        transaction.commit();
        setContentView(R.layout.activity_edit_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Uri tempImageLocation = getIntent().getParcelableExtra("imageLocation");
        try {
            // inputImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(tempImageLocation));
            inputImage = decodeSampledBitmapFromUri(tempImageLocation,imageWidth,imageHeight);
            // if here, that mean image has been successfully loaded.
            if(inputImage!=null){
                imageDisplay = (ImageView) findViewById(R.id.imageDisplay);
                imageDisplay.setImageBitmap(inputImage);
                ViewAnimator.animate(imageDisplay).translationY(-1000, 0).alpha(0,1).dp().translationX(-20, 0)
                        .descelerate().duration(1500).start();
                imageDisplay.invalidate();
                imageViewFunctionality = new PhotoViewAttacher(imageDisplay);
                imageViewFunctionality.update();
            }
            outputImage = inputImage;
            undoImage = inputImage;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.failed_image_load), Toast.LENGTH_SHORT).show();
        }

        if (IMAGE_FILENAME_CAMERA == null)
            FINAL_IMAGE_FILENAME = IMAGE_FILENAME_SD;
        else
            FINAL_IMAGE_FILENAME = IMAGE_FILENAME_CAMERA;

    }
    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) {

        Bitmap bm = null;

        try{
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        return bm;
    }

    private Bitmap mirror(float a, float b){
        undoImage = Bitmap.createScaledBitmap(inputImage,inputImage.getWidth(), inputImage.getHeight(),true);
        Matrix flip = new Matrix();
        flip.preScale(a, b);
        return Bitmap.createBitmap(inputImage, 0, 0, inputImage.getWidth(), inputImage.getHeight(), flip, true);
    }

    private Bitmap rotate(){
        undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
        Matrix rotate = new Matrix();
        rotate.postRotate(90);
        return Bitmap.createBitmap(inputImage, 0, 0, inputImage.getWidth(), inputImage.getHeight(), rotate, true);
    }

    private Bitmap undoLastEffect(){
        return Bitmap.createBitmap(undoImage);
    }

    @Override
    public void processStrength(int process_type, int value) {
            new Computations().execute(3f, (float) value);
    }

    @Override
    public void discardEffect() {
        new Computations().execute(0f,0f);
    }

    private class Computations extends AsyncTask<Float, Void, Void> {
        @Override
        protected void onPreExecute() {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Float... argument) {
            int choice = Math.round(argument[0]);
            float effectStrength = argument[1];
            switch (choice) {
                case 0: // undo selected
                    outputImage = undoLastEffect();
                    inputImage = outputImage;
                    break;
                case 1: // blur selected
                    outputImage = new BlurTransformation().transform(inputImage);
                    inputImage = outputImage;
                    break;
                case 2: // rotate selected
                    outputImage = rotate();
                    inputImage = outputImage;
                    break;
                case 3: // brightness selected
                    outputImage = applyBrightnessEffect(inputImage, (int) effectStrength);
                    break;
                case 4: // mirror horizontal selected
                    outputImage = mirror(-1, 1);
                    inputImage = outputImage;
                    break;
                case 5: // mirror vertical selected
                    outputImage = mirror(1,-1);
                    inputImage = outputImage;
                    break;
                case 6: // grayscale selected
                    outputImage = doGreyScale(inputImage);
                    inputImage = outputImage;
                    break;
                case 7: // sharpen selected
                    float[] sharpenMatrix = {
                            0.0f, -1f,  0.0f,
                            -1f,  5.1f, -1f,
                            0.0f, -1f,  0.0f
                    };
                    Kernel kernel = new Kernel(3,3,sharpenMatrix);
                    BlurTransformation blur = new BlurTransformation();
                    blur.setKernel(kernel);
                    outputImage = blur.transform(inputImage);
                    inputImage = outputImage;
                    break;
//
//                /********************************** EFFECTS ****************************/
                case 101: // some effect2 selected
                    GainTransformation temp = new GainTransformation();
                    temp.setBias(3);
                    Bitmap gain = Bitmap.createBitmap(outputImage);
                    outputImage = temp.transform(gain);
                    inputImage = outputImage;
                    // System.out.println("in bf pro");
                    break;
                case 102: // some effect selected
                    Bitmap marble = Bitmap.createBitmap(outputImage);
                    outputImage = new MarbleTransformation().transform(marble);
                    inputImage = outputImage;
                    break;
                case 103: // some effect2 selected
                    Bitmap k = Bitmap.createBitmap(outputImage);
                    outputImage = new KaleidoscopeTransformation().transform(k);
                    inputImage = outputImage;
                    break;
                case 104: // some effect2 selected
                    PosterizeTransformation poster = new PosterizeTransformation();
                    poster.setNumLevels(4);
                    Bitmap painting = Bitmap.createBitmap(outputImage);
                    outputImage = poster.transform(painting);
                    inputImage = outputImage;
                    break;
                case 105: // some effect2 selected
                    Bitmap polar = Bitmap.createBitmap(outputImage);
                    outputImage = new PolarTransformation().transform(polar);
                    inputImage = outputImage;
                    break;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void x) {

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            imageSetter(outputImage);
        }
    }

    private void imageSetter(Bitmap image){
        imageDisplay.setImageBitmap(image);
        imageDisplay.invalidate();
        imageViewFunctionality.update();
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void processImage(View v){
        int id = v.getId();
        switch (id){
            case R.id.undoButton:
                // 0 option is Undo
                new Computations().execute(0f, 0f);
                break;

            case R.id.blurButton:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                new Computations().execute(1f, 0f);
                break;

            case R.id.rotateButton:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 2 option is rotate
                new Computations().execute(2f, 0f);
                break;

            case R.id.mirrorHor6:
                // 4 option is mirror effect horizontally
                new Computations().execute(4f, 0f);
                break;

            case R.id.mirrorVert6:
                // 5 option is mirror effect vertically
                new Computations().execute(5f, 0f);
                break;

            case R.id.sharpenButton:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 3 option is brightness
                // slider option available, call for it
                new Computations().execute(7f, 0f);
                break;

            case R.id.grayScaleEffectButton:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 6 option is grayscale effect
                new Computations().execute(6f, 0f);
                break;

            /* effects */

            case R.id.filterButton1:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 101 filter effect
                new Computations().execute(101f, 0f);
                break;

            case R.id.filterButton2:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 101 filter effect
                new Computations().execute(102f, 0f);
                break;

            case R.id.filterButton3:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 101 filter effect
                new Computations().execute(103f, 0f);
                break;

            case R.id.filterButton4:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 101 filter effect
                new Computations().execute(104f, 0f);
                break;

            case R.id.filterButton5:
                undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
                // 101 filter effect
                new Computations().execute(105f, 0f);
                break;
        }
    }

    @Override
    public void showSeekBar(int max, int initial) {
        undoImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth(), inputImage.getHeight(), true);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment strengthFragment = new StrengthFragment();
        int container = R.id.fragmentContainer;
        Bundle bundle = new Bundle();
        bundle.putInt("strength",max);
        if (brightness == -1)
            bundle.putInt("initial",initial);
        else
            bundle.putInt("initial",brightness);
        strengthFragment.setArguments(bundle);
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        transaction.addToBackStack("null");
        transaction.replace(container,strengthFragment);
        transaction.commit();
    }

    public void saveImageFinal(View v){
        File file = SaveToDisk.writeToDisk(outputImage,FINAL_IMAGE_FILENAME,getApplicationContext());
        String location = file.toString();
        Uri imageUri = Uri.fromFile(file);
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{location}, null, null);
        Intent editingCompletedIntent = new Intent(this, EditingComplete.class);
        editingCompletedIntent.putExtra("imageLocation", imageUri);
        startActivity(editingCompletedIntent);
    }
}
