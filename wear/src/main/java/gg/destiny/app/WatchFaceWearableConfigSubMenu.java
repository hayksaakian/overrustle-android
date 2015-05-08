package gg.destiny.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Hayk on 5/5/2015.
 */
public class WatchFaceWearableConfigSubMenu extends Activity implements WearableListView.ClickListener {
    private static final String TAG = "WatchFaceSubMenu";

    private GoogleApiClient mGoogleApiClient;
    private TextView mHeader;

    public static final String[] background_ids = new String[]{
            "le_ruse",
            "da_feels",
            "happy_pepe",
            "sad_pepe"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_picker_layout);

        String pretty_list_name;
        String list_name;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                pretty_list_name= "No List??";
                list_name= "none";
                Log.e(TAG, "Somehow started without a specific list in extras...");
            } else {
                pretty_list_name= extras.getString("pretty_list_name");
                list_name= extras.getString("list_name");
            }
        } else {
            pretty_list_name= (String) savedInstanceState.getSerializable("pretty_list_name");
            list_name= (String) savedInstanceState.getSerializable("list_name");
        }

        mHeader = (TextView)findViewById(R.id.default_header);
        mHeader.setText(pretty_list_name);

        WearableListView mPicker = (WearableListView) findViewById(R.id.item_picker);
        mPicker.setClickListener(this);
        mPicker.setAdapter(new ImageListAdapter(this, background_ids, list_name));


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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onTopEmptyRegionClick() { }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        ItemViewHolder imageViewHolder = (ItemViewHolder) viewHolder;
        String stag = (String)imageViewHolder.mImageView.getTag();
        // because Java's .split takes a regex in the form of a string
        // we need to escape the pipe symbol
        String[] parts = stag.split("\\|");

        Log.d(TAG, "Selected: " + stag);
        updateConfigDataItem(parts[0], parts[1], this);
    }

    private void updateConfigDataItem(final String backgroundCondition, final String backgroundImage, final Activity finishableActivity) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(backgroundCondition, backgroundImage);
        WatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite, finishableActivity);
    }

}
