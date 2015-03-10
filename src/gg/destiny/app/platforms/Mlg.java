package gg.destiny.app.platforms;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Mlg extends Platform {


    // MLG API
//    http://tv.majorleaguegaming.com/channel/formal
//
//    step 1: resolve the name into an mlgXXX channel
//            "formal" -> "mlg60"
//    step 2: look up the mlgXXX channel to see if it's live
//             look for mlg60 in the data.items of the MLG_STREAMS_STATUS_URL endpoint
//    step 3: get the m3u8 playlist from the appropriate endpoint
//


    // http://streamapi.majorleaguegaming.com/service/streams/playback/mlg232?format=hls
    public final static String MLG_QUALITIES_ENDPOINT = "http://streamapi.majorleaguegaming.com/service/streams/playback/%s?format=hls";
    public final static String MLG_LIVE_ENDPOINT = "http://streamapi.majorleaguegaming.com/service/streams/all";
    public final static String MLG_NAME_RESOLVER_ENDPOINT = "http://www.majorleaguegaming.com/api/channels/all.js?fields=id,name,slug,subtitle,stream_name,image_16_9_medium,description";

    public final static String GAMEONGG_STREAM_NAME = "mlg17";

    JSONArray cachedStatuses = null;
    long lastStatusFetch = 0l;
    long MAX_AGE = 45l;

    JSONArray getStatuses(){
        long ct = System.currentTimeMillis() / 1000L;
        if(cachedStatuses != null && lastStatusFetch > MAX_AGE+ct){
            return cachedStatuses;
        }
        String mlgStatuses = HttpGet(MLG_NAME_RESOLVER_ENDPOINT);
        try {
            cachedStatuses = new JSONObject(mlgStatuses)
                    .getJSONObject("data")
                    .getJSONArray("items");
            lastStatusFetch = ct;
            return cachedStatuses;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    String resolvedName(String channel) throws JSONException {
        JSONArray json = getStatuses();
        int l = json.length();
        for (int i = 0; i < l; i++) {
            JSONObject item = json.getJSONObject(i);
            if (item.getString("name").toLowerCase().equals(channel.toLowerCase()) ||
                    item.getString("slug").toLowerCase().equals(channel.toLowerCase()) ) {
                return item.getString("stream_name");
            }
        }

        return channel;
    }

    @Override
    public JSONObject status(String channel) throws JSONException {
        if (!channel.startsWith("mlg")){
            channel = resolvedName(channel);
        }
        JSONArray json = getStatuses();
        int l = json.length();
        for (int i = 0; i < l; i++) {
            JSONObject item = json.getJSONObject(i);
            if (item.getString("stream_name").equals(channel)) {
                return item;
            }
        }
        return new JSONObject();
    };

    @Override
    public LiveStream liveStatus(String channel){
        JSONObject json = null;

        try {
            json = status(channel);
            String status =  channel + " is offline";
            if(json == null || json.length() == 0 || !json.has("stream_name")){
                return new LiveStream(status, false);
            }
            Log.d("MLG DEBUG", json.toString());
            String mlgchannel = json.getString("stream_name");
            // there are cases where the live endpoint will return a 503 json object
            // even though the other APIs claim the stream is Live
            String sLiveStatuses = HttpGet(MLG_LIVE_ENDPOINT);
            JSONArray items = new JSONObject(sLiveStatuses)
                    .getJSONObject("data")
                    .getJSONArray("items");
            int l = items.length();
            boolean isLive = false;
            for (int i = 0; i < l; i++) {
                JSONObject item = items.getJSONObject(i);
                if (item.getString("stream_name").equals(mlgchannel)) {
                    isLive = item.getInt("status") == 1;
                    break;
                }
            }
            if (isLive){
                status = json.getString("subtitle");
            }
            return new LiveStream(status, isLive);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public HashMap<String, String> qualities(String channel){
        HashMap<String, String> mQualities = new HashMap<String, String>();
        try {
            JSONObject cStatus = status(channel);
            if(cStatus == null || cStatus.length() == 0 || !cStatus.has("stream_name")){
                return mQualities;
            }
            String mlgchannel = cStatus.getString("stream_name");

            String sLiveStatuses = HttpGet(MLG_LIVE_ENDPOINT);
            JSONObject rootjson = new JSONObject(sLiveStatuses);
            if(rootjson.getInt("status_code") >= 400){
                return mQualities;
            }
//            String sQualities = HttpGet(String.format(MLG_QUALITIES_ENDPOINT, channel));
            JSONArray items = rootjson
                    .getJSONObject("data")
                    .getJSONArray("items");

            int l = items.length();
            for (int i = 0; i < l; i++) {
                JSONObject item = items.getJSONObject(i);
                if (item.getString("stream_name").equals(mlgchannel)) {
                    int iStatus = item.getInt("status");
                    boolean isLive = iStatus == 1;
                    if (isLive) {
                        String sQualities = HttpGet(MLG_QUALITIES_ENDPOINT);
                        JSONArray qItems = new JSONObject(sQualities)
                                .getJSONObject("data")
                                .getJSONArray("items");
                        JSONObject qItem = qItems.getJSONObject(0);
                        return parseQualitiesFromURL(qItem.getString("url"));
//                    } else if (iStatus == 2) {
                        // from replay
//                        status = GAMEONGG_GENERIC_STATUS + " [rebroadcast]";
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  mQualities;
    };
}
