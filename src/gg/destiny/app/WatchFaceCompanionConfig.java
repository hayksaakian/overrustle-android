package gg.destiny.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Hayk on 4/23/2015.
 */
public class WatchFaceCompanionConfig extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = "WatchFaceConfig";

    private static final String KEY_DEFAULT_BACKGROUND = "DEFAULT_BACKGROUND";
    private static final String KEY_OFFLINE_BACKGROUND= "OFFLINE_BACKGROUND";
    private static final String PATH_WITH_FEATURE = "/watch_face_config";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

//    private RecyclerView mRecyclerViewOffline;
//    private RecyclerView.Adapter mAdapterOffline;
//    private RecyclerView.LayoutManager mLayoutManagerOffline;

    // TODO: find some way to simply use the resource in the wear app
    // instead of including them twice
    public static final int[] background_ids = new int[]{
            R.drawable.le_ruse,
            R.drawable.da_feels,
            R.drawable.happy_pepe,
            R.drawable.sad_pepe
    };

    public static Integer[] classy_background_ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);

        // because array adapters don't take primitives
        classy_background_ids = new Integer[background_ids.length];
        // convert int[] to Integer[]
        for(int ctr = 0; ctr < background_ids.length; ctr++) {
            classy_background_ids[ctr] = Integer.valueOf(background_ids[ctr]);
        }

        // set up google play services api
        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        // consider deleting this, it just sets a label
        ComponentName name = getIntent().getParcelableExtra(
                WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        TextView label = (TextView)findViewById(R.id.label);
        label.setText(label.getText() + " (" + name.getClassName() + ")");

        // default picker

        // offline picker
//        mRecyclerViewOffline = (RecyclerView)findViewById(R.id.default_background_picker);
//        mRecyclerViewOffline.setHasFixedSize(true);
//
//        mLayoutManagerOffline = new LinearLayoutManager(this);
//        mRecyclerViewOffline.setLayoutManager(mLayoutManagerOffline);
//
//        mAdapterOffline = new BackgroundListAdapter(background_ids);
//        mRecyclerViewOffline.setAdapter(mAdapterOffline);


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
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }

    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers(config);
        } else {
            // If DataItem with the current config can't be retrieved, select the default items on
            // each picker.
            setUpAllPickers(null);
        }
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionFailed: " + result);
        }
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Sets up selected items for all pickers according to given {@code config} and sets up their
     * item selection listeners.
     *
     * @param config the {@code DigitalWatchFaceService} config {@link DataMap}. If null, the
     *         default items are selected.
     */
    private void setUpAllPickers(DataMap config) {
        setUpPickerSelection(R.id.default_background_picker, KEY_DEFAULT_BACKGROUND, config,
                0);
        setUpPickerSelection(R.id.offline_background_picker, KEY_OFFLINE_BACKGROUND, config,
                1);

        setUpPickerListener(R.id.default_background_picker, KEY_DEFAULT_BACKGROUND);
        setUpPickerListener(R.id.offline_background_picker, KEY_OFFLINE_BACKGROUND);
    }

    private void setUpPickerSelection(int spinnerId, final String configKey, DataMap config,
                                           int defaultIndexId) {

//        String defaultColorName = getString(defaultColorNameResId);
//        int defaultColor = Color.parseColor(defaultColorName);
        int resId;
        if (config != null) {
            // TODO: consider using just the index
            // and not the literal resource ID
            // because IDs might change between wearable and app
            int indexId = config.getInt(configKey, defaultIndexId);
            resId = background_ids[indexId];
        } else {
            resId = background_ids[defaultIndexId];
        }


//        RecyclerView mRecyclerViewDefault = (RecyclerView)findViewById(R.id.default_background_picker);
//        mRecyclerViewDefault.setHasFixedSize(true);
//
//        LinearLayoutManager mLayoutManagerDefault = new LinearLayoutManager(this);
//        mRecyclerViewDefault.setLayoutManager(mLayoutManagerDefault);
//
//        BackgroundListAdapter mAdapterDefault = new BackgroundListAdapter(background_ids);
//        mRecyclerViewDefault.setAdapter(mAdapterDefault);

        Spinner spinner = (Spinner)findViewById(spinnerId);

        spinner.setAdapter(new BackgroundListAdapter(this, R.layout.background_card, classy_background_ids ));

        for (int i = 0; i < background_ids.length; i++) {
            if (background_ids[i] == resId) {
                // TODO better indication that an item is selected!
                // look at the "android wear" app's watch picker for a good example
                // TODO: we might need to do something with "index" instead  of "position"
//                mRecyclerViewDefault.smoothScrollToPosition(i);
                spinner.setSelection(i);
                break;
            }
        }

    }

    private void setUpPickerListener(int spinnerId, final String configKey) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                final Integer background_res_id = (Integer) adapterView.getItemAtPosition(pos);
                int index = java.util.Arrays.binarySearch(background_ids, background_res_id);
                sendConfigUpdateMessage(configKey, index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    private void sendConfigUpdateMessage(String configKey, int background_index) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            // TODO:
            // consider only sending the INDEX of the resource id
            // because the resource may have different IDs in the Wearable app
            config.putInt(configKey, background_index);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + Integer.toString(background_index));
            }
        }
    }
}
