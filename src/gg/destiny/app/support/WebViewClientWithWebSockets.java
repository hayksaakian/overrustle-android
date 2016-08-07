package gg.destiny.app.support;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientWithWebSockets extends WebViewClient {
	// TODO: cache the websocket.js file instead of loading on on each page
	Context mContext;
	public WebViewClientWithWebSockets(Context context){
		mContext = context;
	}
	
	 @Override
     public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon)
     {
     	if(!url.startsWith("javascript")){
     		injectWebsocketScriptTag(view);
     	}
     	super.onPageStarted(view, url, favicon);
     }
	 

     String scheme = "localassets:";
     @Override
     public void onPageFinished(WebView view, String url)
     {
     	if(!url.startsWith("javascript")){
     		injectWebsocketScriptTag(view);
     	}
     }
     @Override
     public WebResourceResponse shouldInterceptRequest(WebView view, String url){
         if (url.startsWith(scheme)){
             try
                 {
                 return new WebResourceResponse(url.endsWith("js") ? "text/javascript" : "text/css", "utf-8",
                         mContext.getAssets().open(url.substring(scheme.length())));
                 }
             catch (IOException e)
                 {
                 Log.e(getClass().getSimpleName(), e.getMessage(), e);
                 }
             Log.d("scheme detected", scheme);
         }
         return null;
     }
     
     public void injectWebsocketScriptTag(WebView view){
     	view.loadUrl("javascript:(function() { \r\n"
     			+ "var script=document.createElement('script'); \r\n"
     			+ " script.setAttribute('type','text/javascript'); \r\n"
     			+ " script.setAttribute('src', 'localassets:www/js/websocket.js'); \r\n"
     			+ " script.onload = function(){ \r\n"
     			+ "		if(typeof(init) !== 'undefined'){\r\n"
     			+ "   	   	init(); \r\n"
     			+ " 	}; \r\n"
     			+ "		if(typeof(destiny) !== 'undefined'){\r\n"
     			+ "			console.log('we have destiny');" 
     			+ "			$('.input').attr('onclick', 'WebSocket.focusInput();');\r\n"
     			+ "   	   	destiny.chat.start(); \r\n"
     			+ " 		window.setInput = function(jInput){\r\n"
     			+ " 			$('.input').val(jInput).submit();\r\n"
     			+ " 		}; "
     			+ " 	}; \r\n"
     			+ " }; \r\n"
   	           	+ "	document.body.appendChild(script);\r\n"
   	           	+ "})();");
     }
}
