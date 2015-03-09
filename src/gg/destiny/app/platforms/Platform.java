package gg.destiny.app.platforms;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import gg.destiny.app.Business;

/**
 * Created by Hayk on 2/26/2015.
 */
public class Platform {

    public String HttpGet(String url){
        return Business.HttpGet(url);
    }

    public LiveStream liveStatus(String channel){
        return null;
    }
    public JSONObject status(String channel) throws JSONException {
        return null;
    };
    public HashMap<String, String> qualities(String channel){
        return null;
    };

    public static HashMap parseQualitiesFromURL(String url) {
        return parseQualitiesFromURL(url, "http");
    }

//    parse a a file from a m3u8 endpoint into a list of qualities
    public static HashMap<String, String> parseQualitiesFromURL(String url, String urlPrefix) {
        HashMap mQualities = new HashMap<String, String>();
        String qualityOptions = Business.HttpGet(url);
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
}
