package com.aki.photoeditor.udacity_capstone;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.aki.photoeditor.udacity_capstone.MainActivity.DIRECTORY_LOCATION;

public class SaveToDisk {

    public static File writeToDisk(Bitmap bitmap , String file_name, Context context){
        //checking if external storage is mounted or not
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath()+DIRECTORY_LOCATION);
            if(!Dir.exists()){
                if(!Dir.mkdir()){
                    Toast.makeText(context,context.getString(R.string.failed_new_dir), Toast.LENGTH_SHORT).show();
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd--HH:mm:ss", Locale.getDefault());
            String  currentTimeStamp = dateFormat.format(new Date());
            String filename = DIRECTORY_LOCATION+"_"+currentTimeStamp+".jpeg";
            File file;
            if(file_name != null)
                file = new File(Dir,file_name);
            else
                file = new File(Dir,filename);
            OutputStream outStream;
            if (file.exists()) {
                boolean delete = file.delete();
            }
            try {
                outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("file", "" + file);
            return file;
        }
        else {
            Toast.makeText(context, context.getString(R.string.sd_missing), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
