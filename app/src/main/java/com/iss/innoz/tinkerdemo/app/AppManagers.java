package com.iss.innoz.tinkerdemo.app;

import android.content.Context;

import com.iss.innoz.tinkerdemo.utils.CacheManager;
import com.iss.innoz.tinkerdemo.utils.Toastor;

import java.io.File;

/**
 * RxJavaDemo
 * com.isoftstone.rxjavademo.app
 *
 * @Author: xie
 * @Time: 2016/8/18 10:16
 * @Description:
 */

public class AppManagers {

    private static AppManagers appManagers = new AppManagers();
    private static CacheManager cacheManager;
    private static Toastor toastor;
    private static ActivitiesManager activities;
    private static RequestManager requestManager;

    public static AppManagers getAppManagers(Context context) {

        if (cacheManager == null) {
            cacheManager = CacheManager.get(context.getExternalCacheDir().getAbsolutePath() +
                    File.separator + "DataCache");
        }


        if (toastor == null) {
            toastor = new Toastor(context);
        }

        if (requestManager == null) {
            requestManager = RequestManager.get();
        }

        return appManagers;
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    public static Toastor getToastor() {
        return toastor;
    }

    public static ActivitiesManager getActivitiesManager() {
        if (activities == null) {
            activities = ActivitiesManager.getInstance();
        }
        return activities;
    }

    public static RequestManager getRequestManager() {
        return requestManager;
    }
}
