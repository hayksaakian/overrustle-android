package gg.destiny.app;

import android.app.Application;

public class DestinyApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        //ChromeInitializer.initialize(this);
    }
}
