package com.aki.photoeditor.udacity_capstone.imageProcessors;

import android.graphics.Bitmap;
import android.graphics.Color;


public class Filters {

    public static Bitmap doGreyScale(Bitmap src) {
        //best grayScale values
        final double RED = 0.299;
        final double GREEN = 0.587;
        final double BLUE = 0.114;

        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap out = Bitmap.createBitmap(width, height, src.getConfig());
        // pixel information
        int a,r,g,b;
        int pixel;
        // scan through every single pixel
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);
                // take conversion up to one single value
                r = g = b = (int) (RED * r + GREEN * g + BLUE * b);
                // set new pixel color to output bitmap
                out.setPixel(x, y, Color.argb(a,r,g,b));
            }
        }
        return out;
    }

    public static Bitmap applyBrightnessEffect(Bitmap src, int value) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap out = Bitmap.createBitmap(width, height, src.getConfig());
        int a,r,g,b;
        int pixel;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                pixel = src.getPixel(x, y);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);

                // increase/decrease each channel
                r = r + value;
                if(r > 255) { r = 255; }
                else if(r < 0) { r = 0; }

                g = g + value;
                if(g > 255) { g = 255; }
                else if(g < 0) { g = 0; }

                b = b + value;
                if(b> 255) { b = 255; }
                else if(b < 0) { b = 0; }

                out.setPixel(x, y, Color.argb(a,r,g,b));
            }
        }
        return out;
    }
}
