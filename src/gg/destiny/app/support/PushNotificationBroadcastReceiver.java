package gg.destiny.app.support;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gg.destiny.app.WatchNotifier;
import gg.destiny.app.WatchPushService;

/**
 * Created by Hayk on 4/20/2015.
 */
public class PushNotificationBroadcastReceiver extends ParsePushBroadcastReceiver {
    public static final String PARSE_DATA_KEY = "com.parse.Data";
    public static final String TAG = "Push OverRustle";

    @Override
    protected void onPushReceive(Context context, final Intent intent) {
        String raw_notification_data = intent.getExtras().getString(PARSE_DATA_KEY);
        Log.d(TAG, raw_notification_data);
        try{
            JSONObject j = new JSONObject(raw_notification_data);
            // TODO set a preference for getting notified when it goes offline
            if(!(j.has("is_live") && !j.getBoolean("is_live"))){
                super.onPushReceive(context, intent);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        if(!isMyServiceRunning(context)){
            Intent mServiceIntent = new Intent(context, WatchPushService.class);
            mServiceIntent.putExtra("raw_notification_data", raw_notification_data);
            context.startService(mServiceIntent);
//            new WatchNotifier(raw_notification_data, context);
        }else{
            Log.d(TAG, "Service is Already Running!!");
        }

    }

    private boolean isMyServiceRunning(Context ct) {
        ActivityManager manager = (ActivityManager) ct.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("gg.destiny.app.WatchPushService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
