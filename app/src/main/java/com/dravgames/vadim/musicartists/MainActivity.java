package com.dravgames.vadim.musicartists;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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

public class MainActivity extends ActionBarActivity {

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
            // выводим целиком полученную json-строку
            Log.d(LOG_TAG, strJson);

            JSONObject dataJsonObj = null;
            String secondName = "";

            try {
                //dataJsonObj = new JSONObject(strJson);
                JSONArray artists = new JSONArray(strJson);

                // 1. достаем инфо о втором друге - индекс 1
                /*JSONObject secondFriend = friends.getJSONObject(1);
                secondName = secondFriend.getString("name");
                Log.d(LOG_TAG, "Второе имя: " + secondName);*/
                int count = artists.length();
                List<ObjectItem> list = new ArrayList<ObjectItem>();
                // 2. перебираем и выводим контакты каждого друга
                for (int i = 0; i < count; i++) {
                    JSONObject artist = artists.getJSONObject(i);

                    //JSONArray genres = artist.getJSONArray("genres");
                   // String strGenres = genres.join(",");

                    JSONObject cover = artist.getJSONObject("cover");
                    //JSONArray cover = artist.getJSONArray("cover");
                    String name = artist.getString("name");
                    String smallImage = cover.getString("small");
                    List<String> genres = new ArrayList<String>();
                    for (int j = 0; j < artist.getJSONArray("genres").length(); j++) {
                        genres.add( artist.getJSONArray("genres").getString(j) );
                    }

                    //String[] genres = artist.getJSONArray("genres").toString().replace("},{", " ,").split(" ");
                    int albums = artist.getInt("albums");
                    int tracks = artist.getInt("tracks");
                    ObjectItem item = new ObjectItem(name,smallImage,genres, albums, tracks);
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
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
