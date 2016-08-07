package gg.destiny.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
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
    DisplayMetrics metrics;
    int thumb_width = 0;

    public ThumbnailAdapter(Context context, int resource, int viewResourceId, List<Metadata> objects) {
        super(context, resource, viewResourceId, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.metadatas = objects;
        this.metrics = context.getResources().getDisplayMetrics();
        this.thumb_width = Math.round(this.metrics.density*80f);
//        Log.d("THUMB_WIDTH", String.valueOf(this.thumb_width));
//        Log.d("DENSITY_DPI", String.valueOf(this.metrics.densityDpi));
//        Log.d("DENSITY", String.valueOf(this.metrics.density));
//        Log.d("SCALED_DENSITY", String.valueOf(this.metrics.scaledDensity));
    }

//    n5
// 04-04 16:00:04.740  12004-12004/gg.destiny.app D/DENSITY_DPI﹕ 480
// 04-04 16:00:04.740  12004-12004/gg.destiny.app D/DENSITY﹕ 3.0
// 04-04 16:00:04.740  12004-12004/gg.destiny.app D/SCALED_DENSITY﹕ 3.0

//    n7
// 04-04 16:07:46.824    8809-8809/gg.destiny.app D/DENSITY_DPI﹕ 213
// 04-04 16:07:46.824    8809-8809/gg.destiny.app D/DENSITY﹕ 1.3312501
// 04-04 16:07:46.824    8809-8809/gg.destiny.app D/SCALED_DENSITY﹕ 1.3312501

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
                    .resize(this.thumb_width, 0)
                    .into(holder.image);
        }else if (position > 0){
            Picasso.with(context)
                    .load(R.drawable.onebyone)
                    .resize(this.thumb_width, this.thumb_width/2)
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
