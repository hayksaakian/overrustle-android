package gg.destiny.app;

//import android.R;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.view.MotionEventCompat;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.Window.Callback;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.*;

import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
//import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.*;

//import com.mogoweb.chrome.*;
//import com.webviewbrowser.*;
import gg.destiny.app.R;

import java.util.*;

import android.view.View.OnFocusChangeListener;

public class MainActivity extends Activity implements OnItemSelectedListener
{
	
	@Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String qualityName = ((Spinner) parent).getSelectedItem().toString();
        qualityOptions = Business.GetCachedHash(channel + "|cache", this);
        Log.d("quality setting", qualityName);
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
                	qualityName = "Low";  
                }
            }
            if(qualityOptions.containsKey(qualityName)){
            	// only reload if this is a different quality
            	if(!lastQuality.startsWith(qualityName)){
                    String url = (String)qualityOptions.get(qualityName);
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
    
    private RelativeLayout header_container;
    private RelativeLayout navigation;

//    NOTE: WebView is created at runtime
//YoutubeLayout youtubeLayout;
    LinearLayout youtubeLayout;
    
    ResizingVideoView video;
    private EditText et;
    Button loadFeaturedStreamButton;
    TextView header;
    private TextView pageLoadTime;

    private Spinner qualityPicker;
    public HashMap qualityOptions;
    String lastQuality = "";
    String channel;

    WebViewer wvr;
//    SystemUIHider uiHider;
    
    // remember the original attributes of the video view
    private int ogwidth;
    private int ogheight;

	private boolean isOnCreateDone = false;

	boolean gameonlive = false;

	@Override
	public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//                wv = (WebView) findViewById(R.id.wv);
//        youtubeLayout = (YoutubeLayout) findViewById(R.id.youtubeLayout);
        youtubeLayout = (LinearLayout) findViewById(R.id.youtubeLayout);
        video = (ResizingVideoView)findViewById(R.id.video);
        ViewGroup.LayoutParams ogparams = (ViewGroup.LayoutParams) video.getLayoutParams();
        ogheight = ogparams.height;
        ogwidth = ogparams.width;
        header_container = (RelativeLayout)findViewById(R.id.header_container);
        header = (TextView)findViewById(R.id.header);
        navigation = (RelativeLayout)findViewById(R.id.navigation);
        loadFeaturedStreamButton = (Button)findViewById(R.id.load_gameongg_stream);
        //newActivityBtn = (ImageButton) findViewById(R.id.new_activity);
        qualityPicker = (Spinner)findViewById(R.id.quality_picker);
        pageLoadTime = (TextView) findViewById(R.id.page_load_time);
        et = (EditText) findViewById(R.id.et);

        // setup wvr
        wvr = new WebViewer();
        wvr.contentContainer = (RelativeLayout) findViewById(R.id.chat_container);
        wvr.loparams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        
        //wvr.loparams.addRule(RelativeLayout.BELOW, R.id.youtubeLayout); 
        wvr.backToLoadedURLButton = (Button)findViewById(R.id.back_button);
        wvr.pageLoadTime = pageLoadTime;
        wvr.Make(this);
        wvr.LoadURL("http://www.destiny.gg/embed/chat");
        
        RelativeLayout everythingelse = (RelativeLayout)findViewById(R.id.everything_else);
        everythingelse.bringToFront();
        
        
//      setup events
        OnFocusChangeListener toggle = new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    header.setMaxLines(Integer.MAX_VALUE);
                    header.setEllipsize(null);
                    Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();
                }
                else
                {
                    header.setMaxLines(1);
                    header.setEllipsize(TextUtils.TruncateAt.END);
                    Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
                }
            }
        };

        header.setOnFocusChangeListener(toggle);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	channel = et.getText().toString();
                    loadChannel(channel);
                    return true;
                } else {
                    return false;
                }
            }
        });
        loadFeaturedStreamButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				et.setText("gameongg");
				loadChannel("gameongg");
			}
		});

        qualityPicker.setOnItemSelectedListener(this);
        
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
        if(!Business.isKitkat()){
        	alert(this);
        }
        isOnCreateDone  = true;
        

        channel = et.getText().toString();
        loadChannel(channel);
        
        // destiny + check on gameon.gg
        checkStatus("destiny", Business.MLG_STREAMS_STATUS_URL);
        //checkStatus();       
        
	} // TODO </ on create >
	
	private void checkStatus(String... channel_name_or_mlg_urls){
        Business.LiveChecker chkgameon = new Business().new LiveChecker();
        chkgameon.goToStreamButton = loadFeaturedStreamButton;
        chkgameon.channelSearch = et;
        chkgameon.execute(channel_name_or_mlg_urls);		
	}

	private void loadChannel(String channel) {
        Business.DownloadTask dt = new Business.DownloadTask();
        dt.qualityPicker = qualityPicker;
        dt.spinner_item = R.layout.simple_spinner_item;
        dt.qualities = qualityOptions;
        dt.video = video;
        dt.header = header;
        dt.channelSearch = et;
        dt.context = this;// getApplicationContext();
        dt.execute(channel);
        
        if(channel.equals("gameongg")){
        	loadFeaturedStreamButton.setVisibility(View.GONE);
        }else{
        	if(gameonlive){
            	loadFeaturedStreamButton.setVisibility(View.VISIBLE);        		
        	}
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
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
        header_container.setVisibility(View.VISIBLE);
        navigation.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        shownUI = true;
	}
	void hideUI(){
		Log.d("Called", "hide ui");
        //hide UI widgets
        header_container.setVisibility(View.GONE);
        navigation.setVisibility(View.INVISIBLE);
        // hide open keyboards
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(wvr.getWindowToken(), 0);

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
			if(isLandscape())
				disableFullscreen();
		}else{
			ResizeViewTo(video, "original");
			if(isLandscape())
				enableFullscreen();
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
    		int wd = (int) (320f*metrics.density);
    		int ht = (int) (180f*metrics.density);
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