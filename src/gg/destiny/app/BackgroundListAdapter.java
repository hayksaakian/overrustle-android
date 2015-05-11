package gg.destiny.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Created by Hayk on 4/26/2015.
 */
public class BackgroundListAdapter extends ArrayAdapter<String> {
    private String[] mDataset;
    Activity mContext;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BackgroundListAdapter(Activity context, int resource,   String[] image_ids) {
        super(context, resource, image_ids);
        mContext = context;
        mDataset = image_ids;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=mContext.getLayoutInflater();
        View row=inflater.inflate(R.layout.background_card, parent, false);

        ImageView icon=(ImageView)row.findViewById(R.id.background_thumbnail_item);

        icon.setImageDrawable(WatchFaceCompanionConfig.getImage(mContext, mDataset[position]));

        return row;
    }
}
