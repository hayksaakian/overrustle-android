package gg.destiny.app;

import java.util.concurrent.*;

import org.apache.cordova.*;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ChatActivity extends Activity implements CordovaInterface
{
    public static String TAG = "ChatActivity";
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    CordovaWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        Config.init(this);
        webView = (CordovaWebView) findViewById(R.id.web_view);
        //webView.loadUrl(Config.getStartUrl());
        //webView.loadUrl("file:///android_asset/www/chat2.html");
        webView.loadUrl("file:///android_asset/www/chat-lite.html");
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public Activity getActivity()
    {
        return this;
    }

    @Override
    public Object onMessage(String id, Object data)
    {
        //Log.d(TAG, "onMessage(" + id + "," + data + ")");
        if ("onReceivedError".equals(id)) {
            Log.e(TAG, "onReceivedError");
        }
        else if ("onPageFinished".equals(id)) {
            //webView.loadUrl("javascript:alert(window.cordova);");
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool()
    {
        return threadPool;
    }

}
