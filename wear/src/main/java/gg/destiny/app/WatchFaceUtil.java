package gg.destiny.app;

/**
 * Created by Hayk on 4/23/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public final class WatchFaceUtil {
    private static final String TAG = "WatchFaceUtil";

    public static final String KEY_DEFAULT_BACKGROUND = "DEFAULT_BACKGROUND";
    public static final String KEY_OFFLINE_BACKGROUND = "OFFLINE_BACKGROUND";

    /**
     * The path for the {@link DataItem} containing {@link AnalogWatchFaceService} configuration.
     */
    public static final String PATH_WITH_FEATURE = "/watch_face_config";

    /**
     * Callback interface to perform an action with the current config {@link DataMap} for
     * {@link AnalogWatchFaceService}.
     */
    public interface FetchConfigDataMapCallback {
        /**
         * Callback invoked with the current config {@link DataMap} for
         * {@link AnalogWatchFaceService}.
         */
        void onConfigDataMapFetched(DataMap config);
    }

    /**
     * Asynchronously fetches the current config {@link DataMap} for {@link AnalogWatchFaceService}
     * and passes it to the given callback.
     * <p>
     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    public static void fetchConfigDataMap(
            final String path,
            final GoogleApiClient client,
            final FetchConfigDataMapCallback callback) {
        Log.d(TAG, "fetchConfigDataMap");
        Log.d(TAG, "connected? " + String.valueOf(client.isConnected()));
        if(!client.isConnected()){
            Log.e(TAG, "NOT CONNECTED FOR SOME REASON!!");
        }

        Wearable.NodeApi.getLocalNode(client).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                Log.d(TAG, " Wearable.NodeApi.getLocalNode.onResult");
                String localNode = getLocalNodeResult.getNode().getId();
                Uri backgrounds_uri = new Uri.Builder()
                        .scheme("wear")
                        .path(path)
                        .authority(localNode)
                        .build();
                Wearable.DataApi.getDataItem(client, backgrounds_uri)
                        .setResultCallback(new DataItemResultCallback(callback));


            }
        });

    }

    /**
     * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
     * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
     * it's created.
     * <p>
     * It is allowed that only some of the keys used in the config DataItem appear in
     * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
     */
    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
                                                    final DataMap configKeysToOverwrite,
                                                    final Activity activityToFinish
                                                    ) {
        Log.d(TAG, "overwriteKeysInConfigDataMap");
        WatchFaceUtil.fetchConfigDataMap(
            PATH_WITH_FEATURE,
            googleApiClient,
            new FetchConfigDataMapCallback() {
                @Override
                public void onConfigDataMapFetched(DataMap currentConfig) {
                    Log.d(TAG, "onConfigDataMapFetched");
                    DataMap overwrittenConfig = new DataMap();
                    overwrittenConfig.putAll(currentConfig);
                    overwrittenConfig.putAll(configKeysToOverwrite);
                    WatchFaceUtil.putConfigDataItem(PATH_WITH_FEATURE,
                            googleApiClient,
                            overwrittenConfig,
                            activityToFinish);
                }
            }
        );
    }

    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
    public static void putConfigDataItem(String path,
                                         GoogleApiClient googleApiClient,
                                         DataMap newConfig, final Activity activityToFinish) {
        Log.d(TAG, "putConfigDataItem");
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                }
                Log.d(TAG, "activityToFinish? "+String.valueOf(activityToFinish != null));
                if (activityToFinish != null) {
                    activityToFinish.finish();
                }
            }
        });
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }

    public static Drawable getImage(Context context, String name) {
        return context.getResources().getDrawable(context.getResources().getIdentifier(name, "drawable", context.getPackageName()));
    }

    private WatchFaceUtil() { }
}

