package com.dravgames.vadim.musicartists;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vadim on 06.04.2016.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    public static String LOG_TAG = "my_log";
    public static int layoutPosition = 0;
    public int top,bottom = 0;

    private List<ObjectItem> data;
    private List<DawnloadImageTask> tasks;

    private DiskLruImageCache diskLruImageCache;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";


    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, genres, albums;
        ImageView thumbImage;

        ViewHolder(View item){
            super(item);
            title = (TextView) item.findViewById(R.id.Name);
            genres = (TextView) item.findViewById(R.id.Genres);
            albums = (TextView) item.findViewById(R.id.Albums);
            thumbImage = (ImageView) item.findViewById(R.id.imageView);
        }
    }

    public RecycleAdapter(Context context, List<ObjectItem> data){
        this.data = data;
        tasks = new ArrayList<DawnloadImageTask>();
        this.context = context;
        diskLruImageCache = new DiskLruImageCache(context,DISK_CACHE_SUBDIR,DISK_CACHE_SIZE,Bitmap.CompressFormat.JPEG,70);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artists_list, parent, false);
        ViewHolder pvh = new ViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ObjectItem objectItem = data.get(position);
        holder.title.setText(objectItem.getTitle());
        holder.genres.setText(objectItem.getGenres(", "));
        String albumsStr = objectItem.getAlbums()+" "+context.getResources().getString(R.string.albums_count);
        String tracksStr = objectItem.getTracks()+" "+context.getResources().getString(R.string.tracks_count);
        holder.albums.setText(albumsStr+", "+tracksStr);

        int id = objectItem.getId();
        boolean cache = diskLruImageCache.containsKey(String.valueOf(id));
        if(!cache) {
            try {
                DawnloadImageTask dawnloadImageTask = new DawnloadImageTask(id, holder.thumbImage, position);
                dawnloadImageTask.execute(objectItem.getImage());
                tasks.add(dawnloadImageTask);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Bitmap bitmap = diskLruImageCache.getBitmap(String.valueOf(id));
            if(bitmap != null)
                holder.thumbImage.setImageBitmap(bitmap);
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public String getItemTitle(int position){
        return data.get(position).getTitle();
    }

    public void setVisibleItems(int top,int bottom){
        this.top = top;
        this.bottom = bottom;
        Log.d(LOG_TAG,"top="+top+"::bottom="+bottom+"");
    }

    public void closeAllTasks(){
        for (DawnloadImageTask task: tasks){
            if(task.getStatus().equals(AsyncTask.Status.RUNNING)){
                task.cancel(true);
            }
        }
    }

    public ObjectItem getObjectItem(int index){
        return data.get(index);
    }

    private class DawnloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        int id, position;

        public DawnloadImageTask(int id,ImageView bmImage, int position) {
            this.bmImage = bmImage;
            bmImage.setImageResource(R.drawable.misic_thumb);
            this.id = id;
            this.position = position;
        }

        protected Bitmap doInBackground(String... urls) {

            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
                diskLruImageCache.put(String.valueOf(id),bitmap);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if(((position >= top && position <= bottom) || bottom == 0) && result != null){
                Log.d(LOG_TAG, "++position >= top = " + (position >= top));
                Log.d(LOG_TAG, "++position <= bottom = " + (position <= bottom));
                Log.d(LOG_TAG, "bottom == 0 =" + (bottom == 0));
                Log.d(LOG_TAG, "position = "+position);
                bmImage.setImageBitmap(result);
            }
        }
    }
}
