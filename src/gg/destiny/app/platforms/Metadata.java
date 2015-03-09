package gg.destiny.app.platforms;

import org.json.JSONObject;

/**
 * Created by Hayk on 3/8/2015.
 */
public class Metadata {
    public String name = null;
    public String channel;
    public String platform;
    public String url;
    public String image_url;
    public boolean live;
    public int viewers = 0;
    public int rustlers = 0;

    public Metadata(String _channel, String _platform){
        channel = _channel;
        platform = _platform;
        image_url = String.format("http://static-cdn.jtvnw.net/previews-ttv/live_user_%s-640x360.jpg", _channel.toLowerCase());
        live = true;
    }

    public Metadata(JSONObject o){
        try {
            if (o.has("name")){
                name = o.getString("name");
            }
            // a comment!
            channel = o.getString("channel");
            platform = o.getString("platform");
            url = o.getString("url");
            image_url = o.getString("image_url");
            live = o.has("live") ? o.getBoolean("live") : false;
            if(live){
                viewers = o.getInt("viewers");
            }
            rustlers = o.getInt("rustlers");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

//    show if someone is streaming on overrustle.com

    public String sidebarTitle(){
        if(name != null){
            return name;
        }
        return channel;
    }

    public String sidebarSubTitle(){
        if(name != null){
            return channel + " on " +platform;
        }
        return platform;
//        TODO: consider showing platform viewers too?
    }
}
