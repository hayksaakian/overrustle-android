package gg.destiny.app;

import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.json.*;

public class Business
{
	final static String GAMEONGG_QUALITIES_URL = "http://mlghds-lh.akamaihd.net/i/mlg17_1@167001/master.m3u8"; 

	Business(){
	}
	/**
	 steps
	 1 GET token and sig for $channel

	 String turl = "http://api.twitch.tv/api/channels/"+channel+"/access_token";
	 ex; 
	 channel=dansgaming
	 =>
	 {"token":"{\"user_id\":null,\"channel\":\"dansgaming\",\"expires\":1389597116,\"chansub\":{\"view_until\":1924905600,\"restricted_bitrates\":[]},\"private\":{\"allowed_to_view\":true},\"privileged\":false}","sig":"77b105f2fb91892cb8908508a7e54c75c7a8d468","mobile_restricted":false}

	 (note; dont url encode anything)

	 2 GET the quality options as a m3u8 list

	 http://usher.justin.tv/api/channel/hls/CHANNEL.m3u8?token=TOKEN&sig=SIG

	 ex, continued

	 3 choose a quality (url) from the list which is basically just a text file
	 comments in the file start with a #
	 otherwise the other lines are urls pointing to usable urls
	 ex, 

	 4 provide a chosen quality setting to the VideoView



	 **/

	public static class DownloadTask extends AsyncTask<String, Void, HashMap>
	{
		public Spinner qualityPicker;
		public int spinner_item;
		public VideoView video;
		public Context context;

		String channel;
		JSONObject channelStatus;

		public TextView header;
		public EditText channelSearch;

		public HashMap qualities;

