package gg.destiny.app;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
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
;

//import org.apache.http.*;
import org.json.*;

import gg.destiny.app.platforms.Azubu;
import gg.destiny.app.platforms.Cb;
import gg.destiny.app.platforms.Hitbox;
import gg.destiny.app.platforms.LiveStream;
import gg.destiny.app.platforms.Livestreamdotcom;
import gg.destiny.app.platforms.Metadata;
import gg.destiny.app.platforms.Mlg;
import gg.destiny.app.platforms.Platform;
import gg.destiny.app.platforms.Twitch;
import gg.destiny.app.platforms.Ustream;
import gg.destiny.app.platforms.YoutubeLive;
import gg.destiny.app.support.NavigationDrawerFragment;

public class Business {
    final static Platform PlatformAPI = new Twitch();
    final static Twitch TwitchAPI = new Twitch();
    final static Ustream UstreamAPI = new Ustream();
    final static Azubu AzubuAPI = new Azubu();
    final static Hitbox HitboxAPI = new Hitbox();
    final static Mlg MlgAPI = new Mlg();
    final static Livestreamdotcom LivestreamAPI = new Livestreamdotcom();
    final static YoutubeLive YoutubeAPI = new YoutubeLive();
    final static Cb CbAPI = new Cb();

    final static Platform[] PLATFORM_LIST = {
            TwitchAPI,
            UstreamAPI,
            AzubuAPI,
            HitboxAPI,
            MlgAPI,
            LivestreamAPI,
            YoutubeAPI,
            CbAPI
    };

    final static Map<String, Platform> Platforms;
    static
    {
//        WONTDO: castamp
//        TODO: twitch vods
//        TODO: youtube
//        https://developers.google.com/youtube/android/player/
//        https://developers.google.com/youtube/android/player/downloads/YouTubeAndroidPlayerApi-1.2.1.zip
//        TODO: youtube playlist
//        TODO: dailymotion
//        TODO: picarto
        Platforms = new HashMap<String, Platform>();
        Platforms.put("twitch", TwitchAPI);
        Platforms.put("ustream", UstreamAPI);
        Platforms.put("azubu", AzubuAPI);
        Platforms.put("hitbox", HitboxAPI);
        Platforms.put("mlg", MlgAPI);
        Platforms.put("livestream", LivestreamAPI);
        Platforms.put("youtube", YoutubeAPI);
        Platforms.put("nsfw-chaturbate", CbAPI);

    }

    //	final static String DESTINY_EMOTICON_CSS_ENDPOINT = "http://cdn.destiny.gg/1.25.3/chat/css/emoticons.css";

    Business() {
    }

    public class LiveChecker extends AsyncTask<String, Void, LiveStream> {
        Activity mActivity;
        //TextView header;
        Button goToStreamButton;
        LiveStream ls = new LiveStream();
        String channelname = "";
        String platform = null;
        //EditText channelSearch;

        @Override
        protected LiveStream doInBackground(String... urls) {
            for (String url_or_channel : urls) {
                ls = checkStream(url_or_channel);
                if (ls != null && ls.live) {
                    break; // we found a live stream, stop checking
                }
            }
            return ls;
        }

        @Override
        protected void onPostExecute(LiveStream liveStatus) {

            //context.setTitle
            if (liveStatus != null)
                mActivity.setTitle(liveStatus.title);

            if (goToStreamButton != null) {

                if (ls != null && ls.live) {
                    goToStreamButton.setVisibility(View.VISIBLE);
                    goToStreamButton.setText(channelname + " is live. Watch Now");
                } else {
                    goToStreamButton.setVisibility(View.GONE);
                }
            }
        }

        protected LiveStream checkStream(String url_or_channel_name) {
//            String status = url_or_channel_name + " is offline or the app is broken";
//            ls = new LiveStream(url_or_channel_name + " is offline. Type another channel\'s name below to watch something else.", false);

            Log.d("LC platform,channel", url_or_channel_name+" on "+String.valueOf(platform));
            if(platform != null && Platforms.containsKey(platform)){
                return Platforms.get(platform).liveStatus(url_or_channel_name);
            }
            if (url_or_channel_name.contains("mlg")
                    || url_or_channel_name.contains("majorleaguegaming")
                    || url_or_channel_name.toLowerCase().contains("gameon")) {
                if (url_or_channel_name.toLowerCase().contains("gameon")) {
                    url_or_channel_name = MlgAPI.GAMEONGG_STREAM_NAME;
                }
                return MlgAPI.liveStatus(url_or_channel_name);
            }
            // ustream channel names are just numbers (and channel names are different from
            // user names, so keep that in mind!!)  http://api.ustream.tv/channels/19068261.json
            if (url_or_channel_name.matches("-?\\d+(\\.\\d+)?")) {
                return UstreamAPI.liveStatus(url_or_channel_name);
            }
            for (int i = 0; i < PLATFORM_LIST.length; i++) {
                ls = PLATFORM_LIST[i].liveStatus(url_or_channel_name);
                if(ls != null && ls.live){
                    return ls;
                }
            }

            ls = new LiveStream(url_or_channel_name + " is offline. Type another channel\'s name below to watch something else.", false);
            return ls;
        }
    }

