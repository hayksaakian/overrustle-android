package gg.destiny.app.platforms;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Ustream extends Platform {
    final String STATUS_ENDPOINT = "http://api.ustream.tv/channels/%s.json";
    final String STREAM_ENDPOINT = "http://iphone-streaming.ustream.tv/uhls/%s/streams/live/iphone/playlist.m3u8";
    @Override
    public JSONObject status(String channel) throws JSONException {
        // ustream channel names are just numbers (and channel names are different from
        // user names, so keep that in mind!!)
        // http://api.ustream.tv/channels/19068261.json
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
            if (json == null || json.length() == 0 || !json.has("channel")){
                ls.title = channel+" doesn't exist on ustream ";
                ls.live = false;
                return ls;
            }

            Log.d("USTREAM", json.toString());
            json = json.getJSONObject("channel");
            ls.live = "live".equals(json.getString("status"));
            ls.title = channel+" (aka "+json.getJSONObject("owner").getString("username")+") is offline";
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
