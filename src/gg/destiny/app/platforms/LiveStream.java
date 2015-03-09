package gg.destiny.app.platforms;

/**
 * Created by Hayk on 3/8/2015.
 */
public class LiveStream {
    public String title;
    public boolean live;

    public LiveStream(){
        title = "offline or the app is broken";
        live = false;
    }

    public LiveStream(String _title, boolean _live){
        title = _title;
        live = _live;
    }
}
