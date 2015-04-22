package gg.destiny.app;

import android.app.Application;
import android.util.Log;
//import com.mogoweb.chrome.ChromeInitializer;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class DestinyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        //ChromeInitializer.initialize(this);
        String parse_client_key = "";
        String parse_app_id = "";
        try {
            InputStream inputStream = getAssets().open("api_keys.json");
            JSONObject api_keys = new JSONObject(readInputStream(inputStream).trim());
            //do whatever with your key
            parse_client_key = api_keys.getString("parse_client_key");
            parse_app_id = api_keys.getString("parse_app_id");
            Log.d("GOT API KEY LENGTH", String.valueOf(parse_client_key.length()));
            Log.d("GOT APP ID LENGTH", String.valueOf(parse_app_id.length()));
        }  catch (IOException e) {
            Log.d("DEVELOPER ERROR", "No keyfile found, remember to add one");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ParseCrashReporting.enable(this);
        Parse.initialize(this, parse_app_id, parse_client_key);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
    public static String readInputStream(InputStream in) throws IOException {
        StringBuffer stream = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;)
            stream.append(new String(b, 0, n));

        return stream.toString();
    }
}
