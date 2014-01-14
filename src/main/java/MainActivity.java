package main.java;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.webkit.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.webviewbrowser.*;
import java.util.*;

import android.view.View.OnFocusChangeListener;


public class MainActivity extends Activity implements OnItemSelectedListener
{

	private RelativeLayout header_container;

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
		else if (qualityOptions != null && qualityOptions.containsKey(qualityName))
		{
			//else{
				String url = (String)qualityOptions.get(qualityName);
				Business.PlayURL(video, url);
			//}
		}
		
		// todo: cache, remember chosen quality
    }

	@Override
    public void onNothingSelected(AdapterView<?> parent)
	{
        // Another interface callback
    }

	private long pageStartTime = 0;
	private WebView wv;
	VideoView video;
	//private ImageButton newActivityBtn;
	private EditText et;
	TextView header;
	private TextView pageLoadTime;

	private Spinner qualityPicker;
	public HashMap qualityOptions;
	String channel;

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
			et.setVisibility(View.INVISIBLE);
			//hack to force the video to resize
			//toggleFullscreen();
			//toggleFullscreen();
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
			et.setVisibility(View.VISIBLE);
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
		wv = (WebView) findViewById(R.id.wv);
		video = (VideoView)findViewById(R.id.video);
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
		else
		{
			//et.setText("");
		}

		// setup wv
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			WebView.setWebContentsDebuggingEnabled(true);
		}
		wv.setWebChromeClient(new WebChromeClient());
		wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		WebSettings settings = wv.getSettings();
		settings.setAllowUniversalAccessFromFileURLs(true);
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setAppCacheEnabled(false);
		settings.setDomStorageEnabled(true);
		wv.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon)
				{
					super.onPageStarted(view, url, favicon);
					//et.setText(url);
					pageStartTime = System.currentTimeMillis();
					pageLoadTime.setText("0ms");
				}

				@Override
				public void onPageFinished(WebView view, String url)
				{
					super.onPageFinished(view, url);
					if (pageStartTime == 0)
					{
					}
					else
					{
						long loadTime = (System.currentTimeMillis() - pageStartTime);
						pageLoadTime.setText(String.format("%sms to load chat", loadTime));
						System.out.println(String.format("page load time: %sms", loadTime));
					}
				}
			});
		handleLoadUrl();
		wv.loadUrl("http://www.destiny.gg/embed/chat");

		// setup events
//		newActivityBtn.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View v, MotionEvent event)
//				{
//					if (event.getAction() == MotionEvent.ACTION_DOWN)
//					{
//						newActivityBtn.setColorFilter(getResources().getColor(android.R.color.holo_blue_dark));
//						return false;
//					}
//					else if (event.getAction() == MotionEvent.ACTION_UP)
//					{
//						newActivityBtn.setColorFilter(null);
//						return false;
//					}
//					return false;
//				}
//			});
//		newActivityBtn.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v)
//				{
//					Intent intent = new Intent(MainActivity.this, MainActivity.class);
//					intent.putExtra("url", et.getText().toString());
//					startActivity(intent);
//				}
//			});

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
						handleLoadUrl();
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


	private void handleLoadUrl()
	{
		channel = et.getText().toString();

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
