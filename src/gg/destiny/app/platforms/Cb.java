package gg.destiny.app.platforms;

import android.util.Log;

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
public class Cb extends Platform {
    @Override
    public JSONObject status(String channel) throws JSONException {
        String hburl = String.format("https://chaturbate.com/contest/log_presence/%s", channel);
        String hbretval = HttpGet(hburl);
        if(hbretval.length() == 0){
            return new JSONObject();
        }
        return new JSONObject(hbretval);
    };

    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;

        try {
            LiveStream ls = new LiveStream();
            json = status(channel);
            ls.live = json != null && (json.length() > 0) && !(json.get("total_viewers") instanceof Integer) && (Integer.parseInt(json.getString("total_viewers")) > 0);
            if(ls.live){
                ls.title = channel + " NSFW for " + json.getString("total_viewers") + " viewers";
            }
            return ls;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, String> qualities(String channel){
        HashMap mQualities = new HashMap<String, String>();

        try {
            JSONObject jsno = status(channel);
            if ((jsno.length() > 0) && !(jsno.get("total_viewers") instanceof Integer) && (Integer.parseInt(jsno.getString("total_viewers")) > 0)) {
            }else{
                return mQualities;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String rawhtml = Business.HttpGet("https://www.chaturbate.com/" + channel);

        Pattern pattern = Pattern.compile("http.*playlist.m3u8", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rawhtml);
        if(matcher.find()){
            Log.d("found matches", "some matches");
        }else{
            Log.d("found matches", "no/0 matches");
            return mQualities;
        }

        String qualitiesURL  = matcher.group(0);

        // their qualities are not prefixed by a proper url
        mQualities = parseQualitiesFromURL(qualitiesURL, "chunklist");

        List<String> list = new ArrayList<String>();
        list.addAll(mQualities.keySet());

        String urlprefix = qualitiesURL.replace("playlist.m3u8", "");

        for (int i = 0; i < list.size(); i++) {
            String mq = list.get(i);
            String mqpath = (String)mQualities.get(mq);
            mQualities.put(mq, urlprefix + mqpath);
        }

        return mQualities;
    }
}
