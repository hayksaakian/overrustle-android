package gg.destiny.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

//    TODO:
//    reverse merge new config options
//    remove the MessageApi code
//    send up the merged config back through the DataApi

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
    public static final String[] backgrounds = new String[]{
            "le_ruse",
            "da_feels",
            "happy_pepe",
            "sad_pepe"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);

        // set up google play services api
        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Log.d(TAG, "PeerId: "+mPeerId);
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
            Log.d(TAG, "Got config data!");
            Log.d(TAG, config.toString());
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
                "le_ruse");
        setUpPickerSelection(R.id.offline_background_picker, KEY_OFFLINE_BACKGROUND, config,
                "da_feels");

        setUpPickerListener(R.id.default_background_picker, KEY_DEFAULT_BACKGROUND);
        setUpPickerListener(R.id.offline_background_picker, KEY_OFFLINE_BACKGROUND);
    }

    private void setUpPickerSelection(int spinnerId, final String configKey, DataMap config,
                                           String defaultIndexId) {

        String name = defaultIndexId;;
        if (config != null) {
            // TODO: consider using just the index
            // and not the literal resource ID
            // because IDs might change between wearable and app
            name = config.getString(configKey, defaultIndexId);
        }

        Spinner spinner = (Spinner)findViewById(spinnerId);

        spinner.setAdapter(new BackgroundListAdapter(this, R.layout.background_card, backgrounds));

        int index = java.util.Arrays.asList(backgrounds).indexOf(name);

        spinner.setSelection(index);
    }

    private void setUpPickerListener(int spinnerId, final String configKey) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                sendConfigUpdateMessage(configKey, backgrounds[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    private void sendConfigUpdateMessage(String configKey, String configValue) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            // TODO:
            // consider only sending the INDEX of the resource id
            // because the resource may have different IDs in the Wearable app
            config.putString(configKey, configValue);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + configValue);
            }
        }
    }


    public static Drawable getImage(Context context, String name) {
        return context.getResources().getDrawable(context.getResources().getIdentifier(name, "drawable", context.getPackageName()));
    }
}
