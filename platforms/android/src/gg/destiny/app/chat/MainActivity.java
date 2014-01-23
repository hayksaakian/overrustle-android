package gg.destiny.app.chat;

//import android.R;
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
import android.view.inputmethod.*;

import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
//import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.*;

//import com.mogoweb.chrome.*;
//import com.webviewbrowser.*;

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

    private RelativeLayout header_container;

//    NOTE: WebView is created at runtime
//YoutubeLayout youtubeLayout;
    LinearLayout youtubeLayout;

    ResizingVideoView video;
    private EditText et;
    TextView header;
    private TextView pageLoadTime;

    private Spinner qualityPicker;
    public HashMap qualityOptions;
    String lastQuality = "";
    String channel;

    WebViewer wvr;

    // remember the original attributes of the video view

    private int ogwidth;
    private int ogheight;


    @Override
    public void onConfigurationChanged(Configuration newConfig){
//        youtubeLayout.maximize();
        toggleFullscreen(); // to force resize of video p1
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//          et.setVisibility(View.INVISIBLE);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//          et.setVisibility(View.VISIBLE);
        }
        //hack
        toggleFullscreen(); // to force resize of video p2
//        video.requestLayout();
//        video.forceLayout();
//        video.invalidate();

//        video.la
//        youtubeLayout.requestLayout();
//    	video.requestLayout();

    }

	@Override
	public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//                wv = (WebView) findViewById(R.id.wv);
//        youtubeLayout = (YoutubeLayout) findViewById(R.id.youtubeLayout);
        youtubeLayout = (LinearLayout) findViewById(R.id.youtubeLayout);
//        youtubeLayout = (YoutubeLayout) findViewById(R.id.youtubeLayout);
        // TODO move to a better place
//        youtubeLayout.maximize();
        video = (ResizingVideoView)findViewById(R.id.video);
        ViewGroup.LayoutParams ogparams = (ViewGroup.LayoutParams) video.getLayoutParams();
        ogheight = ogparams.height;
        ogwidth = ogparams.width;
        header_container = (RelativeLayout)findViewById(R.id.header_container);
        header = (TextView)findViewById(R.id.header);
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

        channel = et.getText().toString();
        loadChannel(channel);

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

        qualityPicker.setOnItemSelectedListener(this);

        OnTouchListener vlistener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	Log.d("Touched Video", event.toString());
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

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    //helper methods
	//UI modes:
//	fullscreen portrait
//	fullscreen landscape
//	topofscreen portrait
//	bottomright_corner portrait


	private void toggleFullscreen()	{
		//toggle title bar
		//header_container.setVisibility(View.GONE);

		// system fullscreen
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		boolean currentlyFullscreen = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		if(currentlyFullscreen){
			disableFullscreen(attrs);
			header_container.setVisibility(View.VISIBLE);
		}else{
			enableFullscreen(attrs);
            header_container.setVisibility(View.GONE);
            //youtubeLayout.setVisibility(View.VISIBLE);

		}
	}

	private void enableFullscreen(){
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		enableFullscreen(attrs);
	}
	private void enableFullscreen(WindowManager.LayoutParams attrs){
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    	//attrs.flags |= WindowManager.LayoutParams.FLAG_;
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        getWindow().setAttributes(attrs);
	}


		private void disableFullscreen(WindowManager.LayoutParams attrs){
	        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
	        getWindow().setAttributes(attrs);
//	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		}
		private void disableFullscreen(){
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			disableFullscreen(attrs);
		}

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
			wvr.setAlignParentTop(true);
			// also make the chat match_parent
		}else{
			ResizeViewTo(video, "original");
			wvr.setAlignParentTop(false);
			// also make the chat match_parent
		}
		inMinimode = b;
	}

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
}