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
	final static String CONTENTPROVIDER_APP_ID = "^gg.destiny.app";
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


	public static class EmoteDownloader extends AsyncTask<String, Void, String[]>{
		Context mContext;
		String note = null;
		
		@Override
		protected String[] doInBackground(String... notes) {
			String[] emotes = EMOTICON_LIST;
			//if(false){
			if(notes.length > 0){
				note = notes[0];
				
				return emotes;
			}
			// TODO get emotes from proper endpoint
			//String[] emotes = {};
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
				if(note !=null){
					if(note.equals("delete")){
						DeleteAllDict(mContext);
					}else if(note.equals("deletebyappid")){
						DeleteAppDict(mContext);
					}
				}else{
					AddEmotesToUserDict(mContext, foundEmotes);
					String s = "Done adding emotes total#:"+String.valueOf(foundEmotes.length);
					Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
					Log.d("EmoteDownload", s);
				}
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
				
			} catch (JSONException e) {	e.printStackTrace();}
			
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
			
			if (qualities.size() > 0)
			{
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

			String qualitiesURL = "http://usher.justin.tv/api/channel/hls/" + channel + ".m3u8?token=" + token + "&sig=" + sig "&allow_source=true";
			
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
		
		//sort list
		java.util.Collections.sort(list, Collator.getInstance());

		list.add("Audio Only");
		list.add("Chat Only");
		
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
		if (rawhm != null && !rawhm.equals("")){
			try{
				JSONObject jsnhm = new JSONObject(rawhm);
				//Log.d(LOG, rawhm);
				if (jsnhm.length() > 0){
					JSONArray nms = jsnhm.names();
					int klength = nms.length();
					for (int i = 0; i < klength; i++){
						String nm = nms.getString(i);
						String vl = jsnhm.getString(nm);
						hm.put(nm, vl);
					}
				}
			}catch (JSONException e)
			{e.printStackTrace();}
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

	
	// NOTE HACK using the shortcut field
	// to identify words added by this app
	// UserDictionary.Words.addWord(Context context, String word, int frequency, String shortcut, Locale locale)
	// UserDictionary.Words.addWord( this , "newMedicalWord", 1, UserDictionary.Words.LOCALE_TYPE_CURRENT);
	// TODO support api level 15 (4.0)
	// TODO maybe move this to a class?
	
	public static void oldAddEmotesToUserDict(Context context, String[] emotes){
		Locale locale = context.getResources().getConfiguration().locale;
		for (int i = 0; i < emotes.length; i++) {
			String word = emotes[i];
			//if(currentWords.indexOf(word) != -1){
			//}else{
			UserDictionary.Words.addWord(context, word, 255, null, locale);
			//}
		}
		
	}
	
	public static void AddEmotesToUserDict(Context context, String[] emotes){
		Locale locale = context.getResources().getConfiguration().locale;
		String APP_ID = CONTENTPROVIDER_APP_ID;
		int iAPP_ID = android.os.Process.myUid();
		//APP_ID = String.valueOf(iAPP_ID);
		//APP_ID = FIXED_APP_ID;
		Log.d("app id", APP_ID);
		ContentResolver userDictResolver = context.getContentResolver();
		
//		2) Delete all old emotes
		DeleteAppDict(context);
		
		
		// Defines a new Uri object that receives the result of the insertion
		Uri mNewUri = null;
		
		int frequency = 255;
		String shortcut = null;
		
		for (int i = 0; i < emotes.length; i++) {
			String word = emotes[i];
			//if(currentWords.indexOf(word) != -1){
			//}else{
			//	UserDictionary.Words.addWord(context, word, frequency, shortcut, locale);
			//}

			// Defines an object to contain the new values to insert
			ContentValues mNewValues = new ContentValues();

			/*
			 * Sets the values of each column and inserts the word. The arguments to the "put"
			 * method are "column name" and "value"
			 */
			mNewValues.put(UserDictionary.Words.APP_ID, iAPP_ID);
			mNewValues.put(UserDictionary.Words.LOCALE, locale.toString());
			mNewValues.put(UserDictionary.Words.WORD, word);
			mNewValues.put(UserDictionary.Words.SHORTCUT, APP_ID);
			mNewValues.put(UserDictionary.Words.FREQUENCY, frequency);
			//mNewValues.put(UserDictionary.Words.FREQUENCY, String.valueOf(frequency));

			mNewUri = userDictResolver.insert(
				UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
				mNewValues                          // the values to insert
			);
		}		
	}
	
	public static void DeleteAppDict(Context context){
		int iAPP_ID = android.os.Process.myUid();
		String appid = String.valueOf(iAPP_ID);
		String APP_ID = CONTENTPROVIDER_APP_ID; 
		//appid = FIXED_APP_ID;
		//DeleteAllDict(context, appid);
		//DeleteAllDict(context, FIXED_APP_ID);
		
		ContentResolver userDictResolver = context.getContentResolver();
		//		2) Delete all old emotes
//		// Defines selection criteria for the rows you want to delete

		String mSelectionClause = UserDictionary.Words.SHORTCUT + " LIKE ?";
		String[] genericAppId = {APP_ID}; // this will delete everything

		// Defines a variable to contain the number of rows deleted
		int mRowsDeleted = 0;

		// Deletes the words that match the selection criteria
		mRowsDeleted = userDictResolver.delete(
			UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
			mSelectionClause,                   // the column to select on
			genericAppId                      // the value to compare to
		);
		String s = "Deleted old emotes. Total="+String.valueOf(mRowsDeleted);
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		Log.d("Deleted emotes", s);
		
		
	}
	
	public static void DeleteAllDict(Context context){
		DeleteAllDict(context, "0");
	}
	public static void DeleteAllDict(Context context, String appid){
		ContentResolver userDictResolver = context.getContentResolver();
		//		2) Delete all old emotes
//		// Defines selection criteria for the rows you want to delete
		
		String mSelectionClause = UserDictionary.Words.APP_ID + " LIKE ?";
		String[] genericAppId = {appid}; // this will delete everything

		// Defines a variable to contain the number of rows deleted
		int mRowsDeleted = 0;

		// Deletes the words that match the selection criteria
		mRowsDeleted = userDictResolver.delete(
			UserDictionary.Words.CONTENT_URI,   // the user dictionary content URI
			mSelectionClause,                   // the column to select on
			genericAppId                      // the value to compare to
		);
		String s = "Deleted old emotes. Total="+String.valueOf(mRowsDeleted);
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
		Log.d("Deleted emotes", s);
		
	}

	public static boolean hasAutocomplete(Context context) {
		int iAPP_ID = android.os.Process.myUid();
		String APP_ID = String.valueOf(iAPP_ID);
		APP_ID = CONTENTPROVIDER_APP_ID;
		
		ContentResolver userDictResolver = context.getContentResolver();

		// A "projection" defines the columns that will be returned for each row
		   // Contract class constant for the _ID column name
		String[] mProjection = {UserDictionary.Words._ID };
// Initializes an array to contain selection arguments
		String mmSelectionClause = UserDictionary.Words.SHORTCUT + " LIKE ?";
		String[] mmSelectionArgs = {APP_ID};
		
		// Does a query against the table and returns a Cursor object
		Cursor mCursor = null;
		String mSortOrder = UserDictionary.Words.DEFAULT_SORT_ORDER;
		mCursor = userDictResolver.query(
			UserDictionary.Words.CONTENT_URI,  // The content URI of the words table
			mProjection,                       // The columns to return for each row
			mmSelectionClause,                   // Either null, or the word the user entered
			mmSelectionArgs,                    // Either empty, or the string the user entered
			mSortOrder);                       // The sort order for the returned rows

//		http://stackoverflow.com/questions/468211/how-do-i-get-the-count-in-my-content-provider
//		may be more efficient
		return (mCursor != null && mCursor.getCount() > 0);
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
