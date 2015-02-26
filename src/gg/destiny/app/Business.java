package gg.destiny.app;

import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.NotificationCompat;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;;
import org.apache.http.*;

//import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.json.*;

import gg.destiny.app.support.NavigationDrawerFragment;

public class Business {
    final static String GAMEONGG_QUALITIES_URL = "http://mlghds-lh.akamaihd.net/i/mlg17_1@167001/master.m3u8";
    final static String MLG_STREAMS_STATUS_URL = "http://streamapi.majorleaguegaming.com/service/streams/all";
    final static String GAMEONGG_STREAM_NAME = "mlg17";
    final static String GAMEONGG_GENERIC_STATUS = "MLG GameOn.gg SC2 Invitational";

    //	final static String DESTINY_EMOTICON_CSS_ENDPOINT = "http://cdn.destiny.gg/1.25.3/chat/css/emoticons.css";

    Business() {
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

    public class LiveChecker extends AsyncTask<String, Void, String> {
        Activity mActivity;
        //TextView header;
        Button goToStreamButton;
        boolean isLive = false;
        String channelname = "";
        //EditText channelSearch;

        @Override
        protected String doInBackground(String... urls) {
            String mStatus = null;
            for (String url_or_channel : urls) {
                mStatus = checkStream(url_or_channel);
                if (isLive) {
                    break; // we found a live stream, stop checking
                }
            }

            return mStatus;
        }

        @Override
        protected void onPostExecute(String liveStatus) {

            //context.setTitle
            if (liveStatus != null)
                mActivity.setTitle(liveStatus);

            if (goToStreamButton != null) {

                if (isLive) {
                    goToStreamButton.setVisibility(View.VISIBLE);
                    goToStreamButton.setText(channelname + " is live. Watch Now");
                } else {
                    goToStreamButton.setVisibility(View.GONE);
                }
            }
        }

        protected String checkStream(String url_or_channel_name) {
            String status = url_or_channel_name + " is offline or the app is broken";
            try {
                if (url_or_channel_name.equals("gameongg")) {
                    url_or_channel_name = MLG_STREAMS_STATUS_URL;
                }
                JSONObject jsno;
                if (url_or_channel_name.contains("mlg") || url_or_channel_name.contains("majorleaguegaming")) {
                    String mlgStatuses = HttpGet(url_or_channel_name);
                    channelname = "GameOn.gg";
                    JSONArray jsna = new JSONObject(mlgStatuses)
                            .getJSONObject("data")
                            .getJSONArray("items");
                    int l = jsna.length();
                    for (int i = 0; i < l; i++) {
                        JSONObject item = jsna.getJSONObject(i);
                        if (item.getString("stream_name").equals(GAMEONGG_STREAM_NAME)) {

                            int iStatus = item.getInt("status");
                            isLive = iStatus == 1;
                            if (isLive) {
                                status = GAMEONGG_GENERIC_STATUS;
                            } else if (iStatus == 2) {
                                // from replay
                                status = GAMEONGG_GENERIC_STATUS + " [rebroadcast]";
                            } else {
                                status = "GameOn.gg might be offline. Tell hephaestus if this is wrong.";
                            }
                            break;
                        }
                    }

                } else if (url_or_channel_name.matches("-?\\d+(\\.\\d+)?")) {
                    // ustream channel names are just numbers (and channel names are different from
                    // user names, so keep that in mind!!)
                    // http://api.ustream.tv/channels/19068261.json
                    String usurl = String.format("http://api.ustream.tv/channels/%s.json", url_or_channel_name);
                    JSONObject usStatus = new JSONObject(HttpGet(usurl));
                    String liveStatus = usStatus.getJSONObject("channel").getString("status");
                    isLive = "live".equals(liveStatus);
                    if (isLive) {
                        usStatus.getJSONObject("channel").getJSONObject("stream").getString("hls");
                        status = usStatus.getJSONObject("channel").getString("title");
                    }

                } else {
                    // 1) check twitch
                    jsno = getTwitchStatus(url_or_channel_name);
                    channelname = url_or_channel_name;
                    isLive = jsno != null && jsno.length() > 0;
                    if (isLive) {
                        return jsno.getString("status");
                    }
                    // 2) check hitbox unless twitch was good
                    jsno = getHitboxStatus(url_or_channel_name);
                    // because hitbox is bad and does not actually 404 from bad channel names
                    isLive = jsno.length() > 0 && "1".equals(jsno.getString("media_is_live"));
                    if(isLive) {
                        return jsno.getString("media_status");
                    }
                    // 3) check azubu
                    jsno = getAzubuStatus(url_or_channel_name);
                    isLive = jsno.length() > 0;
                    if (isLive){
                        return jsno.getString("title");
                    }
                    // 4) check cb
                    jsno = getCBStatus(url_or_channel_name);
                    isLive = (jsno.length() > 0) && !(jsno.get("total_viewers") instanceof Integer) && (Integer.parseInt(jsno.getString("total_viewers")) > 0);
                    if(isLive){
                        return channelname + " NSFW for " + jsno.getString("total_viewers") + " viewers";
                    }

                    return channelname + " is offline. Type another channel\'s name below to watch something else.";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return status;
        }
    }

    public static class DownloadTask extends AsyncTask<String, Void, HashMap> {
        Activity mActivity;
        public Spinner qualityPicker;
        public int spinner_item;
        public ResizingVideoView video;
        public Context context;

        String channel;
        JSONObject channelStatus;

        public TextView header;
        //public EditText channelSearch;

        public HashMap qualities;


        @Override
        protected HashMap doInBackground(String... channels) {
            HashMap newQualities = new HashMap<String, String>();
            //String url = "http://pubapi.cryptsy.com/api.php?method=singleorderdata&marketid=132";
            //String retval = "";
            channel = channels[0];
            //String url = "";
//  TODO: instead of checking every platform, use a dict and only check the relevant platorm
            if (channel.equals("gameongg")) {
                newQualities = parseQualitiesFromURL(GAMEONGG_QUALITIES_URL);
                if (newQualities.size() > 0) {
                    channelStatus = new JSONObject();
                    try {
                        channelStatus.put("status", GAMEONGG_GENERIC_STATUS);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (channel.matches("-?\\d+(\\.\\d+)?")) {
                // ustream channels are just numbers
                // (and channels are different from user ids
                String usUrl = String.format("http://iphone-streaming.ustream.tv/uhls/%s/streams/live/iphone/playlist.m3u8", channel);
                newQualities = parseQualitiesFromURL(usUrl);
            } else {
                String auth = getTwitchAuth(channel);
                newQualities = getTwitchQualities(channel, auth);

                if(newQualities.size() > 0){
                    return newQualities;
                }

                // check hitbox if twitch doesn't have anything
                try {
                    // unfortunately, getting the list of qualities
                    // does not truthfully tell us if the stream is live
                    // so we have to GET the status API too
                    // TODO: cache this maybe
                    JSONObject hbStatus = getHitboxStatus(channel);

                    if(hbStatus.length() > 0) {
                        String liveStatus = hbStatus.getString("media_is_live");
                        if ("1".equals(liveStatus)) {
                            String streams_url = String.format("http://api.hitbox.tv/player/hls/%s.m3u8", channel);
                            newQualities = parseQualitiesFromURL(streams_url);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(newQualities.size() > 0){
                    return newQualities;
                }

                // check azubu if hitbox doesn't have anything
                newQualities = getAzubuQualities(channel);

                if(newQualities.size() > 0){
                    return newQualities;
                }

                // check cb if azubu doesn't have anything
                newQualities = getCBQualities(channel);

                if(newQualities.size() > 0){
                    Log.d("testing", "found some for CB");
                    return newQualities;
                }

            }

            //String readURL = HttpGet(url);
            //String readURL = "[1, 2]";
            return newQualities;
        }

        //needs a list of quality options
        @Override
        protected void onPostExecute(HashMap foundQualities) {
            qualities = foundQualities;
            Log.d("found qualities #", String.valueOf(qualities.size()));

            if (qualities.size() > 0) {
                LoadQualities(qualityPicker, qualities, context, spinner_item);
//				if(qualityPicker.)
            }
            SetCachedHash(channel + "|cache", qualities, context);

            // get the stream status
            Business nb = new Business();
            LiveChecker lc = nb.new LiveChecker();
            lc.mActivity = mActivity;
            lc.execute(channel);
        }
        //Note, url should be good and proper before hand

    }

    public static Response RawHttpGet(String url){
        Log.d("GET ing with OkHTTP", url);

        final OkHttpClient client = new OkHttpClient();
        try{
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String HttpGet(String url){
        Response r = RawHttpGet(url);
        if(r == null){
            return "";
        }
        if(r.isSuccessful()) {
            try {
                return r.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(Business.class.toString(), "Failed to download file");
        return "";

    }

    // TODO: Deprecate
    public static String OldHttpGet(String url) {
        Log.d("GET ing", url);
        //return "[200]";}
        //public String baddoGet(String url) {

        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(Business.class.toString(), "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public static HashMap getTwitchQualities(String channel, String auth) {
        HashMap mQualities = new HashMap<String, String>();
        try {
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


    public static HashMap getCBQualities(String channel) {
        try {
            JSONObject jsno = getCBStatus(channel);
            if ((jsno.length() > 0) && !(jsno.get("total_viewers") instanceof Integer) && (Integer.parseInt(jsno.getString("total_viewers")) > 0)) {
                return getCBQualities(channel, jsno);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    }

    public static HashMap getCBQualities(String channel, JSONObject status) {
        HashMap mQualities = new HashMap<String, String>();

        String rawhtml = HttpGet("https://www.chaturbate.com/"+channel);

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

    public static HashMap getAzubuQualities(String channel) {
        try {
            JSONObject status = getAzubuStatus(channel);
            if (status.length() > 0) {
                return getAzubuQualities(channel, status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new HashMap<String, String>();
    }

    // this seems like a constant, but might not be
    public static String AZUBU_PUB_ID = "3361910549001";

    public static HashMap getAzubuQualities(String channel, JSONObject status) {
        HashMap mQualities = new HashMap<String, String>();
        try {

            String keyContainingHtml = HttpGet("http://www.azubu.tv/"+channel);
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

            String sStreamApiData = HttpGet(streamApiUrl);
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

    public static JSONObject getCBStatus(String channel) throws JSONException{
        String hburl = String.format("https://chaturbate.com/contest/log_presence/%s", channel);
        String hbretval = HttpGet(hburl);
        if(hbretval.length() == 0){
            return new JSONObject();
        }
        return new JSONObject(hbretval);
    }

    public static JSONObject getAzubuStatus(String channel) throws JSONException{
        String hburl = String.format("http://www.azubu.tv/api/video/active-stream/%s", channel);
        String hbretval = HttpGet(hburl);
        JSONObject json = new JSONObject(hbretval);
        if(json.getInt("total") == 0){
            return new JSONObject();
        }
        return json.getJSONArray("data").getJSONObject(0);
    }



    public static JSONObject getHitboxStatus(String channel) throws JSONException{
        JSONObject channelStatus = new JSONObject();

        String hburl = String.format("http://hitbox.tv/api/media/live/%s?showHidden=true", channel);
        String hbretval = HttpGet(hburl);
        // because hitbox is bad and does not actually 404 from bad channel names
        if(hbretval.startsWith("{")) {
            channelStatus = new JSONObject(hbretval).getJSONArray("livestream").getJSONObject(0);
        }
        return channelStatus;
    }

    public static JSONObject getTwitchStatus(String channel) throws JSONException {
        JSONObject channelStatus = new JSONObject();
        //maybe put this in another task...
        String strStatus = HttpGet("https://api.twitch.tv/kraken/streams/" + channel);

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

    public static void PlayURL(ResizingVideoView video, String url) {
        video.setVisibility(View.VISIBLE);
        video.showProgress();
        //video.progressBar.setVisibility(View.VISIBLE);
        video.setVideoURI(Uri.parse(url));

        //video.setMediaController(new MediaController(context));
        video.requestFocus();
        video.start();
    }

    public static String getTwitchAuth(String channel) {
        String authurl = "http://api.twitch.tv/api/channels/" + channel + "/access_token";
        return HttpGet(authurl);
    }

    public static HashMap parseQualitiesFromURL(String url) {
        return parseQualitiesFromURL(url, "http");
    }
    public static HashMap<String, String> parseQualitiesFromURL(String url, String urlPrefix) {
        HashMap mQualities = new HashMap<String, String>();
        String qualityOptions = HttpGet(url);
//        Log.d("Quality Response", qualityOptions);
        String line = null;
        try {
            String lastquality = null;
            qualityOptions = qualityOptions.replace("#", "\n#").replace("http", "\nhttp");

            String[] opA = qualityOptions.split("\n");
//            Log.d("Quality Response Lines", Integer.toString(opA.length));
            for (int i = 0; i < opA.length; i++) {
                line = opA[i];
                if (line.length() > 0) {
//                    Log.d("Quality Response Line", line);
                    if (line.startsWith("#EXT-X-STREAM")) {
                        // descriptor, therefore parse quality
                        // example
                        // #EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID="high",NAME="High",AUTOSELECT=YES,DEFAULT=YES

                        // ustream example:
                        // #EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=352519, CODECS="avc1.77.30, mp4a.40.2",RESOLUTION=640x360

                        HashMap<String, String> info = new HashMap<String, String>();
                        String[] parts = line.split(",");
                        for (String part : parts) {
                            String[] pieces = part.split("=");
                            if (pieces.length < 2) {
                                continue;
                            }
                            if (pieces[1].contains("\"")) {
                                pieces[1] = pieces[1].split("\"")[1];
                            }
                            info.put(pieces[0], pieces[1]);
                        }
                        if (info.containsKey("NAME")) {
                            lastquality = info.get("NAME");
                        } else if (info.containsKey("RESOLUTION")) {
                            lastquality = info.get("RESOLUTION");
                        } else if (info.containsKey("VIDEO")) {
                            lastquality = info.get("VIDEO");
                        } else {
                            lastquality = parts[parts.length - 1];
                        }
                        if (mQualities.containsKey(lastquality) && info.containsKey("BANDWIDTH")) {
                            int bw = Integer.parseInt(info.get("BANDWIDTH"));
                            lastquality = lastquality + "@" + Integer.toString(bw / 1000) + "kbps";
                        }else if(lastquality.contains("BANDWIDTH=")){
                            int bw = Integer.parseInt(info.get("BANDWIDTH"));
                            lastquality = Integer.toString(bw / 1000) + "kbps";
                        }

                        Log.d("found quality", lastquality);
                    } else if (line.startsWith(urlPrefix) && lastquality != null) {
                        // we need to pull the quality name out of the URL for hitbox m3u8 streams
                        if(line.contains("hitbox.tv/hls/") && line.contains("/index.m3u8")){
                            lastquality = line.substring(line.indexOf("hitbox.tv/hls/") + 1, line.indexOf("/index.m3u8"));
                            if (lastquality.contains("_")){
                                String[] parts = lastquality.split("_");
                                lastquality = parts[parts.length-1];
                            }
                        }
                        Log.d("quality url", line);
                        mQualities.put(lastquality, line);
                        lastquality = null;
                    }
                }
//				Log.d("raw qualities", line);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mQualities.size() == 0){
            Log.d("Qualities", "Could not parse any qualities from the following response:");
            Log.d("Qualities", qualityOptions);
        }
        return mQualities;
    }

    public static void LoadQualities(Spinner qualityPicker, HashMap qualities, Context context, int spinnerItemId) {
        List<String> list = new ArrayList<String>();

        list.addAll(qualities.keySet());

        //sort list
        java.util.Collections.sort(list, Collator.getInstance());

        list.add("Audio Only");
        list.add("Chat Only");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, spinnerItemId, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualityPicker.setAdapter(dataAdapter);
    }




    static public boolean SetCachedHash(String key, HashMap value, Context cn) {
        Log.d("business", "caching qualities=" + String.valueOf(value.size()));
        SharedPreferences prefs = cn.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor edit = prefs.edit();
        JSONObject jsn = new JSONObject(value);

        edit.putString(key, String.valueOf(jsn));
        return edit.commit();
    }

    static public boolean SetCachedArray(String key, String[] array, Context cn) {
        Log.d("business", "caching array key=" + key + " of length " + String.valueOf(array.length));
        SharedPreferences prefs = cn.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor edit = prefs.edit();

        Set<String> set = new HashSet<String>(Arrays.asList(array));
        //edit.putString(key, String.valueOf(jsn));
        edit.putStringSet(key, set);
        return edit.commit();

    }

    static public HashMap GetCachedHash(String key, Context cn) {
        HashMap hm = new HashMap<String, String>();

        SharedPreferences settings;
        settings = cn.getSharedPreferences("prefs", 0);
        //get the sharepref
        String rawhm = settings.getString(String.valueOf(key), "");
        if (rawhm != null && !rawhm.equals("")) {
            try {
                JSONObject jsnhm = new JSONObject(rawhm);
                //Log.d(LOG, rawhm);
                if (jsnhm.length() > 0) {
                    JSONArray nms = jsnhm.names();
                    int klength = nms.length();
                    for (int i = 0; i < klength; i++) {
                        String nm = nms.getString(i);
                        String vl = jsnhm.getString(nm);
                        hm.put(nm, vl);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return hm;
    }

    static public String[] GetCachedArray(String key, Context cn) {
        String[] retval = {};

        SharedPreferences settings;
        settings = cn.getSharedPreferences("prefs", 0);
        //get the sharepref
        Set<String> rawarr = settings.getStringSet(key, null);

        rawarr.toArray(retval);

        return retval;
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    // Get a list of other streams to show in the navigation drawer
    private static final OkHttpClient rustleClient = new OkHttpClient();

    static void GetRustlers(final Activity activity, final NavigationDrawerFragment frag){
        final String api_endpoint = "http://api.overrustle.com/api";

        Request request = new Request.Builder()
                .url(api_endpoint)
                .build();

        Call call = rustleClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Request request, IOException e) {
                Log.d("HTTP FAIL", "Failed to execute " + request, e);
            }

            @Override public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                final String rv = response.body().string();
                final List<Pair<String, String>> mm = ParseJsonToList(rv);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    frag.setLabelValueList(mm);
                    }
                });
            }
        });
    }

    static List<Pair<String, String>> ParseJsonToList(String jString) {
        List<Pair<String, String>> retval = new ArrayList<Pair<String, String>>();

        try {
            JSONObject j = new JSONObject(jString);
            retval = ParseJsonToList(j);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }


    public static List<Pair<String, String>> ParseJsonToList(JSONObject j) {
        List<Pair<String, String>> retval = new ArrayList<Pair<String, String>>();

        try {
            JSONObject metadata = j.getJSONObject("metadata");
            JSONObject metaindex = j.getJSONObject("metaindex");

            int totalviewers = (Integer) j.get("viewercount");

            // loop array
            JSONObject streamsObj = (JSONObject) j.get("streams");
            Iterator<String> streams = streamsObj.keys();
            List<Pair<String, Integer>> sorted = new ArrayList<Pair<String, Integer>>();
            while ((streams.hasNext())){
                String key = streams.next();
                sorted.add(new Pair<String, Integer>(key, streamsObj.getInt(key)));
            }
            Collections.sort(sorted, new Comparator<Pair<String, Integer>>() {
                @Override
                public int compare(Pair<String, Integer> lhs, Pair<String, Integer> rhs) {
                    return Integer.valueOf(lhs.second).compareTo(Integer.valueOf(rhs.second));
                }
            });
            Collections.reverse(sorted);
//            retval[0] = "Destiny";
//            int i = 0;
            retval.add(new Pair<String, String>(Integer.toString(totalviewers) +" Rustlers Watching:", "1"));
            retval.add(new Pair<String, String>("Destiny", "destiny"));
//            if(streamsObj.length() > 0){
//                hm.clear();
//            }
            Iterator<Pair<String, Integer>> sortedstreams = sorted.iterator();
            while (sortedstreams.hasNext()) {
//                i = i + 1;
                Pair<String, Integer> pair = sortedstreams.next();
                String key = pair.first;
//                TODO: replace with a less naive implementation in case we add querystring parameters
                String[] parts = key.split("=");
                if(parts.length > 0) {
                    String stream = parts[parts.length - 1];
                    String streamid = stream;
                    //retval[i] = stream;//
                    if(key.startsWith("/channel")){
                        JSONObject md = metadata.getJSONObject(metaindex.getString(key));
                        streamid = md.getString("channel");
                    }
                    retval.add(new Pair<String, String>(String.format("%s %s", Integer.toString(pair.second), stream), streamid));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }




    // Push Notifications!!


    public static final int NOTIFICATION_ID = 1;

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message .
    public static void sendNotification(Context context, String msg) {
        String title = "Live Notification";
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, MainActivity.DEFAULT_CHANNEL);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
//        .setVibrate(new long[]{1l})
//        .setSound("some uri?")
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