    public static class DownloadTask extends AsyncTask<String, Void, HashMap> {
        MainActivity mActivity;
        public Spinner qualityPicker;
        public int spinner_item;
        public ResizingVideoView video;
        public Context context;

        String channel;
        public String platform = null;
//        JSONObject channelStatus;

        public TextView header;
        //public EditText channelSearch;

        public HashMap qualities;


        @Override
        protected HashMap doInBackground(String... channels) {
            channel = channels[0];
            Log.d("DT platform,channel", channel+" on "+String.valueOf(platform));
            if(platform != null && Platforms.containsKey(platform)){
                return Platforms.get(platform).qualities(channel);
            }
            HashMap newQualities = new HashMap<String, String>();
            //String url = "http://pubapi.cryptsy.com/api.php?method=singleorderdata&marketid=132";
            //String retval = "";
            //String url = "";
//  TODO: instead of checking every platform, use a dict and only check the relevant platorm
            if (channel.contains("mlg")
                    || channel.contains("majorleaguegaming")
                    || channel.toLowerCase().contains("gameon")) {
                if (channel.toLowerCase().contains("gameon")) {
                    channel = MlgAPI.GAMEONGG_STREAM_NAME;
                }
                platform = "mlg";
                return MlgAPI.qualities(channel);
            }

            // ustream channels are just numbers
            // (and channels are different from user ids
            if (channel.matches("-?\\d+(\\.\\d+)?")) {
                platform = "ustream";
                return UstreamAPI.qualities(channel);
            }

            for (int i = 0; i < PLATFORM_LIST.length; i++) {
                newQualities = PLATFORM_LIST[i].qualities(channel);
                if(newQualities.size() > 0){
                    platform = getKeyByValue(Platforms, PLATFORM_LIST[i]);
                    return newQualities;
                }
            }
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
                mActivity.setWatcherSocket(platform, channel);
            }
            SetCachedHash(channel + "|cache", qualities, context);

            // get the stream status
            // TODO: consider not doing this, since we just found out whether or not there are any streams
            // consider: return "x is offline" if no_qualities
//            if(qualities.size() == 0){
//                return;
//            }
            Business nb = new Business();
            LiveChecker lc = nb.new LiveChecker();
            lc.mActivity = mActivity;
            lc.platform = this.platform;
            lc.execute(channel);
        }
        //Note, url should be good and proper before hand

    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
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
                Log.e(Business.class.toString(), "Failed to download file, IO Error");
                e.printStackTrace();
            }
        }else{
            Log.w(Business.class.toString(), "Failed to download file, HTTP Error");
        }
        return "";

    }

    public static void PlayURL(ResizingVideoView video, String url) {
        video.setVisibility(View.VISIBLE);
        video.showProgress();
        //video.progressBar.setVisibility(View.VISIBLE);
        video.setVideoURI(Uri.parse(url));

        //video.setMediaController(new MediaController(context));
        video.requestFocus();
//        video.start();
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
                final List<Metadata> mm = ParseJsonToList(rv);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    frag.setLabelValueList(mm);
                    }
                });
            }
        });
    }

    static List<Metadata> ParseJsonToList(String jString) {
        List<Metadata> retval = new ArrayList<Metadata>();

        try {
            JSONObject j = new JSONObject(jString);
            retval = ParseJsonToList(j);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    }


    public static List<Metadata> ParseJsonToList(JSONObject j) {
        List<Metadata> retval = new ArrayList<Metadata>();

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
            retval.add(new Metadata(Integer.toString(totalviewers) +" Rustlers Watching", null));
            retval.add(new Metadata("Destiny", "twitch"));
//            if(streamsObj.length() > 0){
//                hm.clear();
//            }
            Iterator<Pair<String, Integer>> sortedstreams = sorted.iterator();
            List<Metadata> liveList = new ArrayList<Metadata>();
            List<Metadata> offlineList = new ArrayList<Metadata>();

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
                    if(!metaindex.has(key) || !metadata.has(metaindex.getString(key))){
                        continue;
                    }
                    JSONObject jmd = metadata.getJSONObject(metaindex.getString(key));
                    Metadata md = new Metadata(jmd);
                    if(md.live){
                        liveList.add(md);
                    }else{
                        offlineList.add(md);
                    }
                }
            }
            retval.addAll(liveList);
            retval.addAll(offlineList);
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
