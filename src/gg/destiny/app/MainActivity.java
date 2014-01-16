package gg.destiny.app;

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
import android.view.inputmethod.*;

import android.widget.*;
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
                ResizeVideoTo(video, 10, 10);                        
            }else{
            	ResizeVideoTo(video, ogwidth, ogheight);
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
//        private WebView wv;
    VideoView video;
    private EditText et;
    TextView header;
    private TextView pageLoadTime;

    private Spinner qualityPicker;
    public HashMap qualityOptions;
    String lastQuality = "";
    String channel;

    WebViewer wvr;
    
    // remember the original attributes of the video view
    RelativeLayout.LayoutParams ogparams;
    private int ogwidth;
    private int ogheight;

    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//          et.setVisibility(View.INVISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//          et.setVisibility(View.VISIBLE);
        }
    }

	@Override
	public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//                wv = (WebView) findViewById(R.id.wv);
        video = (VideoView)findViewById(R.id.video);
        ogparams = (RelativeLayout.LayoutParams) video.getLayoutParams();
        ogheight = ogparams.height;
        ogwidth = ogparams.width;
        header_container = (RelativeLayout)findViewById(R.id.header_container);
        header = (TextView)findViewById(R.id.header);
        //newActivityBtn = (ImageButton) findViewById(R.id.new_activity);
        qualityPicker = (Spinner)findViewById(R.id.quality_picker);
        pageLoadTime = (TextView) findViewById(R.id.page_load_time);
        et = (EditText) findViewById(R.id.et);

        // setup edit text
        et.setSelected(false);
        if (getIntent().getStringExtra("url") != null)
        {
                et.setText(getIntent().getStringExtra("url"));
        }

        // setup wvr
        wvr = new WebViewer();
        wvr.contentContainer = (RelativeLayout) findViewById(R.id.lo);
        wvr.loparams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
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

        video.setOnTouchListener(new View.OnTouchListener() {
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
                    	toggleFullscreen();
                        return false;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d(DEBUG_TAG,"Action was CANCEL");
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                                "of current screen element");
                        return true;      
                    default : 
                        return true;
                }
            }
        });
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
	private void toggleFullscreen()	{
		//toggle title bar
		//header_container.setVisibility(View.GONE);
	
		// system fullscreen
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		boolean fullscreen = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		if (!fullscreen){
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        	//attrs.flags |= WindowManager.LayoutParams.FLAG_;
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            header_container.setVisibility(View.GONE);
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            header_container.setVisibility(View.VISIBLE);
        }
        getWindow().setAttributes(attrs);
	}
        
    private static void ResizeVideoTo(VideoView video, int width, int height){
    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) video.getLayoutParams();
    	params.width = width;
    	params.height = height;
        video.setLayoutParams(params);        	
    }
}