		@Override
		protected HashMap doInBackground(String... channels)
		{
			HashMap newQualities = new HashMap<String, String>();
			//String url = "http://pubapi.cryptsy.com/api.php?method=singleorderdata&marketid=132";
			//String retval = "";
			channel = channels[0];
			//String url = "";
			if(channel.equals("gameongg")){
				String qualitiesURL = GAMEONGG_QUALITIES_URL;
				newQualities = parseQualitiesFromURL(qualitiesURL);
				if(newQualities.size() > 0){
					channelStatus = new JSONObject();
					try {
						channelStatus.put("status", "MLG GameOn.gg SC2 Invitational");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				String auth = getTwitchAuth(channel);
				newQualities = getTwitchQualities(channel, auth);
				try {
					channelStatus = getTwitchStatus(channel);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			//String readURL = HttpGet(url);
			//String readURL = "[1, 2]";
			return newQualities;
		}

		//needs a list of quality options
		@Override
		protected void onPostExecute(HashMap foundQualities)
		{
			qualities = foundQualities;
			Log.d("found qualities #", String.valueOf(qualities.size()));
			String qualityURL = "";

			if(qualities.containsKey("Low")){
				qualityURL = (String) qualities.get("Low");
			}else if(qualities.containsKey("Medium")){
				qualityURL = (String) qualities.get("Medium");
			}else if(qualities.containsKey("High")){
				qualityURL = (String) qualities.get("High");
			}else{
				if(qualities.size() > 0){
					String fq = String.valueOf(qualities.keySet().toArray()[0]);
					qualityURL = (String)qualities.get(fq);
				}
			}
			
			if(!qualityURL.equals("")){
				PlayURL(video, qualityURL);
			}

			// set status
			String headerText = channel + " is offline. You can watch another stream if you type its channel name exactly below.";
			if (channelStatus != null && channelStatus.length() > 0)
			{
				try
				{
					headerText = channelStatus.getString("status");
				}
				catch (JSONException e)
				{}
			}else{
				//reveal channel search
				channelSearch.setVisibility(View.VISIBLE);
			}
			if (header != null)
			{
				header.setText(headerText);
			}
			if (qualities.size() > 0)
			{
				LoadQualities(qualityPicker, qualities, context, spinner_item);
			}
			SetCachedHash(channel + "|cache", qualities, context);
		}
		//Note, url should be good and proper before hand
		
	}

	public static String HttpGet(String url)
	{ 
		Log.d("GET ing", url);
		//return "[200]";}
		//public String baddoGet(String url) { 

		StringBuilder builder = new StringBuilder(); 
		HttpClient client = new DefaultHttpClient(); 
		HttpGet httpGet = new HttpGet(url); 
		try
		{ 
			HttpResponse response = client.execute(httpGet); 
			StatusLine statusLine = response.getStatusLine(); 
			int statusCode = statusLine.getStatusCode(); 
			if (statusCode == 200)
			{ 
				HttpEntity entity = response.getEntity(); 
				InputStream content = entity.getContent(); 
				BufferedReader reader = new BufferedReader(new InputStreamReader(content)); 
				String line; 
				while ((line = reader.readLine()) != null)
				{ 
					builder.append(line); 
				} 
			}
			else
			{ 
				Log.e(Business.class.toString(), "Failed to download file"); 
			} 
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{ 
			e.printStackTrace(); 
		} 

		return builder.toString(); 
	}

	public static HashMap getTwitchQualities(String channel, String auth) {
		HashMap mQualities = new HashMap<String, String>();
		try
		{
			JSONObject authObj = new JSONObject(auth);
			String token = authObj.getString("token");
			String sig = authObj.getString("sig");

			token = Uri.encode(token);

			String qualitiesURL = "http://usher.justin.tv/api/channel/hls/" + channel + ".m3u8?token=" + token + "&sig=" + sig;
			
			mQualities = parseQualitiesFromURL(qualitiesURL);
			
		}
		catch (JSONException e)
		{}
		return mQualities;
	}
	
	public static JSONObject getTwitchStatus(String channel) throws JSONException{
		JSONObject channelStatus = new JSONObject();
		//maybe put this in another task...
		String strStatus = "";
		try
		{
			strStatus = HttpGet("https://api.twitch.tv/kraken/streams/" + channel);
		}
		catch (Exception e)
		{

		}
		JSONObject jsnStatus = new JSONObject(strStatus);
		if (!jsnStatus.isNull("stream"))
		{
			//statusMessage = "";
			channelStatus = jsnStatus.getJSONObject("stream").getJSONObject("channel");
		}
		
		return channelStatus;
	}

	public static void PlayURL(VideoView video, String url)
	{
		video.setVisibility(View.VISIBLE);
		video.setVideoURI(Uri.parse(url));

		//video.setMediaController(new MediaController(context));
		video.requestFocus();
		video.start();
	}

	public static String getTwitchAuth(String channel) {
		String auth = "";
		String authurl = "http://api.twitch.tv/api/channels/" + channel + "/access_token";
		auth = HttpGet(authurl);
		return auth;
	}
	
	public static HashMap parseQualitiesFromURL(String url){
		HashMap mQualities = new HashMap<String, String>();
		String qualityOptions = HttpGet(url);
		String line=null; 
		try
		{
			String lastquality = null;
			qualityOptions = qualityOptions.replace("#", "\n#").replace("http", "\nhttp");

			String [] opA = qualityOptions.split("\n");
			for (int i=0; i < opA.length; i++)
			{
				line = opA[i];
				if (line.length() > 0)
				{
					if (line.startsWith("#EXT-X-STREAM"))
					{
						// descriptor, therefore parse quality
						// example
						// #EXT-X-MEDIA:TYPE=VIDEO,GROUP-ID="high",NAME="High",AUTOSELECT=YES,DEFAULT=YES
						
						lastquality = line.split(",")[2];
						//get text between quotes
						lastquality = lastquality.split("=")[1];
						if(lastquality.contains("\"")){
							lastquality = lastquality.split("\"")[1];
						}
						Log.d("found quality", lastquality);
					}
					else if (line.startsWith("http") && lastquality != null)
					{
						Log.d("quality url", line);
						mQualities.put(lastquality, line);
						lastquality = null;
					}
				}
				Log.d("raw qualities", line);

			}
		}
		catch (Exception e)
		{}
		return mQualities;
	}

	public static void LoadQualities(Spinner qualityPicker, HashMap qualities, Context context, int spinnerItemId)
	{
		List<String> list = new ArrayList<String>();

		list.addAll(qualities.keySet());
		list.add("Audio Only");
		list.add("Chat Only");
		
		//sort list
		java.util.Collections.sort(list, Collator.getInstance());
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, spinnerItemId, list); 
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		qualityPicker.setAdapter(dataAdapter);
	}


	static public boolean SetCachedHash(String key, HashMap value, Context cn)
	{
		SharedPreferences prefs = cn.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor edit = prefs.edit();
		JSONObject jsn = new JSONObject(value);

		edit.putString(key, String.valueOf(jsn));
		return edit.commit();
	}
	static public HashMap GetCachedHash(String key, Context cn)
	{
		HashMap hm = new HashMap<String, String>();

		SharedPreferences settings;
		settings = cn.getSharedPreferences("prefs", 0);
        //get the sharepref
		String rawhm = settings.getString(String.valueOf(key), "");
		if (rawhm != null && !rawhm.equals(""))
		{
			try
			{
				JSONObject jsnhm = new JSONObject(rawhm);
				//Log.d(LOG, rawhm);
				if (jsnhm.length() > 0)
				{

					JSONArray nms = jsnhm.names();
					int klength = nms.length();
					for (int i = 0; i < klength; i++)
					{
						String nm = nms.getString(i);
						String vl = jsnhm.getString(nm);
						hm.put(nm, vl);
					}
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		return hm;
	}
	
	public static boolean isKitkat(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

}