package gg.destiny.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.ParseInstallation;
import com.parse.ParsePush;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hayk on 4/21/2015.
 */
public class PushConfig {
    final static String PREFS_FILE = "notification_prefs";


    public static boolean get(Context cn) {
        SharedPreferences settings;
        settings = cn.getSharedPreferences(PREFS_FILE, 0);
        return settings.getBoolean("gets_live_notifications", false);
    }

    public static boolean set(Context cn, boolean newSetting){
        // TODO: modularize this so people can favorite different channels
        String channel = MainActivity.DEFAULT_PLATFORM + "-_-" + MainActivity.DEFAULT_CHANNEL;

        SharedPreferences prefs = cn.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putBoolean("gets_live_notifications", newSetting);

        Log.d("PushConfigData", "New Setting: " + String.valueOf(newSetting));

        if (newSetting){
            ParsePush.subscribeInBackground(channel);
        }else{
            ParsePush.unsubscribeInBackground(channel);
        }
        List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        if(subscribedChannels != null && subscribedChannels.size() > 0) {
            for (String item : subscribedChannels) {
                Log.d("SubbedChannels", item);
            }
        }else{
            Log.d("PushConfigData", "No Channels Retrieved!");
        }

        return edit.commit();
    }
}
