package gg.destiny.app;

//import android.R;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
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

        private RelativeLayout header_container;

        private int ogwidth;

        private int ogheight;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, 
                                                           int pos, long id)
        {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
                String qualityName = ((Spinner) parent).getSelectedItem().toString();
                qualityOptions = Business.GetCachedHash(channel + "|cache", this);
                if(qualityName.startsWith("Chat")){
                        Log.d("quality setting", qualityName);
                        video.stopPlayback();
                        video.setVisibility(View.GONE);
                }
                else if (qualityOptions != null){
                        boolean audioOnly = false;
                        if(qualityName.startsWith("Audio")){
                                qualityName = "Low";
                                audioOnly = true;
                        }
                        if(qualityOptions.containsKey(qualityName)){
                        //else{
                                String url = (String)qualityOptions.get(qualityName);
                                Business.PlayURL(video, url);
                        }
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) video.getLayoutParams();
                        if(audioOnly){
                                //video.setVisibility(View.INVISIBLE);
                                //video.
                                //video.layout(0, 1, 0, 1);
                                
                                params.width = 10; // (int) (300*metrics.density);
                                params.height = 10;// (int) (250*metrics.density);
                                //params.leftMargin = 30;
                                //video.setLayoutParams(params);
                        }else{
                                params.width = ogwidth;
                                params.height = ogheight;
                        }
                        video.setLayoutParams(params);
                        
                }
                
                // todo: cache, remember chosen quality
    }

        @Override
    public void onNothingSelected(AdapterView<?> parent)
        {
        // Another interface callback
    }

        private long pageStartTime = 0;
//        private WebView wv;
        VideoView video;
        //private ImageButton newActivityBtn;
        private EditText et;
        TextView header;
        private TextView pageLoadTime;

        private Spinner qualityPicker;
        public HashMap qualityOptions;
        String channel;

        WebViewer wvr;
        
        RelativeLayout.LayoutParams ogparams;
        //ok
        @Override
        public void onConfigurationChanged(Configuration newConfig)
        {
                super.onConfigurationChanged(newConfig);

                // Checks the orientation of the screen
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//                        et.setVisibility(View.INVISIBLE);
                        //hack to force the video to resize
                        //toggleFullscreen();
                        //toggleFullscreen();
                }
                else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//                        et.setVisibility(View.VISIBLE);
                        //hack to force the video to resize
                        //toggleFullscreen();
                        //toggleFullscreen();
                }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
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
                
                
//              setup events
                OnFocusChangeListener toggle = new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus)
                        {
                                if (hasFocus)
                                {
                                        header.setMaxLines(Integer.MAX_VALUE);
                                        header.setEllipsize(null);
                                        //header.setHeight(

                                        //have tried so far is:
                                        //header.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

                                        Toast.makeText(getApplicationContext(), "got the focus", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                        header.setMaxLines(1);
                                        header.setEllipsize(TextUtils.TruncateAt.END);
                                        //header.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, "50dip"));
                                        Toast.makeText(getApplicationContext(), "lost the focus", Toast.LENGTH_LONG).show();
                                }
                        }
                };

                header.setOnFocusChangeListener(toggle);
                et.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event)
                    {
                        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                        {
                        	channel = et.getText().toString();
                            loadChannel(channel);
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                });

                qualityPicker.setOnItemSelectedListener(this);

                video.setOnTouchListener(new View.OnTouchListener()
                        {
                                @Override
                                public boolean onTouch(View v, MotionEvent event)
                                {
                                        int ac = event.getAction();
                                        if (ac == MotionEvent.ACTION_UP)
                                        {
                                                //Log.d("touch", "ACTION_DOWN");
                                                toggleFullscreen();
                                                return false;
                                        }else if(ac == MotionEvent.ACTION_MOVE || ac == MotionEvent.ACTION_CANCEL){
                                                return false;
                                        }

                                        //return super.onTouchEvent(event);
                                        return true;
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

        private void toggleFullscreen()
        {
                //toggle title bar
                //header_container.setVisibility(View.GONE);
                
                // system fullscreen
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                boolean fullscreen = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
                if (!fullscreen)
                {
                        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        
                        //attrs.flags |= WindowManager.LayoutParams.FLAG_;
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                        header_container.setVisibility(View.GONE);
                }
                else
                {
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        header_container.setVisibility(View.VISIBLE);
                }
                getWindow().setAttributes(attrs);
        }

}