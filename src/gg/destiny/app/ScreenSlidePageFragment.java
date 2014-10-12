package gg.destiny.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Hayk on 10/9/2014.
 */
public class ScreenSlidePageFragment extends Fragment{
    public static final String ARG_OBJECT = "object";

    WebViewer wvr;
    int TAB_NUM;

    public String home_url = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);
        Bundle args = getArguments();
        TAB_NUM = args.getInt(ARG_OBJECT);


//        // setup wvr
        wvr = new WebViewer();
        wvr.contentContainer = (RelativeLayout) rootView.findViewById(R.id.chat_container);
        wvr.loparams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        //wvr.loparams.addRule(RelativeLayout.BELOW, R.id.youtubeLayout);
        wvr.backToLoadedURLButton = (Button)rootView.findViewById(R.id.back_button);
        wvr.chatInput = (EditText)rootView.findViewById(R.id.input);
        wvr.nativeWV = (WebView)rootView.findViewById(R.id.webview);
        wvr.pageLoadTime = (TextView)rootView.findViewById(R.id.page_load_time);
        wvr.Make(getActivity().getApplicationContext());

        if(TAB_NUM == 0) {
            home_url = "http://www.destiny.gg/embed/chat";
        }else if(TAB_NUM == 1) {
            home_url = "https://kiwiirc.com/client/irc.twitch.tv/#destiny";
        }else{
            home_url = "http://www.example.com/";
        }

        wvr.LoadURL(home_url);

//        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                Integer.toString());
        return rootView;
    }
}
