package cool.camerax.android;

import com.blankj.utilcode.util.LogUtils;

public class CameraXLog {

    private final static String TAG = "CameraXLog";

    private String tag;

    public CameraXLog(String tag) {
        this.tag = tag;
    }

    public void v(String msg) {
        v(tag, msg);
    }

    public void d(String msg) {
        d(tag, msg);
    }

    public void i(String msg) {
        i(tag, msg);
    }

    public void w(String msg) {
        w(tag, msg);
    }

    public void e(String msg) {
        e(tag, msg);
    }

    public void e(String msg, Throwable throwable) {
        e(tag, msg, throwable);
    }

    public static int v(String tag, String msg) {
        if (!canLog()) {
            return 0;
        }
        LogUtils.vTag(tag, msg);
        return 1;
    }

    public static int d(String tag, String msg) {
        if (!canLog()) {
            return 0;
        }
        LogUtils.dTag(tag, msg);
        return 1;
    }

    public static int i(String tag, String msg) {
        if (!canLog()) {
            return 0;
        }
        LogUtils.iTag(tag, msg);
        return 1;
    }

    public static int w(String tag, String msg) {
        LogUtils.wTag(tag, msg);
        return 1;
    }

    public static int e(String tag, String msg) {
        LogUtils.eTag(tag, msg);
        return 1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        LogUtils.eTag(tag, msg, tr);
        return 1;
    }

    public static int json(String json) {
        if (!canLog()) {
            return 0;
        }
        LogUtils.json(json);
        return 1;
    }

    public static boolean canLog() {
        return (BuildConfig.DEBUG || BuildConfig.ENABLE_LOG);
    }


    public static int V(String msg) {
        return v(TAG, msg);
    }

    public static int D(String msg) {
        return d(TAG, msg);
    }

    public static int I(String msg) {
        return i(TAG, msg);
    }

    public static int W(String msg) {
        return w(TAG, msg);
    }

    public static int E(String msg) {
        return e(TAG, msg);
    }

    public static int E(String msg, Throwable tr) {
        return e(TAG, msg, tr);
    }

}