package gg.destiny.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import com.mogoweb.chrome.*;
import android.widget.TextView.OnEditorActionListener;

//import android.webkit.*;
//import android.webkit.WebView;
//import com.mongoweb.chrome.WebView;

import gg.destiny.app.support.*;

public class WebViewer {
	public long pageStartTime = 0;
	public TextView pageLoadTime;
	public RelativeLayout.LayoutParams loparams;
	public RelativeLayout contentContainer;

    public Button backButton;
    public Button homeButton;

	public EditText chatInput;
	
	boolean loadWebSocketShim;
	
	android.webkit.WebView nativeWV;
	//com.mogoweb.chrome.WebView chromiumWV;
	
	private ViewGroup vagueWebView;
	
	private String lastLoadedURL = "";
    public String homeURL = "";

    private WebViewer self;
    public int page_num;

    // leaky abstraction
    public ScreenSlidePageFragment parentFragment;

	WebViewer(){
//		load the websocket code if we dont have kitkat
		this.loadWebSocketShim = !Business.isKitkat();
        self = this;
	}
	public void Make(Context context){
		boolean needToMake = nativeWV == null;
		if(needToMake){
			nativeWV = new android.webkit.WebView(context);
		}
		vagueWebView = nativeWV;
		if(needToMake){
			vagueWebView.setLayoutParams(loparams);
			contentContainer.addView(vagueWebView);
		}
		vagueWebView.requestFocus(); 
		nativeWV = configureNativeWebView(nativeWV, context);
		
		if (loadWebSocketShim){
			SetUpWebSockets(context);
		}
		
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nativeWV.goBack();
			}
		});

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadURL(homeURL);
            }
        });
		
	}

    // this is only called on clients which need websockets
	void SetUpWebSockets(Context context){
		// configure the input used to send messages to chat
		chatInput.setVisibility(View.VISIBLE);
		// TODO read on a key-by-key basis and set the text in the webview
		chatInput.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEND) {
		            String inp = v.getText().toString();
		        	Log.d("input", inp);
		        	nativeWV.loadUrl("javascript:window.setInput(\""+inp+"\")");
		        	v.setText("");
		            handled = true;
		        }
		        return handled;
		    }
		});
		
		nativeWV.setWebViewClient(new WebViewClientWithWebSockets(context){
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                genericOnPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url){
                super.onPageFinished(view, url);
                genericOnPageFinished(view, url);
            }
			
		});

		nativeWV.addJavascriptInterface(new WebSocketFactory(null, nativeWV, chatInput), "WebSocketFactory");
		nativeWV.loadData("", "text/html", null);
		
	}
	
	public void LoadURL(String url){
		lastLoadedURL = url;
		nativeWV.loadUrl(url);
	}	
	
//	native version
	private WebView configureNativeWebView(final WebView webView, Context context){
		
        webView.setWebChromeClient(new android.webkit.WebChromeClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        android.webkit.WebSettings settings = webView.getSettings();
        // fixes a crash on ISC
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            settings.setAllowUniversalAccessFromFileURLs(true);    
        }
//        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon){
                // if the "brower" exists and we are in "chat" then load load links in the browser
                // Log.d("WV_VARS0", String.valueOf(page_num) +": "+url);
                // Log.d("WV_VARS1", String.valueOf(parentFragment != null));
                // Log.d("WV_VARS2", String.valueOf(parentFragment.targetV != null));
                // Log.d("WV_VARS3", String.valueOf(parentFragment.mainV == self));
                // Log.d("WV_VARS4", String.valueOf(!url.startsWith(homeURL)));
                if(parentFragment != null
                        && parentFragment.targetV != null
                        && parentFragment.mainV == self
                        && !url.startsWith(homeURL)){
                    webView.stopLoading();
                    parentFragment.targetV.LoadURL(url);
                    // also animate, and page to page 2
                    parentFragment.pager.setCurrentItem(1);
                }else{
                    super.onPageStarted(view, url, favicon);
                    genericOnPageStarted(view, url, favicon);
                }
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url){
                super.onPageFinished(view, url);
                genericOnPageFinished(view, url);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
		return webView;
	}
	
	// generic methods
	private void genericOnPageStarted(View view, String url, Bitmap favicon){
        //et.setText(url);
        lastLoadedURL = url;
        pageStartTime = System.currentTimeMillis();
        pageLoadTime.setText("0ms");
        if(url.startsWith(homeURL)){
            //hide back button
            backButton.setVisibility(View.GONE);
            homeButton.setVisibility(View.INVISIBLE);
        }else{
            //show back button
            backButton.setVisibility(View.VISIBLE);
            // if we're more than 1 page away from home, show a home button
            if(nativeWV != null){
                WebBackForwardList history = nativeWV.copyBackForwardList();
                if(history.getSize() > 0
                        && history.getCurrentIndex() > 0
                        && !history.getItemAtIndex(history.getCurrentIndex()-1).getUrl().startsWith(homeURL) ){
                    homeButton.setVisibility(View.VISIBLE);
                }
            }
        }

	}
	
	private void genericOnPageFinished(View view, String url){
		if (pageStartTime == 0)
        {
        }
        else
        {
            long loadTime = (System.currentTimeMillis() - pageStartTime);
            pageLoadTime.setText(String.format("%sms to load page", loadTime));
            System.out.println(String.format("page load time: %sms", loadTime));
        }
//        if(url != lastLoadedURL){
//        	//shoe back button
//        	backToLoadedURLButton.setVisibility(View.VISIBLE);
//        }else{
//        	//hide back button
//        	backToLoadedURLButton.setVisibility(View.GONE);
//        }
	}
	
	// TODO deprecate
	public void setAlignParentTop(boolean b) {
		// TODO Auto-generated method stub
//		contentContainer.`
		RelativeLayout.LayoutParams cloParams;
		cloParams = (RelativeLayout.LayoutParams)contentContainer.getLayoutParams();
		if(b){
			cloParams.removeRule(RelativeLayout.BELOW);
			//cloParams.addRule(RelativeLayout.BELOW, R.id.header_container);
//			cloParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		}else{
//			cloParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
			cloParams.removeRule(RelativeLayout.BELOW);
			//cloParams.addRule(RelativeLayout.BELOW, R.id.everything_else);
		}
		contentContainer.setLayoutParams(cloParams);
		contentContainer.requestLayout();
		vagueWebView.requestLayout();
		
	}
	public IBinder getWindowToken() {
		// TODO Auto-generated method stub
		return vagueWebView.getWindowToken();
	}
 
	public String GetFileFromAssets(Context context, String path) throws IOException{
		StringBuilder buf=new StringBuilder();
	    InputStream json= context.getAssets().open(path);
	    BufferedReader in=
	        new BufferedReader(new InputStreamReader(json));
	    String str;

	    while ((str=in.readLine()) != null) {
	      buf.append(str);
	    }

	    in.close();
		
		return buf.toString();
	}

}
