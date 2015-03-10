package gg.destiny.app.platforms;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import gg.destiny.app.Business;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Twitch extends Platform {

    @Override
    public JSONObject status(String channel) throws JSONException {
        JSONObject channelStatus = new JSONObject();
        //maybe put this in another task...
        String strStatus = Business.HttpGet("https://api.twitch.tv/kraken/streams/" + channel);

        if(strStatus.length() == 0){
            return channelStatus;
        }
        JSONObject jsnStatus = new JSONObject(strStatus);
        if (!jsnStatus.isNull("stream")) {
            //statusMessage = "";
            channelStatus = jsnStatus.getJSONObject("stream").getJSONObject("channel");
        }

        return channelStatus;
    }

    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;

        try {
            json = status(channel);
            if(json != null && json.length() > 0 && json.has("status")){
                return new LiveStream(json.getString("status"), true);
            }else{
                return new LiveStream(channel+" is not live on Twitch", false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * steps
     * 1 GET token and sig for $channel
     * <p/>
     * String turl = "http://api.twitch.tv/api/channels/"+channel+"/access_token";
     * ex;
     * channel=dansgaming
     * =>
     * {"token":"{\"user_id\":null,\"channel\":\"dansgaming\",\"expires\":1389597116,\"chansub\":{\"view_until\":1924905600,\"restricted_bitrates\":[]},\"private\":{\"allowed_to_view\":true},\"privileged\":false}","sig":"77b105f2fb91892cb8908508a7e54c75c7a8d468","mobile_restricted":false}
     * <p/>
     * (note; dont url encode anything)
     * <p/>
     * 2 GET the quality options as a m3u8 list
     * <p/>
     * http://usher.justin.tv/api/channel/hls/CHANNEL.m3u8?token=TOKEN&sig=SIG
     * <p/>
     * ex, continued
     * <p/>
     * 3 choose a quality (url) from the list which is basically just a text file
     * comments in the file start with a #
     * otherwise the other lines are urls pointing to usable urls
     * ex,
     * <p/>
     * 4 provide a chosen quality setting to the VideoView
     */

    @Override
    public HashMap<String, String> qualities(String channel) {
        String auth = getTwitchAuth(channel);
        HashMap mQualities = new HashMap<String, String>();
        try {
            Log.d("Twitch Auth", auth);
            if (auth.length() == 0){
                return mQualities;
            }
            JSONObject authObj = new JSONObject(auth);
            String token = authObj.getString("token");
            String sig = authObj.getString("sig");

            token = Uri.encode(token);

            String qualitiesURL = "http://usher.justin.tv/api/channel/hls/" + channel + ".m3u8?token=" + token + "&sig=" + sig + "&allow_source=true";

            mQualities = parseQualitiesFromURL(qualitiesURL);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mQualities;
    }

    static String getTwitchAuth(String channel) {
        String authurl = "http://api.twitch.tv/api/channels/" + channel + "/access_token";
        return Business.HttpGet(authurl);
    }
}
