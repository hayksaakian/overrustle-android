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


    public static boolean getHandset(Context cn) {
        SharedPreferences settings;
        settings = cn.getSharedPreferences(PREFS_FILE, 0);
        return settings.getBoolean("gets_live_notifications", false);
    }

    public static boolean getWear(Context cn) {
        SharedPreferences settings;
        settings = cn.getSharedPreferences(PREFS_FILE, 0);
        return settings.getBoolean("gets_subtle_notifications", false);
    }
    public static boolean setWear(Context cn, boolean newSetting){
        // TODO: modularize this so people can favorite different channels
        SharedPreferences prefs = cn.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putBoolean("gets_subtle_notifications", newSetting);

        Log.d("PushConfigData", "New Wear Setting: " + String.valueOf(newSetting));
        return edit.commit();
    }

    public static boolean setHandset(Context cn, boolean newSetting){

        SharedPreferences prefs = cn.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor edit = prefs.edit();

        edit.putBoolean("gets_live_notifications", newSetting);
        Log.d("PushConfigData", "New Handset Setting: " + String.valueOf(newSetting));

        return edit.commit();
    }
}
