package com.dravgames.vadim.musicartists;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    public static String LOG_TAG = "my_log";
    public File pathToJson = null;
    final public String linkToJson = "http://cache-kiev05.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json";
    public String jsonName = "artists";

    public static Activity activity;

    private RecyclerView recyclerView;
    private RecycleAdapter RAdapter;
    private Context context;
    LinearLayoutManager llm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        context = this;
        setContentView(R.layout.activity_main);
        pathToJson = new File(getFilesDir(),"Data");
        new ParseTask().execute();
    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // check internet connection
            if(Utils.isOnline(context)){
                // if internet connection exist load the data
                try {
                    // geting json data fron url link
                    URL url = new URL(linkToJson);

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

                    // update if exist or create json file with data
                    writeToFile(resultJson);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                // if internet connection does not exist try to get internal saved json data
                File f = new File(pathToJson,jsonName+".json");
                // check if has internal json data
                if(f.exists() && f.isFile()){
                    Log.d(LOG_TAG, "File exist and is file");
                    resultJson = readFile(f);
                }
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);

            // if json data is empty show alert dialog and start another activity for waiting internet connection
            if (strJson.isEmpty()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle(R.string.attention_txt)
                        .setMessage(R.string.need_connection_txt)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                Intent noConnection = new Intent(context, NoConnection.class);
                                startActivity(noConnection);
                                finish();
                            }
                        }).show();
            }else{

                try {
                    // convert json string to JSONArray
                    JSONArray artists = new JSONArray(strJson);

                    int count = artists.length();
                    List<ObjectItem> list = new ArrayList<ObjectItem>();

                    // add data from JSONArray to ArrayList list
                    for (int i = 0; i < count; i++) {
                        JSONObject artist = artists.getJSONObject(i);

                        JSONObject cover = artist.getJSONObject("cover");

                        int id = artist.getInt("id");
                        String name = artist.getString("name");
                        String descr = artist.getString("description");

                        List<String> genres = new ArrayList<String>();
                        for (int j = 0; j < artist.getJSONArray("genres").length(); j++) {
                            genres.add(artist.getJSONArray("genres").getString(j));
                        }

                        int albums = artist.getInt("albums");
                        int tracks = artist.getInt("tracks");
                        ObjectItem item = new ObjectItem(id, name, descr, cover, genres, albums, tracks);
                        if (!artist.isNull("link")) {
                            item.setLink(artist.getString("link"));
                        }
                        list.add(item);
                    }

                    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

                    llm = new LinearLayoutManager(context);
                    recyclerView.setLayoutManager(llm);

                    // initialize adapter
                    RAdapter = new RecycleAdapter(context, list);
                    recyclerView.setAdapter(RAdapter);
                    // add click listener
                    recyclerView.addOnItemTouchListener(
                            new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    // close all async tasks
                                    RAdapter.closeAllTasks();
                                    final int result = 1;
                                    Intent more = new Intent(context, MoreAbout.class);
                                    // get artist object by position
                                    ObjectItem artist = RAdapter.getObjectItem(position);

                                    // put extra data to send for another activity
                                    try {
                                        more.putExtra("image", artist.getImage("big"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    more.putExtra("id", artist.getId());
                                    more.putExtra("name", artist.getTitle());
                                    more.putExtra("descr", artist.getDescription());
                                    more.putExtra("genres", artist.getGenres(", "));
                                    more.putExtra("albums", artist.getAlbums());
                                    more.putExtra("tracks", artist.getTracks());
                                    more.putExtra("link", artist.getLink());

                                    // start activity
                                    startActivityForResult(more, result);
                                }
                            })
                    );
                    // add scroll listener
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        /**
                         * Getting and setting the first and last visible items
                         *
                         * @param view
                         * @param dx
                         * @param dy
                         */
                        @Override
                        public void onScrolled(RecyclerView view, int dx, int dy) {
                            super.onScrolled(view, dx, dy);
                            int top = llm.findFirstVisibleItemPosition();
                            int bottom = llm.findLastVisibleItemPosition();
                            RAdapter.setVisibleItems(top, bottom);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write json data string to file
     * @param data
     */
    private void writeToFile(String data) {
        if(!pathToJson.exists()){
            pathToJson.mkdir();
        }
        File f = new File(pathToJson,jsonName+".json");
        try{
            FileWriter file = new FileWriter(f);
            file.write(data);
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    /**
     * read data json from file
     * @param file
     * @return
     */
    private String readFile(File file){
        String ret = "";
        try {
            FileInputStream fis = new FileInputStream (file);

            if ( fis != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                fis.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }
}
