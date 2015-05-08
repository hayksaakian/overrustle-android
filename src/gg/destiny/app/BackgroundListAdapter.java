package gg.destiny.app;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Created by Hayk on 4/26/2015.
 */
public class BackgroundListAdapter extends ArrayAdapter<Integer> {
    private Integer[] mDataset;
    Activity mContext;

//    public BackgroundListAdapter(Context context, int resource, String[] objects) {
//        super(context, resource, objects);
//    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        // each data item is just a string in this case
//        public ImageView mImageView;
//        public ViewHolder(ImageView v) {
//            super(v);
//            v.setClickable(true);
//            mImageView = v;
//        }
//    }
//
    // Provide a suitable constructor (depends on the kind of dataset)
    public BackgroundListAdapter(Activity context, int resource,   Integer[] image_ids) {
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
        icon.setImageResource(mDataset[position]);

        return row;
    }
//    // Create new views (invoked by the layout manager)
//    @Override
//    public BackgroundListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
//                                                               int viewType) {
////        View v = LayoutInflater.from(parent.getContext())
////                .inflate(R.layout.background_card, parent, false);
//        // create a new view
//        View lo = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.background_card, parent, false);
//        // set the view's size, margins, paddings and layout parameters
////            ...
//
//        ViewHolder vh = new ViewHolder((ImageView)lo.findViewById(R.id.info_text));
//
//        return vh;
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        // - get element from your dataset at this position
//        // - replace the contents of the view with that element
//
//        holder.mImageView.setImageResource(mDataset[position]);
//
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    @Override
//    public int getItemCount() {
//        return mDataset.length;
//    }
}
