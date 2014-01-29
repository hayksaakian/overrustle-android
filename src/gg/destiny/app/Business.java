package gg.destiny.app;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.UserDictionary;
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

public class Business{
	final static String GAMEONGG_QUALITIES_URL = "http://mlghds-lh.akamaihd.net/i/mlg17_1@167001/master.m3u8";
	final static String MLG_STREAMS_STATUS_URL = "http://streamapi.majorleaguegaming.com/service/streams/all";
	final static String GAMEONGG_STREAM_NAME = "mlg17";
	final static String GAMEONGG_GENERIC_STATUS = "MLG GameOn.gg SC2 Invitational";
	
	final static String DESTINY_EMOTICON_CSS_ENDPOINT = "http://cdn.destiny.gg/1.25.3/chat/css/emoticons.css";
	final static String DESTINY_EMOTES_ENDPOINT = "";
	
	final static String[] EMOTICON_LIST = {"Abathur", "AngelThump", "ASLAN", "BasedGod", "BibleThump", "CallCatz", "CallChad", "DaFeels", "DappaKappa", "DatGeoff", "DESBRO", "Disgustiny", "DJAslan", "Dravewin", "DuckerZ", "DURRSTINY", "FeedNathan", "FIDGETLOL", "FrankerZ", "GameOfThrows", "Heimerdonger", "Hhhehhehe", "INFESTINY", "Kappa", "Klappa", "LUL", "MotherFuckinGame", "NoTears", "OhKrappa", "OverRustle", "SoDoge", "SoSad", "SURPRISE", "TooSpicy", "UWOTM8", "WhoahDude", "WORTH"};
	
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


	public class EmoteDownloader extends AsyncTask<String, Void, String[]>{
		Context mContext;
		
		@Override
		protected String[] doInBackground(String... urls) {
			// get emotes from proper endpoint
			String[] emotes = {};
			if(!DESTINY_EMOTES_ENDPOINT.equals("")){
				String rawJsonEmotes = HttpGet(DESTINY_EMOTES_ENDPOINT);
				JSONArray jsonEmotes = null;
				try {
					jsonEmotes = new JSONArray(rawJsonEmotes);
					if(jsonEmotes != null && jsonEmotes.length() > 0){
						emotes = new String[jsonEmotes.length()];
						for (int i = 0; i < emotes.length; i++) {
							String emote = jsonEmotes.getString(i);
							emotes[i] = emote;
						}
					}
				} catch (JSONException e) { e.printStackTrace(); }
			}else{
				return EMOTICON_LIST;
			}
			return emotes;
		}
	
		@Override
		protected void onPostExecute(String[] foundEmotes){
			if(foundEmotes != null && foundEmotes.length > 0){
				AddEmotesToUserDict(mContext, foundEmotes);
				Log.d("EmoteDownload", "Done adding emotes total#:"+String.valueOf(foundEmotes.length));
			}else{
				Log.e("EmoteDownload", "Problem finding or GETting emotes list.");
			}			
		}
	}
	
	public class LiveChecker extends AsyncTask<String, Void, String>
	{
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
				if(isLive){
					break; // we found a live stream, stop checking 
				}
			}
			
