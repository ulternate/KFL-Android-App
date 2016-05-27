package com.danielcswain.kfl.AsyncHandlers;

/**
 * Created by ulternate on 27/05/2016.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Public AsyncTask to load the image for the article
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView articleThumbnail;
    String path;

    // Our access point to this private classes methods
    public DownloadImageTask(ImageView imageView) {
        this.articleThumbnail = imageView;
    }

    // Try and load the thumbnail image url in the background
    protected Bitmap doInBackground(String... urls) {
        path = urls[0];
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(path).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    // We have the image loaded from the url so we can update the thumbnail
    protected void onPostExecute(Bitmap result) {
        articleThumbnail.setImageBitmap(result);
    }
}
