package gg.destiny.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Hayk on 5/5/2015.
 */
public class ImageListAdapter extends WearableListView.Adapter {
    private final static String TAG = "ImageListAdapter";

    private final String[] mImages;
    private String listName;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public ImageListAdapter(Context context, String[] mImageIds, String listName) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mImages = mImageIds;
        this.listName = listName;
    }


    public ImageListAdapter(Context context, String[] mImageIds) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mImages = mImageIds;
        this.listName = "";
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        TextView view = itemViewHolder.mTextView;
        String imageId = mImages[position];
        view.setText(imageId);

        itemViewHolder.mImageView.setImageDrawable(WatchFaceUtil.getImage(mContext, imageId));
        String sTag = this.listName+"|"+String.valueOf(imageId);
        itemViewHolder.mImageView.setTag(sTag);

        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        int colorPickerItemMargin = 42;// (int) getResources().getDimension(R.dimen.digital_config_color_picker_item_margin);

        // Add margins to first and last item to make it possible for user to tap on them.
        if (position == 0) {
            layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
        } else if (position == mImages.length - 1) {
            layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        itemViewHolder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return mImages.length;
    }
}
