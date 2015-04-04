package gg.destiny.app.platforms;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Hitbox extends Platform {
    @Override
    public JSONObject status(String channel) throws JSONException {
        JSONObject channelStatus = new JSONObject();

        String hburl = String.format("http://hitbox.tv/api/media/live/%s?showHidden=true", channel);
        String hbretval = HttpGet(hburl);
        // because hitbox is bad and does not actually 404 from bad channel names
        if(hbretval.startsWith("{")) {
            channelStatus = new JSONObject(hbretval).getJSONArray("livestream").getJSONObject(0);
        }
        return channelStatus;
    };

    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;

        try {
            LiveStream ls = new LiveStream();
            json = status(channel);
            // because hitbox is bad and does not actually 404 from bad channel names
            ls.live = json.length() > 0 && "1".equals(json.getString("media_is_live"));
            if(ls.live) {
                ls.title = json.getString("media_status");
            }
            return ls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, String> qualities(String channel){
        // hitbox is case insensitive
        channel = channel.toLowerCase();
        try {
            // unfortunately, getting the list of qualities
            // does not truthfully tell us if the stream is live
            // so we have to GET the status API too
            // TODO: cache this maybe
            JSONObject hbStatus = status(channel);

            if(hbStatus.length() > 0) {
                String liveStatus = hbStatus.getString("media_is_live");
                if ("1".equals(liveStatus)) {
                    String streams_url = String.format("http://api.hitbox.tv/player/hls/%s.m3u8", channel);
                    return parseQualitiesFromURL(streams_url);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    };
}
