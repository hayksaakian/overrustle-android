package gg.destiny.app.platforms;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by hayk on 7/26/16.
 */
public class AngelThump extends Platform {

    final String STATUS_ENDPOINT = "http://angelthump.com/api/%s";
    final String STREAM_ENDPOINT = "http://cdn.angelthump.com/hls/%s.m3u8";

    @Override
    public JSONObject status(String channel) throws JSONException {
        String s = HttpGet(String.format(STATUS_ENDPOINT, channel));
        if (s.length() > 0){
            return new JSONObject(s);
        }else{
            return new JSONObject();
        }
    };

    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;
        try {
            LiveStream ls = new LiveStream();
            json = status(channel);

            if (json == null || json.length() == 0){
                ls.title = channel+" doesn't exist on AngelThump ";
                ls.live = false;
                return ls;
            }

            Log.d("ANGELTHUMP", json.toString());
            ls.live = json.getBoolean("live");
            ls.title = channel+" is offline";
            if (ls.live) {
                ls.title = json.getString("title");
            }
            return ls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, String> qualities(String channel){
        return parseQualitiesFromURL(String.format(STREAM_ENDPOINT, channel));
    };

}
