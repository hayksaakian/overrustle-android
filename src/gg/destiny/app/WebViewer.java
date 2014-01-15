package gg.destiny.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import com.mogoweb.chrome.*;

//import android.webkit.*;
//import android.webkit.WebView;
//import com.mongoweb.chrome.WebView;

public class WebViewer {
	public long pageStartTime = 0;
	public TextView pageLoadTime;
	public RelativeLayout.LayoutParams loparams;
	public RelativeLayout contentContainer;
	
	boolean useNative;
	android.webkit.WebView nativeWV;
	com.mogoweb.chrome.WebView chromiumWV;
	

	WebViewer(){
//		use native if we have websockets natively
		this.useNative = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
	public void Make(Context context){
		View view;
		if (useNative){
			nativeWV = createNativeWebView(context);
			view = nativeWV;
        }else{
        	chromiumWV = createChromiumWebView(context);
        	view = chromiumWV;
        }
//      
		view.setLayoutParams(loparams);
		contentContainer.addView(view);
		view.requestFocus();
	}
	
	public void LoadURL(String url){
		if(useNative){
			nativeWV.loadUrl(url);
		}else{
			Log.d("LoadURL", "via chromium");
			chromiumWV.loadUrl(url);
		}
	}	
	
//	chromium version
	private com.mogoweb.chrome.WebView createChromiumWebView(Context context) {
		com.mogoweb.chrome.WebView webView = new com.mogoweb.chrome.WebView(context);
        webView.setWebChromeClient(new com.mogoweb.chrome.WebChromeClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        com.mogoweb.chrome.WebSettings settings = webView.getSettings();
        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(com.mogoweb.chrome.WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new com.mogoweb.chrome.WebViewClient() {
            @Override
            public void onPageStarted(com.mogoweb.chrome.WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                //et.setText(url);
                pageStartTime = System.currentTimeMillis();
                pageLoadTime.setText("0ms");
            }

            @Override
            public void onPageFinished(com.mogoweb.chrome.WebView view, String url)
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
        webView.getSettings().setJavaScriptEnabled(true);
		return webView;
	}
	
//	native version
	private android.webkit.WebView createNativeWebView(Context context){
		android.webkit.WebView webView = new android.webkit.WebView(context);
        webView.setWebChromeClient(new android.webkit.WebChromeClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        android.webkit.WebSettings settings = webView.getSettings();
        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(com.mogoweb.chrome.WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(false);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                //et.setText(url);
                pageStartTime = System.currentTimeMillis();
                pageLoadTime.setText("0ms");
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url)
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
        webView.getSettings().setJavaScriptEnabled(true);
		return webView;
		
	}


}
