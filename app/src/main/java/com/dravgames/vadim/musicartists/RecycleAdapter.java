package com.dravgames.vadim.musicartists;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vadim on 06.04.2016.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    public static String LOG_TAG = "my_log";
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
            // get the view items
            title = (TextView) item.findViewById(R.id.Name);
            genres = (TextView) item.findViewById(R.id.Genres);
            albums = (TextView) item.findViewById(R.id.Albums);
            thumbImage = (ImageView) item.findViewById(R.id.imageView);
        }
    }

    /**
     * Constructor whose set the data, context and initialize cache
     * @param context
     * @param data
     */
    public RecycleAdapter(Context context, List<ObjectItem> data){
        this.data = data;
        tasks = new ArrayList<DawnloadImageTask>();
        this.context = context;
        // initialize cache
        diskLruImageCache = new DiskLruImageCache(context,DISK_CACHE_SUBDIR,DISK_CACHE_SIZE,Bitmap.CompressFormat.JPEG,70);
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artists_list, parent, false);
        ViewHolder pvh = new ViewHolder(v);
        return pvh;
    }

    /**
     *  Called by RecyclerView to display the data at the specified position.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ObjectItem objectItem = data.get(position);
        holder.title.setText(objectItem.getTitle());
        holder.genres.setText(objectItem.getGenres(", "));
        String albumsStr = objectItem.getAlbums()+" "+context.getResources().getString(R.string.albums_count);
        String tracksStr = objectItem.getTracks()+" "+context.getResources().getString(R.string.tracks_count);
        holder.albums.setText(albumsStr+", "+tracksStr);

        int id = objectItem.getId();
        boolean cache = diskLruImageCache.containsKey(id+"");
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

    /**
     * Get item title by position
     * @param position
     * @return
     */
    public String getItemTitle(int position){
        return data.get(position).getTitle();
    }

    /**
     * Set index of top and bottom visible items
     * @param top visible item
     * @param bottom bottom visible item
     */
    public void setVisibleItems(int top,int bottom){
        this.top = top;
        this.bottom = bottom;
        Log.d(LOG_TAG,"top="+top+"::bottom="+bottom+"");
    }

    /**
     * Cloase all async tasks
     */
    public void closeAllTasks(){
        for (DawnloadImageTask task: tasks){
            if(task.getStatus().equals(AsyncTask.Status.RUNNING)){
                task.cancel(true);
            }
        }
    }

    /**
     * Get object from list by index
     * @param index object
     * @return object
     */
    public ObjectItem getObjectItem(int index){
        return data.get(index);
    }

    /**
     * Getting and setting the main image from url link
     */
    private class DawnloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        int id, position;

        public DawnloadImageTask(int id, ImageView bmImage, int position) {
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
                diskLruImageCache.put(""+id, bitmap);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (((position >= top && position <= bottom) || bottom == 0) && result != null) {
                bmImage.setImageBitmap(result);
            }
        }
    }
}
