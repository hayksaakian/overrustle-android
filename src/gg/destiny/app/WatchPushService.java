package gg.destiny.app;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hayk on 4/20/2015.
 */
public class WatchPushService extends IntentService {
    public static final String TAG = "Watch OverRustle";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WatchPushService(String name) {
        super(name);
    }

    public WatchPushService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String raw_notification_data = intent.getStringExtra("raw_notification_data");
        new WatchNotifier(raw_notification_data, this);
    }
}
