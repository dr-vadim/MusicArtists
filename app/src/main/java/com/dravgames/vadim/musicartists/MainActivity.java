package com.dravgames.vadim.musicartists;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
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
    private RecycleAdapter RAdapter;
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

                    int id = artist.getInt("id");
                    String name = artist.getString("name");
                    String descr = artist.getString("description");

                    List<String> genres = new ArrayList<String>();
                    for (int j = 0; j < artist.getJSONArray("genres").length(); j++) {
                        genres.add( artist.getJSONArray("genres").getString(j) );
                    }

                    int albums = artist.getInt("albums");
                    int tracks = artist.getInt("tracks");
                    ObjectItem item = new ObjectItem(id, name, descr, cover,genres, albums, tracks);
                    list.add(item);
                }

                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

                LinearLayoutManager llm = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(llm);

                // инициализация нашего адаптера
                RAdapter = new RecycleAdapter(context, list);
                recyclerView.setAdapter(RAdapter);

                recyclerView.addOnItemTouchListener(
                        new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                RAdapter.closeAllTasks();
                                final int result = 1;
                                Intent more = new Intent(context, MoreAbout.class);
                                List<String> genres = new ArrayList<String>();
                                genres.add("pop");
                                ObjectItem artist = RAdapter.getObjectItem(position);

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
                                //startActivity(more);
                                startActivityForResult(more,result);
                            }
                        })
                );

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){

                    public void onScrollStateChanged(RecyclerView view, int newState){
                        //if(newState == view.SCROLL_STATE_IDLE) {
                            LinearLayoutManager llm = (LinearLayoutManager) view.getLayoutManager();
                            int top = llm.findFirstVisibleItemPosition();
                            int bottom = llm.findLastVisibleItemPosition();
                            RAdapter.setVisibleItems(top, bottom);
                        //}
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
