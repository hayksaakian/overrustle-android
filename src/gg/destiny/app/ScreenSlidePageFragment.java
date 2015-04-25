package gg.destiny.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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

    public static WebViewer mainV;
    public static WebViewer targetV;
    public static ViewPager pager;

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
        wvr.page_num = TAB_NUM;
        wvr.parentFragment = this;
        wvr.contentContainer = (RelativeLayout) rootView.findViewById(R.id.chat_container);
        wvr.loparams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        //wvr.loparams.addRule(RelativeLayout.BELOW, R.id.youtubeLayout);
        wvr.backButton = (Button)rootView.findViewById(R.id.back_button);
        wvr.homeButton = (Button)rootView.findViewById(R.id.home_button);

        wvr.chatInput = (EditText)rootView.findViewById(R.id.input);
        if (Business.isKitkat() == false){
            wvr.chatInput.setVisibility(View.VISIBLE);
        }

        wvr.nativeWV = (WebView)rootView.findViewById(R.id.webview);
        wvr.pageLoadTime = (TextView)rootView.findViewById(R.id.page_load_time);
        wvr.Make(getActivity().getApplicationContext());

        if(TAB_NUM == 0) {
            home_url = "https://www.destiny.gg/embed/chat";
            mainV = wvr;
        }else if(TAB_NUM == 1) {
            home_url = "https://www.google.com/?gws_rd=ssl";
            targetV = wvr;
        }else{
            home_url = "https://kiwiirc.com/client/irc.twitch.tv/#destiny";
        }
        // TODO: load the chat room appropriate to the current stream

        wvr.homeURL = home_url;
        wvr.LoadURL(home_url);

//        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                Integer.toString());
        return rootView;
    }
}
