package com.dravgames.vadim.musicartists;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
//import android.widget.AbsListView;
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

    private RecyclerView recyclerView;
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

                /*recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

                // инициализация нашего адаптера
                adapter = new CustomAdapter(context, list);
                recyclerView.setAdapter(adapter);*/

                listView = (ListView) findViewById(R.id.listView);
                adapter = new CustomAdapter(context, list);
                listView.setAdapter(adapter);


                // По клику будем выводить текст элемента
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        adapter.closeAllTasks();
                        Toast.makeText(getApplicationContext(), adapter.getItem(position).toString(),
                                Toast.LENGTH_SHORT).show();
                        Intent more = new Intent(context, MoreAbout.class);
                        List<String> genres = new ArrayList<String>();
                        genres.add("pop");
                        ObjectItem artist = adapter.getObjectItem(position);

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
                        //more.putExtra("artist", artist);
                        startActivity(more);
                    }
                });
/*
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    }

                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            adapter.setFlinging(false);
                            //Log.d(LOG_TAG, "scrollState == "+AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
                            //Log.d(LOG_TAG,"setFlinging false");
                            int firstVisibleItem = listView.getFirstVisiblePosition();
                            //listView.fin
                            int lastVisiblePosition = listView.getLastVisiblePosition();
                            for(int i = firstVisibleItem; i <= lastVisiblePosition; i++){
                                adapter.loadImage(i,view);
                            }

                        }else if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                            adapter.setFlinging(true);
                            //Log.d(LOG_TAG, "scrollState == "+AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                            //Log.d(LOG_TAG, "setFlinging true");
                        }
                    }
                });*/

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
