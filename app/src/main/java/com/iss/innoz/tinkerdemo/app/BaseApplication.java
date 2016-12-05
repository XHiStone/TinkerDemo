package com.iss.innoz.tinkerdemo.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.multidex.MultiDex;

import com.iss.innoz.tinkerdemo.tinker.TinkerServerManager;
import com.iss.innoz.tinkerdemo.tinker.util.TinkerManager;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * SampleAndroidProject
 * com.app.sampleandroidproject.app
 *
 * @Author: xie
 * @Time: 2016/9/2 9:58
 * @Description:
 */

@DefaultLifeCycle(application = "com.iss.innoz.tinkerdemo.app.MyBaseApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class BaseApplication extends DefaultApplicationLike {


    public BaseApplication(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent, Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        MultiDex.install(base);
        TinkerManager.setTinkerApplicationLike(this);

        TinkerManager.installTinker(this);

        //初始化TinkerPatch 服务器 SDK
        TinkerServerManager.installTinkerServer(getApplication(), Tinker.with(getApplication()), 3);
        //每隔访问三小时服务器是否有更新
        TinkerServerManager.checkTinkerUpdate(false);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinkerServerManager.checkTinkerUpdate(true);
    }

    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

}
