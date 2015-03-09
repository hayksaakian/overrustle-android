package gg.destiny.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import gg.destiny.app.platforms.Metadata;

/**
 * Created by Hayk on 3/8/2015.
 */
public class ThumbnailAdapter extends ArrayAdapter<Metadata> {

    Context context;
    int layoutResourceId;
    List<Metadata> metadatas;

    public ThumbnailAdapter(Context context, int resource, int viewResourceId, List<Metadata> objects) {
        super(context, resource, viewResourceId, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.metadatas = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ThumbnailHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ThumbnailHolder();
            holder.image = (ImageView)row.findViewById(R.id.image);
            holder.text = (TextView)row.findViewById(R.id.title);
            holder.viewers = (TextView)row.findViewById(R.id.rustlers);
            holder.subtitle = (TextView)row.findViewById(R.id.subtitle);

            row.setTag(holder);
        } else {
            holder = (ThumbnailHolder)row.getTag();
        }

        Metadata metadata = metadatas.get(position);
        if (metadata.rustlers > 0){
            holder.viewers.setText(Integer.toString(metadata.rustlers));
        }else{
            holder.viewers.setVisibility(View.GONE);
        }


        holder.viewers.setBackgroundColor(metadata.live ? Color.parseColor("#ef5cb85c") : Color.parseColor("#efd9534f"));
        holder.text.setText(metadata.sidebarTitle());
        holder.subtitle.setText(metadata.sidebarSubTitle());
        if(metadata.live){
            Picasso.with(context)
                    .load(metadata.image_url)
                    .resize(180, 0)
                    .into(holder.image);
        }else if (position > 0){
            Picasso.with(context)
                    .load(R.drawable.onebyone)
                    .resize(180, 90)
                    .into(holder.image);
        }
        if(position == 0){
            holder.subtitle.setVisibility(View.GONE);
        }
        return row;
    }

    static class ThumbnailHolder
    {
        ImageView image;
        TextView text;
        TextView subtitle;
        TextView viewers;
    }
}
