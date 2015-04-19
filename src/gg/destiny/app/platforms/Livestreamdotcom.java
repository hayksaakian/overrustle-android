package gg.destiny.app.platforms;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Hayk on 4/18/2015.
 */
public class Livestreamdotcom extends Platform  {
    final String STATUS_ENDPOINT = "http://x%sx.api.channel.livestream.com/2.0/widgetinfo.json?cachebuster=";
    final String STREAM_ENDPOINT = "http://x%sx.api.channel.livestream.com/3.0/playlist.m3u8";

//    http://xwfmz-trafficx.api.channel.livestream.com/3.0/getstream.json?r=0.4814788515213877

    @Override
    public JSONObject status(String channel) throws JSONException {
        String s = HttpGet(String.format(STATUS_ENDPOINT, channel.toLowerCase().replaceAll("_", "-"))+String.valueOf(System.currentTimeMillis()));
//        TODO: might need to catch a 404 page. But probably not.
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
            // TODO: test the judge judy stream to see if it still plays from replays
            // even thought the m3u8 stream is not provided
            if (json == null || json.length() == 0 || !json.has("channel")){
                ls.title = channel+" doesn't exist on ustream ";
                ls.live = false;
                return ls;
            }

            Log.d("LiveStream", json.toString());
            json = json.getJSONObject("channel");
            // TODO: might have to change this is this is unreliable
            ls.live = json.getInt("currentViewerCount") > 0;
            ls.title = channel+" (aka "+json.getString("title")+") is offline";
            if (ls.live) {
                // TODO: consider changing this back to getting the 'title'
                ls.title = json.getString("description");
            }
            return ls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, String> qualities(String channel){
        return parseQualitiesFromURL(String.format(STREAM_ENDPOINT, channel.toLowerCase().replaceAll("_", "-")));
    };
}
