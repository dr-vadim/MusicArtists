package com.dravgames.vadim.musicartists;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log;

import java.io.InputStream;

public class MoreAbout extends AppCompatActivity {

    public static String LOG_TAG = "more_about";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_about);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image");
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
        albumsTextView.setText(albumsStr+", "+tracksStr);
        descrTextView.setText(descr);

        new DownloadImageTask(image).execute(imageUrl);

    }

    @Override
    public void onBackPressed() {
        Intent k = new Intent(this, MainActivity.class);
        startActivity(k);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