			return mStatus;
		}
	
		@Override
		protected void onPostExecute(String liveStatus){
			
			//context.setTitle
			if(liveStatus != null)
				mActivity.setTitle(liveStatus);
				
			if(goToStreamButton != null){
				
				if(isLive){
					goToStreamButton.setVisibility(View.VISIBLE);
					goToStreamButton.setText(channelname+" is live. Watch Now");
				}else{
					goToStreamButton.setVisibility(View.GONE);
				}
			}
		}
		
		protected String checkStream(String url_or_channel_name){
			String status = "";
			try {
				if(url_or_channel_name.equals("gameongg")){
					url_or_channel_name = MLG_STREAMS_STATUS_URL;
				}
				JSONObject jsno = new JSONObject();
				if(url_or_channel_name.contains("mlg") || url_or_channel_name.contains("majorleaguegaming")){
					String mlgStatuses = HttpGet(url_or_channel_name);
					channelname = "GameOn.gg";
					JSONArray jsna = new JSONObject(mlgStatuses)
						.getJSONObject("data")
						.getJSONArray("items");
					int l = jsna.length();
					for (int i = 0; i < l; i++) {
						JSONObject item = jsna.getJSONObject(i);
						if(item.getString("stream_name").equals(GAMEONGG_STREAM_NAME)){
							
							int iStatus = item.getInt("status");
							isLive = iStatus == 1;
							if(isLive){
								status = GAMEONGG_GENERIC_STATUS;
							}else if(iStatus == 2){
								// from replay
								status = GAMEONGG_GENERIC_STATUS + " [rebroadcast]";
							}else
							{
								status = "GameOn.gg might be offline. Tell hephaestus if this is wrong.";
							}
							break;
						}
					}
					
				}else{
					jsno = getTwitchStatus(url_or_channel_name);
					channelname = url_or_channel_name;
					if(jsno != null && jsno.length() > 0){
						status = jsno.getString("status");
						isLive = true;
					}else{
						status = channelname + " is offline. Type another channel\'s name below to watch something else.";
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			}
			
			return status;			
		}
	}

	public static class DownloadTask extends AsyncTask<String, Void, HashMap>
	{
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
		protected HashMap doInBackground(String... channels)
		{
			HashMap newQualities = new HashMap<String, String>();
			//String url = "http://pubapi.cryptsy.com/api.php?method=singleorderdata&marketid=132";
			//String retval = "";
			channel = channels[0];
			//String url = "";
			if(channel.equals("gameongg")){
				newQualities = parseQualitiesFromURL(GAMEONGG_QUALITIES_URL);
				if(newQualities.size() > 0){
					channelStatus = new JSONObject();
					try {
						channelStatus.put("status", GAMEONGG_GENERIC_STATUS);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				String auth = getTwitchAuth(channel);
				newQualities = getTwitchQualities(channel, auth);
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

			if(qualities.containsKey("low")){
				qualityURL = (String) qualities.get("low");
			}else if(qualities.containsKey("medium")){
				qualityURL = (String) qualities.get("medium");
			}else if(qualities.containsKey("high")){
				qualityURL = (String) qualities.get("high");
			}else{
				if(qualities.size() > 0){
					String fq = String.valueOf(qualities.keySet().toArray()[0]);
					qualityURL = (String)qualities.get(fq);
				}
			}
			
			if(!qualityURL.equals("")){
				PlayURL(video, qualityURL);
			}

			
			if (qualities.size() > 0)
			{
				LoadQualities(qualityPicker, qualities, context, spinner_item);
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

	public static void PlayURL(ResizingVideoView video, String url)
	{
		video.setVisibility(View.VISIBLE);
		video.showProgress();
		//video.progressBar.setVisibility(View.VISIBLE);
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
		Log.d("business", "caching qualities="+String.valueOf(value.size()));
		SharedPreferences prefs = cn.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor edit = prefs.edit();
		JSONObject jsn = new JSONObject(value);

		edit.putString(key, String.valueOf(jsn));
		return edit.commit();
	}
	
	static public boolean SetCachedArray(String key, String[] array, Context cn){
		Log.d("business", "caching array key="+key+" of length "+String.valueOf(array.length));
		SharedPreferences prefs = cn.getSharedPreferences("prefs", 0);
		SharedPreferences.Editor edit = prefs.edit();
		 
		Set<String> set = new HashSet<String>(Arrays.asList(array));
		//edit.putString(key, String.valueOf(jsn));
		edit.putStringSet(key, set);
		return edit.commit();
		
	}
	
	static public HashMap GetCachedHash(String key, Context cn){
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
	
	static public String[] GetCachedArray(String key, Context cn){
		String[] retval = {};

		SharedPreferences settings;
		settings = cn.getSharedPreferences("prefs", 0);
        //get the sharepref
		Set<String> rawarr = settings.getStringSet(key, null);
		
		rawarr.toArray(retval);
		
		return retval;
	}
	
	public static boolean isKitkat(){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	// UserDictionary.Words.addWord(Context context, String word, int frequency, String shortcut, Locale locale)
	// UserDictionary.Words.addWord( this , "newMedicalWord", 1, UserDictionary.Words.LOCALE_TYPE_CURRENT);
	// TODO support api level 15 (4.0)
	public void AddEmotesToUserDict(Context context, String[] emotes){
		Locale locale = context.getResources().getConfiguration().locale;
		
		// check current dict to avoid redundant adds
		List<String> currentWords = new ArrayList<String>();
		
		int frequency = 0;
		String shortcut = null;
		for (int i = 0; i < emotes.length; i++) {
			String word = emotes[i];
			if(currentWords.indexOf(word) != -1){
			}else{
				UserDictionary.Words.addWord(context, word, frequency, shortcut, locale);
			}
		}		
	}
}
