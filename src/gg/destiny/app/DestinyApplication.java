package gg.destiny.app;

import android.app.Application;
import com.mogoweb.chrome.ChromeInitializer;

public class DestinyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        ChromeInitializer.initialize(this);
    }
}
