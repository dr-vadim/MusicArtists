package com.dravgames.vadim.musicartists;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log;

import java.io.InputStream;

public class MoreAbout extends AppCompatActivity {

    public static String LOG_TAG = "more_about";

    private DiskLruImageCache diskLruImageCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 5; // 10MB
    private static final String DISK_CACHE_SUBDIR = "big";

    private String link = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_about);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        //Initialize cache
        diskLruImageCache = new DiskLruImageCache(this,DISK_CACHE_SUBDIR,DISK_CACHE_SIZE,Bitmap.CompressFormat.JPEG,70);

        getData(ab);
    }

    /**
     * Get the data send by main activity and set them to layout
     * @param ab ActionBar object
     */
    public void getData(ActionBar ab){
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String imageUrl = intent.getStringExtra("image");
        this.link = intent.getStringExtra("link");
        String name = intent.getStringExtra("name");
        String genres = intent.getStringExtra("genres");
        String descr = intent.getStringExtra("descr");

        int albums = intent.getIntExtra("albums", 0);
        int tracks = intent.getIntExtra("tracks",0);

        ab.setTitle(name);

        TextView genresTextView = (TextView)findViewById(R.id.genresTxt);
        TextView albumsTextView = (TextView)findViewById(R.id.albumsTxt);
        TextView descrTextView  = (TextView)findViewById(R.id.biografyTxtInfo);
        ImageView image = (ImageView)findViewById(R.id.bigImage);

        genresTextView.setText(genres);
        String albumsStr = albums+" "+getResources().getString(R.string.albums_count);
        String tracksStr = tracks+" "+getResources().getString(R.string.tracks_count);
        albumsTextView.setText(albumsStr + ", " + tracksStr);
        descrTextView.setText(descr);

        // Get image from cache by key if exist else put image to cache
        boolean cache = diskLruImageCache.containsKey(""+id);
        if (!cache)
            new DownloadImageTask(image, id).execute(imageUrl);
        else {
            Bitmap bitmap = diskLruImageCache.getBitmap(""+id);
            if(bitmap != null)
                image.setImageBitmap(bitmap);
        }
    }

    /**
     * Return to list of artists
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            this.overridePendingTransition(R.anim.back, R.anim.back);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Return to list of artists
     */
    @Override
    public void onBackPressed() {
        finish();
        this.overridePendingTransition(R.anim.back, R.anim.back);
    }

    /**
     * Open web link when click the image and link is not empty
     * @param view
     */
    public void openWebLink(View view){
        if(!link.isEmpty()) {
            try {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
                startActivity(intent);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: " + e);
            }
        }
    }

    /**
     * Getting and setting the main image from url link
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        int id = 0;

        public DownloadImageTask(ImageView bmImage, int id) {
            this.bmImage = bmImage;
            this.id = id;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
                diskLruImageCache.put(""+id, bitmap);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
