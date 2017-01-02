package gg.destiny.app;

//import android.R;
import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.*;
import android.support.v4.widget.DrawerLayout;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView.*;

import java.net.URISyntaxException;
import java.util.*;


import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;
import com.parse.ParsePush;

import org.json.JSONObject;

import gg.destiny.app.platforms.Metadata;
import gg.destiny.app.support.NavigationDrawerFragment;

/*
* CONSIDER: moving options to a popup instead of having a bunch of buttons on the top
*
* */
public class MainActivity extends FragmentActivity
        implements OnItemSelectedListener, NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static String TAG = "MainOverRustle";

    Activity thisActivity;

    // pulls the stream list
    Socket overrustle_browser_socket = null;
    // receives rustler counts for the currently opened stream
    Socket overrustle_watcher_socket = null;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ActionBarDrawerToggle mDrawerToggle;
    //Map<String, String> overRustlers = new HashMap<String, String>();

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if(mNavigationDrawerFragment == null || isOnCreateDone == false)
            return;
        if(position != 0){
            Metadata selectedValue = mNavigationDrawerFragment.getValue(position);
            Log.d("DrawerValue", selectedValue.channel);
            if (position > 0 && selectedValue.channel != null && selectedValue.channel.length() > 0) {
                loadChannel(selectedValue.channel, selectedValue.platform);
                Toast.makeText(getApplicationContext(), selectedValue.channel, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static final int NUM_PAGES = 3;
    private ViewPager chatPager;
    private ScreenSlidePagerAdapter chatPagerAdapter;

    @Override
    public void onBackPressed() {
//        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
    }

    final static String DEFAULT_CHANNEL = "destiny";
    final static String DEFAULT_PLATFORM = "twitch";
	
	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String qualityName = ((Spinner) parent).getSelectedItem().toString();
        qualityOptions = Business.GetCachedHash(channel + "|cache", this);
        Log.d("quality setting", qualityName+" of "+String.valueOf(qualityOptions.size()));
        if(qualityName.startsWith("Chat")){
            video.stopPlayback();
            video.setVisibility(View.GONE);
        } else if (qualityOptions != null){
            boolean audioOnly = false;
            if(qualityName.startsWith("Audio")){
                audioOnly = true;
                // use whatever video format we had before
                if(lastQuality.length() > 0){
                	qualityName = lastQuality;
                }else{
                	// or Low if there was no previous quality
                	qualityName = "low";  
                }
            }
            if(qualityOptions.containsKey(qualityName)){
            	// only reload if this is a different quality
                // this is an annoying premature optimization,
                // the source of many bugs. consider killing it.
            	if(video.isPlaying() == false || (!lastQuality.equals(qualityName) || channel != lastChannel)){
					Log.d("VideoView", "Loading quality="+qualityName);
                    String url = (String)qualityOptions.get(qualityName);
                    Log.d("VideoView", "Loading URL="+url);
                    Business.PlayURL(video, url);
            	}else{
            		Log.d("VideoView", "avoiding loading the stream because that quality is already loaded");

            	}
            }
            if(audioOnly){
            	//params.width = 10; // (int) (300*metrics.density);
                //params.height = 10;// (int) (250*metrics.density);
                //params.leftMargin = 30;
                //video.setLayoutParams(params);
            	ResizeViewTo(youtubeLayout, "tiny"); 
            	disableFullscreen();
            }else{
            	ResizeViewTo(youtubeLayout, "original");
                lastQuality = qualityName;
            }
        }
    }
	 
    @Override
    public void onNothingSelected(AdapterView<?> parent){
        // Another interface callback
    }

    View rootView;
    
	// replaced with actionbar
    //private RelativeLayout header_container;
//    private RelativeLayout navigation;

//    NOTE: WebView is created at runtime
//YoutubeLayout youtubeLayout;
   // LinearLayout youtubeLayout;
	RelativeLayout youtubeLayout;
    
    ResizingVideoView video;
	
	MenuItem actionSearchItem;
	
    //private EditText et; // replaced with searchview in actionbar
    // TODO: replace with side navigation
//    Button loadFeaturedStreamButton;
    //TextView header; // replaced with action bar title
//    private TextView pageLoadTime;

    private Spinner mQualityPicker;
    public HashMap qualityOptions;
    String lastQuality = "";
    String lastChannel = "";
    String channel;

//    SystemUIHider uiHider;
    
    // remember the original attributes of the video view
    private int ogwidth;
    private int ogheight;
    
    static private float MINIMODE_RATIO = 3f;
    
    private int screenLongSide = 960;
    private int screenShortSide = 540;

	private boolean isOnCreateDone = false;

	boolean gameonlive = false;

	@Override
	public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        thisActivity = this;

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mDrawerToggle =  mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        // Instantiate a ViewPager and a PagerAdapter.
        chatPager = (ViewPager) findViewById(R.id.pager);
//        chatPager.setOnPageChangeListener(
//                new ViewPager.SimpleOnPageChangeListener() {
//                    @Override
//                    public void onPageSelected(int position) {
//                        // When swiping between pages, select the
//                        // corresponding tab.
////                        getActionBar().setSelectedNavigationItem(position);
//                        Toast.makeText(getApplicationContext(), "changed to tab: " + Integer.toString(position), Toast.LENGTH_SHORT).show();
//                    }
//                });
        chatPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        chatPagerAdapter.parentPager = chatPager;
        chatPager.setAdapter(chatPagerAdapter);


        Log.i("POKE", "PROD");

//                wv = (WebView) findViewById(R.id.wv);
//        youtubeLayout = (YoutubeLayout) findViewById(R.id.youtubeLayout);
        youtubeLayout = (RelativeLayout) findViewById(R.id.youtubeLayout);
        video = (ResizingVideoView)findViewById(R.id.video);
		video.progressBar = (ProgressBar)findViewById(R.id.video_loading);
        ViewGroup.LayoutParams ogparams = video.getLayoutParams();
        ogheight = ogparams.height;
        ogwidth = ogparams.width;
        //header_container = (RelativeLayout)findViewById(R.id.header_container);
        //header = (TextView)findViewById(R.id.header);
//        navigation = (RelativeLayout)findViewById(R.id.navigation);
//        TODO: move to side navigation
//        loadFeaturedStreamButton = (Button)findViewById(R.id.load_gameongg_stream);

        //newActivityBtn = (ImageButton) findViewById(R.id.new_activity);
        //qualityPicker = (Spinner)findViewById(R.id.quality_picker);
//        pageLoadTime = (TextView) findViewById(R.id.page_load_time);
        //et = (EditText) findViewById(R.id.et);

        
        RelativeLayout everythingelse = (RelativeLayout)findViewById(R.id.everything_else);
        everythingelse.bringToFront();

//      setup events
//        OnFocusChangeListener toggle = new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus){
//                if (hasFocus){
//                    //header.setMaxLines(Integer.MAX_VALUE);
//                    //header.setEllipsize(null);
//                    Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();
//                }else{
//                    //header.setMaxLines(1);
//                    //header.setEllipsize(TextUtils.TruncateAt.END);
//                    Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
//                }
//            }
//        };
        
        //header.setOnFocusChangeListener(toggle);
		// search on submit
//        et.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                	channel = et.getText().toString();
//                    loadChannel(channel);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//        loadFeaturedStreamButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {loadChannel("gameongg");}
//		});

        //qualityPicker.setOnItemSelectedListener(this);
        
        OnTouchListener vlistener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//            	Log.d("Touched Video", event.toString());
            	// True if the listener has consumed the event, false otherwise.
            	// return false if we don't care about the touch any more
            	// return true if we might care about it later
            	int action = MotionEventCompat.getActionMasked(event);
            	String DEBUG_TAG = "Video Touch Event";
                
                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        Log.d(DEBUG_TAG,"Action was DOWN");
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        Log.d(DEBUG_TAG,"Action was MOVE");
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d(DEBUG_TAG,"Action was UP");
                    	toggleMinimode();
                        return false;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d(DEBUG_TAG,"Action was CANCEL");
                        return false;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                                "of current screen element");
                        return false;      
                    default : 
                        return true;
                }
            }
        };
        video.setOnTouchListener(vlistener);
