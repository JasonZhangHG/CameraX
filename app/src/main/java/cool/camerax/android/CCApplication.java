package cool.camerax.android;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

public class CCApplication extends Application {

    private static CCApplication INSTANCE;

    public CCApplication() {
        INSTANCE = this;
    }

    public static CCApplication getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            INSTANCE = new CCApplication();
            INSTANCE.onCreate();
            return INSTANCE;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        Utils.init(getApplicationContext());
    }
}

