package com.dravgames.vadim.musicartists;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Vadim on 06.04.2016.
 */
public class CustomAdapter extends ArrayAdapter<String> {

    private List<ObjectItem> data;
    private Context context;

    public CustomAdapter(Context context, List<ObjectItem> data){
        super(context, R.layout.artists_list);
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public String getItem(int position){
        return data.get(position).getTitle();
    }

    public ObjectItem getObjectItem(int position){
        return data.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    // заполнение элементов списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // задаем вид элемента списка, который мы создали высше
        View view = inflater.inflate(R.layout.artists_list, parent, false);

        // проставляем данные для элементов
        TextView title = (TextView)view.findViewById(R.id.Name);
        TextView genres = (TextView)view.findViewById(R.id.Genres);
        TextView albums = (TextView)view.findViewById(R.id.Albums);
        ImageView thumbImage = (ImageView)view.findViewById(R.id.imageView);

        // получаем элемент со списка
        ObjectItem objectItem = data.get(position);

        // устанавливаем значения компонентам одного эелемента списка
        title.setText(objectItem.getTitle());
        genres.setText(objectItem.getGenres(", "));
        String albumsStr = objectItem.getAlbums()+" "+context.getResources().getString(R.string.albums_count);
        String tracksStr = objectItem.getTracks()+" "+context.getResources().getString(R.string.tracks_count);
        albums.setText(albumsStr+", "+tracksStr);
        try {
            new DownloadImageTask(thumbImage)
                    .execute(objectItem.getImage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //thumbImage.setImageDrawable(objectItem.getImage());

        return view;
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