//        video.setOnErrorListener(new MediaPlayer.OnErrorListener(){
//
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                final String human = String.format("what: %s, extra: %s", what, extra);
//                Log.e("MediaPlayer Error", human);
//                Toast.makeText(getApplicationContext(), "Android MediaPlayer Error: "+human, Toast.LENGTH_SHORT).show();
//                // return false because we haven't really handled the error, let it bubble up the stack.
//                return false;
//            }
//        });
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer p1){
                    video.start();
					video.progressBar.setVisibility(View.GONE);
				}
			});
		
        
//        setWindowCallbacks();
        
        rootView = getWindow().getDecorView();
        rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
            	if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            		// just went not fullscreen
                    showUI();
                    if(!Business.isKitkat()){
                    	setMinimode(true);
                    }
                } else {
                	// just went fullscreen
                    hideUI();
                }
//            	Log.d("UI changed (fullscreen?)", String.valueOf(visibility));
            }
        });
//        if(!Business.isKitkat()){
//        	alert(this);
//        }

        //Business.GetRustlers(this, mNavigationDrawerFragment);

//        TODO: fix the multitasking bug
//        sometimes, re-opening the app will crash because of the websocket

        try {
            if(overrustle_browser_socket == null) {

                Log.d("Socket.IO", "Creating overrustle_browser_socket socket");
                overrustle_browser_socket = IO.socket("https://api.overrustle.com/streams");
                // Receiving an object
                overrustle_browser_socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Transport transport = (Transport)args[0];
                        transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> headers = (Map<String, String>) args[0];
                                headers.put("Referer", "https://overrustle.com/strims?android=true");
                            }
                        });
                    }
                });


                overrustle_browser_socket.on("strims", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //Log.d("Socket.IO", "Recieved data from socket");
                        JSONObject obj = (JSONObject)args[0];
                        final List<Metadata> mm = Business.ParseJsonToList(obj);
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNavigationDrawerFragment.setLabelValueList(mm);
                            }
                        });
                    }
                });
            }
            if(!overrustle_browser_socket.connected()) {
                Log.d("Socket.IO", "Connecting overrustle_browser_socket socket");
                overrustle_browser_socket.connect();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }

        isOnCreateDone  = true;
        
        configProportions();

        //channel = et.getText().toString();
        //loadChannel(DEFAULT_CHANNEL);
        
        // destiny + check on gameon.gg
        //checkStatus(DEFAULT_CHANNEL, Business.MLG_STREAMS_STATUS_URL);
        //checkStatus();       
        //setTitle("t");
        // call this later to account for invokations of this activity via push notifications
		//handleIntent(getIntent());
    }


    @Override
    protected void onDestroy() {
        if(overrustle_browser_socket != null) {
            overrustle_browser_socket.disconnect();
        }
        if(overrustle_watcher_socket != null) {
            overrustle_watcher_socket.disconnect();
        }

        super.onDestroy();
    }

    // TODO: handle app urls from overrustle.com

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
			loadChannel(query);
			if(actionSearchItem != null)
				actionSearchItem.collapseActionView();
			//planetSearchView.co
            //use the query to search your data somehow
			return true;
        }else{
            Log.d("Unhandled Intent", intent.getAction());
        }
        return false;
    }
	
	

	@Override
	protected void onResume()
	{
		if(!video.isPlaying()){
			// play video
			video.start();
			Log.d("video", "autoresuming");
		}
		
		super.onResume();
	} // TODO </ on create >
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);
		

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
			(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		actionSearchItem = menu.findItem(R.id.action_search);
		SearchView mSearchView =
            (SearchView) actionSearchItem.getActionView();
		mSearchView.setSearchableInfo(
            searchManager.getSearchableInfo(getComponentName()));

		// set up quality picker
		mQualityPicker = (Spinner) menu.findItem(R.id.action_quality_picker).getActionView(); // find the spinner
		//ArrayAdapter<String> mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mPlanetTitles);
		//  create the adapter from a StringArray
		//s.setAdapter(mSpinnerAdapter); // set the adapter
		mQualityPicker.setOnItemSelectedListener(this); // (optional) 
		
		// set up the autocomplete checkbox
		boolean hasAutocomplete = EmoteDownloader.hasAutocomplete(this);
		menu.findItem(R.id.action_settings).setChecked(hasAutocomplete);

        // disabled until we
//        boolean getsNoficiations = PushConfig.getHandset(this);
//        menu.findItem(R.id.action_notifications).setChecked(getsNoficiations);
//
//        boolean getsSubtleNoficiations = PushConfig.getWear(this);
//        menu.findItem(R.id.action_subtle_notifications).setChecked(getsSubtleNoficiations);

		// TODO find a better place for this call
		if(!handleIntent(getIntent())){
			loadChannel(DEFAULT_CHANNEL, DEFAULT_PLATFORM);
			// and this one
//			checkStatus("gameongg");
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				// why this is necessary i don't understand
//				http://developer.android.com/guide/topics/ui/menus.html#checkable
	            if (item.isChecked()) item.setChecked(false);
	            else item.setChecked(true);
				setAutocomplete(item.isChecked());
				return true;
//            disable until we migrate off of parse
//            case R.id.action_notifications:
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
////                TODO: modularize this so people can favorite different channels
//                PushConfig.setHandset(this, item.isChecked());
//                return true;
//            case R.id.action_subtle_notifications:
//                if (item.isChecked()) item.setChecked(false);
//                else item.setChecked(true);
////                TODO: modularize this so people can favorite different channels
//                PushConfig.setWear(this, item.isChecked());
//                return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setAutocomplete(boolean newSetting){
		EmoteDownloader ed = new EmoteDownloader();
		ed.mContext = this;
		Log.d("Autocomplete Setting", "turning autocomplete "+(newSetting ? "on" : "off") );
		if(newSetting){
			ed.execute();	
		}else{
			ed.execute("deletebyappid");
		}
	}
	
	private void configProportions(){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		if(width > height){
			screenLongSide = width;
			screenShortSide = height;
		}else{
			screenLongSide = height;
			screenShortSide = width;
		}
	}

    public void setWatcherSocket(final String platform, final String newChannel){
        try {
            if(overrustle_watcher_socket == null || !newChannel.equals(lastChannel) ) {
                if(overrustle_watcher_socket != null){
                    overrustle_watcher_socket.disconnect();
                }

                Log.d("Socket.IO", "Creating overrustle_watcher_socket socket");
                overrustle_watcher_socket = IO.socket("http://api.overrustle.com/stream");
                // Receiving an object
//                final String oPath = String.format("/destinychat?s=%s&stream=%s", platform, newChannel);
                final String oPath = String.format("/%s/%s", platform, newChannel);
                Log.d(TAG, "Watch Path: "+oPath);

                overrustle_watcher_socket.on("connect", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //Log.d("Socket.IO", "Recieved data from socket");
                        Log.d(TAG, "Got viewer data!");
                        Log.d(TAG, Arrays.toString(args));
                        overrustle_watcher_socket.emit("watch", oPath);
                    }
                });

                overrustle_watcher_socket.on("rustlers", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //Log.d("Socket.IO", "Recieved data from socket");
                        Log.d(TAG, "Got viewer data!");
                        Log.d(TAG, Arrays.toString(args));

//                        thisActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                // TODO:
//                                // consider showing viewer counts somewhere
//                            }
//                        });
                    }
                });
            }
            if(!overrustle_watcher_socket.connected()) {
                Log.d("Socket.IO", "Connecting overrustle_watcher_socket socket");
                overrustle_watcher_socket.connect();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
    }

    private void loadChannel(String channel) {
        loadChannel(channel, null);
    }

    private void loadChannel(String channel, String platform){
        this.lastChannel = this.channel;
        this.channel = channel;
        Business.DownloadTask dt = new Business.DownloadTask();
        dt.platform = platform;
        dt.qualityPicker = mQualityPicker;
        dt.spinner_item = R.layout.simple_spinner_item;
        dt.qualities = qualityOptions;
        dt.video = video;
        // TODO find a cleaner way to set title from asynctask
        dt.mActivity = this;
        //dt.header = header;
        //dt.channelSearch = et;
        dt.context = this;// getApplicationContext();
        dt.execute(channel);

//        if(channel.equals("gameongg")){
////        	loadFeaturedStreamButton.setVisibility(View.GONE);
//        }else{
//            if(gameonlive){
////            	loadFeaturedStreamButton.setVisibility(View.VISIBLE);
//            }
//        }

        // TODO figure out what this code was for
        // delete if pointless
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }


    //helper methods
	//UI modes:
