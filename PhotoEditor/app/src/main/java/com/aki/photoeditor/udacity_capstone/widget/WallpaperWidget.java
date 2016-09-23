package com.aki.photoeditor.udacity_capstone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.aki.photoeditor.udacity_capstone.AllWallpapers;
import com.aki.photoeditor.udacity_capstone.MainActivity;
import com.aki.photoeditor.udacity_capstone.ManageWallpaper;
import com.aki.photoeditor.udacity_capstone.R;
import com.aki.photoeditor.udacity_capstone.data.WallpaperContract;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of App Widget functionality.
 */
public class WallpaperWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.wallpaper_widget);

            Intent clickIntentTemplate = new Intent(context, ManageWallpaper.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_wallpaper_view, clickPendingIntentTemplate);
            Cursor c = context.getContentResolver().query(WallpaperContract.WallpaperEntry.CONTENT_URI,
                    new String[]{WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH},null,null,null);
            if (c.getCount()>0) {
                c.moveToNext();
                remoteViews.setViewVisibility(R.id.widget_wallpaper_view, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.widget_empty, View.GONE);
                String path = c.getString(c.getColumnIndexOrThrow(WallpaperContract.WallpaperEntry.COLUMN_WALLPAPER_PATH));
                File file = new File(path);
                Uri imageUri = Uri.fromFile(file);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                remoteViews.setImageViewBitmap(R.id.widget_wallpaper_view, bitmap);
            }
            else {
                remoteViews.setViewVisibility(R.id.widget_wallpaper_view, View.GONE);
                remoteViews.setViewVisibility(R.id.widget_empty, View.VISIBLE);
            }
            Intent intent = new Intent(context, AllWallpapers.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_wallpaper_view, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
            c.close();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (MainActivity.ACTION_DATA_UPDATED.equals(intent.getAction()) || ManageWallpaper.ACTION_DATA_DELETED.equals(intent.getAction()) ) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

}


