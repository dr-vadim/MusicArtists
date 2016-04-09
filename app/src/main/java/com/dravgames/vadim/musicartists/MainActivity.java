package com.dravgames.vadim.musicartists;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";

    private ListView listView;
    private CustomAdapter adapter;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        new ParseTask().execute();
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL("http://cache-kiev05.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            try {
                JSONArray artists = new JSONArray(strJson);

                int count = artists.length();
                List<ObjectItem> list = new ArrayList<ObjectItem>();

                for (int i = 0; i < count; i++) {
                    JSONObject artist = artists.getJSONObject(i);

                    JSONObject cover = artist.getJSONObject("cover");
                    String name = artist.getString("name");
                    String descr = artist.getString("description");

                    List<String> genres = new ArrayList<String>();
                    for (int j = 0; j < artist.getJSONArray("genres").length(); j++) {
                        genres.add( artist.getJSONArray("genres").getString(j) );
                    }

                    int albums = artist.getInt("albums");
                    int tracks = artist.getInt("tracks");
                    ObjectItem item = new ObjectItem(name, descr, cover,genres, albums, tracks);
                    list.add(item);
                }

                listView = (ListView) findViewById(R.id.listView);
                // инициализация нашего адаптера
                adapter = new CustomAdapter(context, list);
                listView.setAdapter(adapter);

                // По клику будем выводить текст элемента
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getApplicationContext(), adapter.getItem(position).toString(),
                                Toast.LENGTH_SHORT).show();
                        Intent more = new Intent(context, MoreAbout.class);
                        List<String> genres = new ArrayList<String>();
                        genres.add("pop");
                        ObjectItem artist = adapter.getObjectItem(position);
                        Log.d(LOG_TAG,artist.getTitle());
                        Log.d(LOG_TAG,artist.getDescription());
                        Log.d(LOG_TAG,artist.getImages().toString());
                        Log.d(LOG_TAG,artist.getGenresList().toString());

                        try {
                            more.putExtra("image", artist.getImage("big"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        more.putExtra("name", artist.getTitle());
                        more.putExtra("descr", artist.getDescription());
                        more.putExtra("genres", artist.getGenres(", "));
                        more.putExtra("albums", artist.getAlbums());
                        more.putExtra("tracks", artist.getTracks());
                        /*more.putExtra("artist", new ObjectItem(artist.getTitle(), artist.getDescription(), artist.getImages() ,
                                artist.getGenresList(), artist.getAlbums(), artist.getTracks()
                        ));*/
                        //more.putExtra("artist", artist);
                        startActivity(more);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
