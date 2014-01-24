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
	public Button backToLoadedURLButton;
	public EditText chatInput;
	
	boolean loadWebSocketShim;
	
	android.webkit.WebView nativeWV;
	//com.mogoweb.chrome.WebView chromiumWV;
	
	private ViewGroup vagueWebView;
	
	private String lastLoadedURL = "";

	WebViewer(){
//		load the websocket code if we dont have kitkat
		this.loadWebSocketShim = !Business.isKitkat();
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
		
		backToLoadedURLButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(lastLoadedURL.length() > 0){
					LoadURL(lastLoadedURL);
				}
			}
		});
		
	}
	
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
	private WebView configureNativeWebView(WebView webView, Context context){
		
        webView.setWebChromeClient(new android.webkit.WebChromeClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        android.webkit.WebSettings settings = webView.getSettings();
        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new android.webkit.WebViewClient() {
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
        webView.getSettings().setJavaScriptEnabled(true);
		return webView;
	}
	
	// generic methods
	private void genericOnPageStarted(View view, String url, Bitmap favicon){
        //et.setText(url);
        pageStartTime = System.currentTimeMillis();
        pageLoadTime.setText("0ms");
        if(!url.startsWith(lastLoadedURL)){
        	//shoe back button
        	backToLoadedURLButton.setVisibility(View.VISIBLE);
        }else{
        	//hide back button
        	backToLoadedURLButton.setVisibility(View.GONE);
        }
	}
	
	private void genericOnPageFinished(View view, String url){
		if (pageStartTime == 0)
        {
        }
        else
        {
            long loadTime = (System.currentTimeMillis() - pageStartTime);
            pageLoadTime.setText(String.format("%sms to load chat", loadTime));
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
