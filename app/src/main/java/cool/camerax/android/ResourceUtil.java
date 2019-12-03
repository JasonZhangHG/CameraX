package cool.camerax.android;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class ResourceUtil {

    public static int getColor(int colorResId) {
        return ContextCompat.getColor(CCApplication.getInstance(), colorResId);
    }

    public static int getDimensionPixelSize(int dimenRes) {
        return CCApplication.getInstance().getResources().getDimensionPixelSize(dimenRes);
    }

    public static String getString(int strResId) {
        return CCApplication.getInstance().getString(strResId);
    }

    public static String getString(int strResId, Object... objects) {
        return String.format(getString(strResId), objects);
    }

    @Nullable
    public static Drawable getDrawable(int drawableResId) {
        if (drawableResId == 0) {
            return null;
        } else {
            return ContextCompat.getDrawable(CCApplication.getInstance(), drawableResId);
        }
    }
}
