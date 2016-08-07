/*
 * Copyright (c) 2010 Animesh Kumar  (https://github.com/anismiles)
 * Copyright (c) 2010 Strumsoft  (https://strumsoft.com)
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *  
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *  
 */
package gg.destiny.app.support;

import java.net.URI;
import java.util.Random;
import java.util.Vector;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;

/**
 * The <tt>WebSocketFactory</tt> is like a helper class to instantiate new
 * WebSocket instaces especially from Javascript side. It expects a valid
 * "ws://" URI.
 * 
 * @author Animesh Kumar
 */
public class WebSocketFactory {
	
	private Vector<HybridWebSocket> socketList = new Vector<HybridWebSocket>();
	
	private Handler handler;

	/** The app view. */
	WebView appView;
	
	EditText inputField;

	/**
	 * Instantiates a new web socket factory.
	 *
	 */
	public WebSocketFactory(Handler h, WebView appViewm, EditText inputField) {
		this.appView = appViewm;
		this.handler = h;
		this.inputField = inputField;
	}
	
	@JavascriptInterface
	public Vector<HybridWebSocket> getSocketList() {
		return socketList;
	}

	@JavascriptInterface
	public HybridWebSocket getInstance(String url) {
		// use Draft76 by default
		return getInstance(url, null);
	}

	@JavascriptInterface
	public HybridWebSocket getInstance(String url, String cookie) {
		HybridWebSocket socket = null;
		Thread th = null;
		try {
//			socket = new WebSocket(handler, appView, new URI(url), draft, getRandonUniqueId(), cookie);
			socket = new HybridWebSocket(handler, appView, new URI(url), null, getRandonUniqueId(), cookie, inputField);
			socketList.add(socket);
			th = socket.connect();
			return socket;
		} catch (Exception e) {
			//Log.v("websocket", e.toString());
			if(th != null) {
				th.interrupt();
			}
		} 
		return null;
	}

	/**
	 * Generates random unique ids for WebSocket instances
	 * 
	 * @return String
	 */
	private String getRandonUniqueId() {
		return "WEBSOCKET." + new Random().nextInt(100);
	}

}
