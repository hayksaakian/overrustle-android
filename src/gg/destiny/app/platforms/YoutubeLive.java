package gg.destiny.app.platforms;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by Hayk on 5/11/2015.
 */
public class YoutubeLive  extends Platform {
    final String STATUS_ENDPOINT = "http://youtube.com/get_video_info?el=player_embedded&video_id=%s";

    final String TAG = "YoutubeLiveAPI";
    // TODO: we may need to set headers or something to make this work

    // TODO: use the official google youtube api V3 to pull this information
    @Override
    public JSONObject status(String channel) throws JSONException {
        JSONObject json = new JSONObject();

        String s = HttpGet(String.format(STATUS_ENDPOINT, channel));

        if (s.length() == 0){
            Log.d(TAG, "Empty string from API");
            return new JSONObject();
        }

        String[] parts = s.split("&");

        for(String p : parts){
            if(p.startsWith("livestream")){
                json.put("live", p.equals("livestream=1"));
            }
            // TODO: pull the title from the real API
            if(p.startsWith("author")){
                json.put("title", p.split("=")[1]+" live on YouTube");
            }
        }
        if(!json.has("live") || json.getBoolean("live") == false){
            Log.d(TAG, "no live stream found in API");
            return new JSONObject();
        }

        return json;
    }

    @Override
    public LiveStream liveStatus(String channel){
        try{
            JSONObject json = status(channel);
            return new LiveStream(json.getString("title"), json.getBoolean("live"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, String> qualities(String channel){

        String s = HttpGet(String.format(STATUS_ENDPOINT, channel));
        if(s.length() == 0 || !s.contains("m3u8")){
            Log.d(TAG, "No m3u8 playlist in API");
            return new HashMap<String, String>();
        }

        String[] parts = s.split("&");

        String playlistUrl = "";
        for(String p : parts){
            if (p.startsWith("hlsvp")){
                Log.d(TAG, "Found HLS Url: "+p);
                try {
                    playlistUrl = java.net.URLDecoder.decode(p.split("=")[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        if (playlistUrl.length() == 0){
            Log.d(TAG, "Could not find m3u8 named variable");
            return new HashMap<String, String>();
        }
        Log.d(TAG, "Playlist URL: " + playlistUrl);
        return parseQualitiesFromURL(playlistUrl);
    };
}
