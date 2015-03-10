package gg.destiny.app.platforms;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gg.destiny.app.Business;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Azubu extends Platform{
    // this seems like a constant, but might not be
    public static String AZUBU_PUB_ID = "3361910549001";

    @Override
    public JSONObject status(String channel) throws JSONException {
        String hburl = String.format("http://www.azubu.tv/api/video/active-stream/%s", channel);
        String hbretval = HttpGet(hburl);
        JSONObject json = new JSONObject(hbretval);
        if(json.getInt("total") == 0){
            return new JSONObject();
        }
        return json.getJSONArray("data").getJSONObject(0);
    };


    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;

        try {
            LiveStream ls = new LiveStream();
            json = status(channel);
            ls.live = json.length() > 0 && json.has("user") && json.getJSONObject("user").getJSONObject("channel").getBoolean("is_live");
            if (ls.live){
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
        try {
            JSONObject status = status(channel);
            if (status.length() > 0) {
                return getAzubuQualities(channel, status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    };


    public static HashMap getAzubuQualities(String channel, JSONObject status) {
        HashMap mQualities = new HashMap<String, String>();
        try {

            String keyContainingHtml = Business.HttpGet("http://www.azubu.tv/" + channel);
            // found here:
            // https://github.com/chrippa/livestreamer/blob/da58e4a05405e0e23bbefb1f9d83a6dd88d1d454/src/livestreamer/plugins/azubutv.py#L135
            Pattern pattern = Pattern.compile("name=\"playerKey\" value=\"(.+)\"");
            Matcher matcher = pattern.matcher(keyContainingHtml);
            matcher.find();

            String playerKey = matcher.group(1);
            String refId = "video" + status.getString("id") + "CH" + channel.replace("_", "");

            String streamApiUrl = "http://c.brightcove.com/services/json/player/media/?command=find_media_by_reference_id" +
                    "&playerKey=" + playerKey +
                    "&refId=" + refId +
                    "&pubId=" + AZUBU_PUB_ID;

            String sStreamApiData = Business.HttpGet(streamApiUrl);
            JSONObject json = new JSONObject(sStreamApiData);
            String qualitiesURL = json.getString("FLVFullLengthURL");
            if (!qualitiesURL.endsWith("m3u8")){
                qualitiesURL = json.getJSONArray("IOSRenditions").getJSONObject(0).getString("defaultURL");
            }

            // their qualities are not prefixed by a proper url
            mQualities = parseQualitiesFromURL(qualitiesURL, refId);

            List<String> list = new ArrayList<String>();
            list.addAll(mQualities.keySet());

            String urlprefix = qualitiesURL.replace(refId+".m3u8", "");

            for (int i = 0; i < list.size(); i++) {
                String mq = list.get(i);
                String mqpath = (String)mQualities.get(mq);
                mQualities.put(mq, urlprefix + mqpath);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mQualities;
    }
}
