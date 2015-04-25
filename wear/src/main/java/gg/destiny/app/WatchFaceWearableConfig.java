package gg.destiny.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Hayk on 4/23/2015.
 */
public class WatchFaceWearableConfig extends Activity implements WearableListView.ClickListener {
    private static final String TAG = "WatchFaceWearableConfig";

    private GoogleApiClient mGoogleApiClient;
    private TextView mHeader;

    public static final int[] background_ids = new int[]{
            R.drawable.le_ruse,
            R.drawable.happy_pepe,
            R.drawable.sad_pepe,
            R.drawable.da_feels
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        WearableListView defaultPicker = (WearableListView) findViewById(R.id.default_background_picker);
        WearableListView offlinePicker = (WearableListView) findViewById(R.id.offline_background_picker);

        defaultPicker.setClickListener(this);
        offlinePicker.setClickListener(this);

//        WARNING: there may be an issue with having "wrap_content" for a listview
//        it might be the case that we need to mash all this stuff into one list
        defaultPicker.setAdapter(new ImageListAdapter(background_ids, "default"));
        offlinePicker.setAdapter(new ImageListAdapter(background_ids, "offline"));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "onConnected: " + connectionHint);
                }
            }

            @Override
            public void onConnectionSuspended(int cause) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "onConnectionSuspended: " + cause);
                }
            }
        })
        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "onConnectionFailed: " + result);
                }
            }
        })
        .addApi(Wearable.API)
        .build();
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        ImageViewHolder imageViewHolder = (ImageViewHolder) viewHolder;
        String stag = (String)imageViewHolder.mImageView.getTag();
        String[] parts = stag.split("|");
        int imageId = Integer.parseInt(parts[1]);
        String listName = parts[0];

        updateConfigDataItem(listName, imageId);
//        // TODO: call finish once we're done
//        finish();
    }

    private void updateConfigDataItem(final String backgroundCondition, final int backgroundImage) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putInt(backgroundCondition,
                backgroundImage);
        WatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }

    private class ImageListAdapter extends WearableListView.Adapter {
        private final int[] mImages;
        private String listName;

        public ImageListAdapter(int[] mImageIds, String listName) {
            this.mImages = mImageIds;
            this.listName = listName;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ImageViewHolder(new ImageView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            int imageId = mImages[position];
            imageViewHolder.mImageView.setImageResource(imageId);
            String sTag = this.listName+"|"+String.valueOf(imageId);
            imageViewHolder.mImageView.setTag(sTag);

            RecyclerView.LayoutParams layoutParams =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
//            int colorPickerItemMargin = (int) getResources().getDimension(R.dimen.digital_config_color_picker_item_margin);
            int colorPickerItemMargin = 42;// (int) getResources().getDimension(R.dimen.digital_config_color_picker_item_margin);

            // Add margins to first and last item to make it possible for user to tap on them.
            if (position == 0) {
                layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
            } else if (position == mImages.length - 1) {
                layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            imageViewHolder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return mImages.length;
        }
    }

    private static class ImageViewHolder extends WearableListView.ViewHolder {
        private final ImageView mImageView;

        public ImageViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }
}