//	fullscreen portrait
//	fullscreen landscape
//	topofscreen portrait
//	bottomright_corner portrait
	
	boolean isFullscreen(){
		return isFullscreen(getWindow().getAttributes());
	}
	boolean isFullscreen(WindowManager.LayoutParams attrs){
		if(attrs == null){
			attrs = getWindow().getAttributes();			
		}
		boolean currentlyFullscreen = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		return currentlyFullscreen;
	}
	
	private void toggleFullscreen()	{
		if(isFullscreen()){
			disableFullscreen();
		}else{
			enableFullscreen();
		}
	}

	private void enableFullscreen(){
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		enableFullscreen(attrs);
	}
	// this is fine because constants are set at compile? time
	@SuppressLint("InlinedApi")
	private void enableFullscreen(WindowManager.LayoutParams attrs){
		// change the actual screen
//		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//    	attrs.flags |= WindowManager.LayoutParams.FLAG_;
//		attrs.flags |= WindowManager.LayoutParams.FLAG_
		rootView.setSystemUiVisibility(
	            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
	            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
	            | View.SYSTEM_UI_FLAG_IMMERSIVE);
//		getWindow().setAttributes(attrs);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		setFullscreen(true);
	}
	

	private void disableFullscreen(WindowManager.LayoutParams attrs){
		//rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        getWindow().setAttributes(attrs);
//      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		setFullscreen(false);
        // 0 clears the flags
		rootView.setSystemUiVisibility(0);
//		.setSystemUiVisibility(
//	            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//	            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//	            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}
	
	private void disableFullscreen(){
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		disableFullscreen(attrs);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	void setFullscreen(boolean enable){
		if(enable){
			enableFullscreen();
//			rootView.setSystem
//			rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			//rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}else{
			disableFullscreen();
//			rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			//rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}
	
	private boolean shownUI = true;
	void showUI(){
        //show UI widgets
        //header_container.setVisibility(View.VISIBLE);
//        navigation.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        shownUI = true;
	}
	void hideUI(){
		Log.d("Called", "hide ui");
        //hide UI widgets
        //header_container.setVisibility(View.GONE);
//        navigation.setVisibility(View.INVISIBLE);
        // hide open keyboards
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(wvr.getWindowToken(), 0);

        shownUI = false;
	}
	
// Minimized stream mode
	
	public boolean inMinimode = false;
		
	private void toggleMinimode(){
		//video.setTop(0);
//		video.invalidate();
		setMinimode(!inMinimode);
		//video.setTop(0);
//		video.invalidate();
	}
	private void setMinimode(boolean b){
		if(b){
			ResizeViewTo(video, "small");
			if(isLandscape()){
				disableFullscreen();
			}else{
				showUI();
			}
		}else{
			ResizeViewTo(video, "original");
			if(isLandscape()){
				enableFullscreen();
			}else{
				hideUI();
			}
		}
		inMinimode = b;
	}

// Resizing streams
	
    private static void ResizeViewTo(View view, int width, int height){
    	Log.d("resizing to preciely", String.valueOf(width)+" by "+String.valueOf(height));
    	ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) view.getLayoutParams();
    	params.width = width;
    	params.height = height;
        view.setLayoutParams(params);
    	view.layout(view.getLeft(), view.getTop(), width, height);
//        view.requestLayout();
        //view.setDimensions(width, height);
        
    }
    private void ResizeViewTo(View view, String sizeName){
    	// tiny is for Audio Only mode
    	Log.d("resizing to", sizeName);
    	if(sizeName.startsWith("tiny")){
    		ResizeViewTo(view, 10, 10);
    	}else if(sizeName.startsWith("small")){
    		DisplayMetrics metrics = new DisplayMetrics(); 
    		getWindowManager().getDefaultDisplay().getMetrics(metrics);
    		int wd = (int) ((screenLongSide)/MINIMODE_RATIO);
    		int ht = (int) ((screenShortSide)/MINIMODE_RATIO);
    		ResizeViewTo(view, wd, ht);
    	}else{ // if(original){
    		ResizeViewTo(view, ogwidth, ogheight);
    	}
    }
    
// orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            if(!inMinimode){
                enableFullscreen();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            if(isFullscreen() == false){
                disableFullscreen();
            }
        }
    }
    
    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        Point size = new Point();
        getOrient.getSize(size);
        if(size.x < size.y){
        	orientation = Configuration.ORIENTATION_PORTRAIT;
        }else { 
        	orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }
    public boolean isLandscape(){
    	return getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }
    public void alert(Context context){
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
    	        context);
    	 
    	// set the title of the Alert Dialog
    	alertDialogBuilder.setTitle("Warning: you don\'t have at least Android 4.4");
    	 
    	// set dialog message
    	alertDialogBuilder
    	        .setMessage("Chat won\'t work on older versions of Android yet. I will post on the Destiny subreddit when I solve this problem.")
    	        .setCancelable(true)
    	        .setPositiveButton("NoTears",
    	                new DialogInterface.OnClickListener() {
    	                    public void onClick(DialogInterface dialog,
    	                            int id) {
    	                        // if yes is clicked, close
    	                        // current activity
    	                        //MainActivity.this.finish();
    	                    	dialog.cancel();
    	                    }
    	                });
    	 
    	AlertDialog alertDialog = alertDialogBuilder.create();
    	 
    	alertDialog.show();

    }
}
