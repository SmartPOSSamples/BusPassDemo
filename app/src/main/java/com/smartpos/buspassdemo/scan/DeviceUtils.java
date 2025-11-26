package com.smartpos.buspassdemo.scan;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;

import com.smartpos.buspassdemo.MainActivity;


/**
 * Created by gecx on 16-11-8.
 */

public class DeviceUtils {

    /**
     * dp转px
     * @param context
     * @param dipValue  dp
     * @return px
     */
    public static int dp2px(Context context, int dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        Log.d("123", "density:" + density);
        return (int)(dipValue * density + 0.5f);
    }

    /**
     * px转dp
     * @param context
     * @param pxValue px
     * @return dp
     */
    public static int px2dp(Context context, int pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/density + 0.5f);
    }

    /**
     * sp转px
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 屏幕宽度
     * @param context
     * @return px
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 屏幕高度
     * @param context
     * @return px
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getStausHeight(Context context) {
        int height = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getViewTop(Context context) {
        return ((MainActivity) context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }
}
