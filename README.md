# TinkerDemo
它是微信官方的Android热补丁解决方案，它支持动态下发代码、So库以及资源，让应用能够在不需要重新安装的情况下实现更新。
##框架集成说明
###1.build.gradle(Project)
```
dependencies {
       ...
        classpath "com.tencent.tinker:tinker-patch-gradle-plugin:1.7.3"
    }
```
###2.build.gradle(Module)
```
 defaultConfig {
        ...
        multiDexEnabled true
        buildConfigField "String", "APP_KEY", "\"d43825ef836b3ca0\""
        buildConfigField "String", "APP_VERSION", "\"1.0.0\""
    }
```
```
dependencies {
    ...
    compile 'com.android.support:multidex:1.0.1'
    compile("com.tencent.tinker:tinker-server-android:0.3.0")
    compile("com.tencent.tinker:tinker-android-lib:1.7.3")
    provided("com.tencent.tinker:tinker-android-anno:1.7.3")
}
```
####在app的build目录下创建一个bakApk文件夹用了存储oldApp的
```
def bakPath = file("${buildDir}/bakApk/")
```
####定义ext相当于java静态类定义静态变量，此类需要特别注意tinkerOldApkPath、tinkerApplyMappingPath、tinkerApplyResourcePath首次可随便填写，但是第二次需要生成补丁时需要正确填写bakApk目录下的文件名否侧打补丁无法通过，tinkerApplyMappingPath文件可能没有生成但不影响使用
```
ext {
    //for some reason, you may want to ignore tinkerBuild, such as instant run debug build?
    tinkerEnabled = true
    //for normal build
    //old apk file to build patch apk
    tinkerOldApkPath = "${bakPath}/app-debug-1205-10-39-12.apk"
    tinkerApplyMappingPath = "${bakPath}/app-debug-1205-10-39-12-mapping.txt"
    //resource R.txt to build patch apk, must input if there is resource changed
    tinkerApplyResourcePath = "${bakPath}/app-debug-1205-10-39-12-R.txt"
}
```
####自定义gitSha方法提供TINKER_ID
```
def gitSha() {
        String gitRev = "1.0.0"//用户自定义
        return gitRev
}
```
####定义一些接下来需要使用的相关方法
```
def getApplyMappingPath() {
    return hasProperty("APPLY_MAPPING") ? APPLY_MAPPING : ext.tinkerApplyMappingPath
}
def getApplyResourceMappingPath() {
    return hasProperty("APPLY_RESOURCE") ? APPLY_RESOURCE : ext.tinkerApplyResourcePath
}
def getOldApkPath() {
    return hasProperty("OLD_APK") ? OLD_APK : ext.tinkerOldApkPath
}
def buildWithTinker() {
    return hasProperty("TINKER_ENABLE") ? TINKER_ENABLE : ext.tinkerEnabled
}
def getTinkerIdValue() {
    return hasProperty("TINKER_ID") ? TINKER_ID : gitSha()
}
```
####由ext的tinkerEnabled决定是否使用Tinker执行下面语句，特别需要注意，下面的loader的第二个参数必须填写项目中指定生成的Application
```
if (buildWithTinker()) {
    apply plugin: 'com.tencent.tinker.patch'

    tinkerPatch {
        oldApk = getOldApkPath()
        ignoreWarning = false
        useSign = true
        buildConfig {
            applyMapping = getApplyMappingPath()
            applyResourceMapping = getApplyResourceMappingPath()
            tinkerId = getTinkerIdValue()
        }

        dex {
            dexMode = "jar"
            usePreGeneratedPatchDex = false
            pattern = ["classes*.dex",
                       "assets/secondary-dex-?.jar"]
            loader = ["com.tencent.tinker.loader.*",
                      //warning, you must change it with your application
                      "com.iss.innoz.tinkerdemo.app.MyBaseApplication"
            ]
        }

        lib {
            pattern = ["lib/armeabi/*.so",
                       "lib/arm64-v8a/*.so",
                       "lib/armeabi-v7a/*.so",
                       "lib/mips/*.so",
                       "lib/mips64/*.so",
                       "lib/x86/*.so",
                       "lib/x86_64/*.so"]
        }

        res {
            pattern = ["res/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
            ignoreChange = ["assets/sample_meta.txt"]
            largeModSize = 100
        }

        packageConfig {
            configField("patchMessage", "tinker is sample to use")
        }
        sevenZip {
            zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
//        path = "/usr/local/bin/7za"
        }
    }

    List<String> flavors = new ArrayList<>();
    project.android.productFlavors.each { flavor ->
        flavors.add(flavor.name)
    }
    boolean hasFlavors = flavors.size() > 0
    /**
     * bak apk and mapping
     */
    android.applicationVariants.all { variant ->
        /**
         * task type, you want to bak
         */
        def taskName = variant.name
        def date = new Date().format("MMdd-HH-mm-ss")

        tasks.all {
            if ("assemble${taskName.capitalize()}".equalsIgnoreCase(it.name)) {

                it.doLast {
                    copy {
                        def fileNamePrefix = "${project.name}-${variant.baseName}"
                        def newFileNamePrefix = hasFlavors ? "${fileNamePrefix}" : "${fileNamePrefix}-${date}"

                        def destPath = hasFlavors ? file("${bakPath}/${project.name}-${date}/${variant.flavorName}") : bakPath
                        from variant.outputs.outputFile
                        into destPath
                        rename { String fileName ->
                            fileName.replace("${fileNamePrefix}.apk", "${newFileNamePrefix}.apk")
                        }

                        from "${buildDir}/outputs/mapping/${variant.dirName}/mapping.txt"
                        into destPath
                        rename { String fileName ->
                            fileName.replace("mapping.txt", "${newFileNamePrefix}-mapping.txt")
                        }

                        from "${buildDir}/intermediates/symbols/${variant.dirName}/R.txt"
                        into destPath
                        rename { String fileName ->
                            fileName.replace("R.txt", "${newFileNamePrefix}-R.txt")
                        }
                    }
                }
            }
        }
    }
}

```
###3.BaseApplication继承DefaultApplicationLike是Application的关联类并不是真正的Application但需要在此类执行需要在Application执行的操作
```
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

@DefaultLifeCycle(application = "com.iss.innoz.tinkerdemo.app.MyBaseApplication",//指定生成MyBaseApplication会在app/build/generated/source/debug/目录下生成com.iss.innoz.tinkerdemo.app.MyBaseApplication具体内容看下面MyBaseApplication
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
```
###4.在debug下指定生成MyBaseApplication,次Application会自动关联上面的BaseApplication以及实现内部相关方法
```
package com.iss.innoz.tinkerdemo.app;

import com.tencent.tinker.loader.app.TinkerApplication;

/**
 *
 * Generated application for tinker life cycle
 *
 */
public class MyBaseApplication extends TinkerApplication {

    public MyBaseApplication() {
        super(7, "com.iss.innoz.tinkerdemo.app.BaseApplication", "com.tencent.tinker.loader.TinkerLoader", false);
    }

}
```
###5.修改manifest类添加权限已经application
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.iss.innoz.tinkerdemo">
    <!--所需要的权限-->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--指定application，找不到的话重新Make Project就ok了-->
    <application
        android:name=".app.MyBaseApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <!--添加该服务为后面接收热修复操作服务-->
        <service
            android:name=".tinker.service.SampleResultService"
            android:exported="false" />
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```
###6.测试MainActivity
####1.首次运行生成oldApk到相应目录下
```
package com.iss.innoz.tinkerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toast.makeText(this,"测试",Toast.LENGTH_SHORT).show();
    }
}
```
####2.修改MainActivity然后执行打补丁操作
```
package com.iss.innoz.tinkerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this,"测试",Toast.LENGTH_SHORT).show();
    }
}
```
修改完毕之后点击AS右侧Gradle栏目下:app/tinker/tinkerPathDebug,鼠标双击tinkerPathDebug开始编译生成补丁文件
![image](https://github.com/18337129968/TinkerDemo/blob/master/photo/buding1.png)
编译失败图：这种情况属于gradle文件的oldApp三个路径填写错误找不到文件
![image](https://github.com/18337129968/TinkerDemo/blob/master/photo/buding2.png)
编译成功图：
![image](https://github.com/18337129968/TinkerDemo/blob/master/photo/buding3.png)
###7.上传补丁文件
####将打包完成携带有签名的patch_signed_7zip.apk上传到Tinker即可，然后打开app热修复完成后杀死并重新启动app完成热修复

[详细Tinker 接入指南](https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)<br>
[详细自定义Application](https://github.com/Tencent/tinker/wiki/Tinker-%E8%87%AA%E5%AE%9A%E4%B9%89%E6%89%A9%E5%B1%95)<br>
[Tinker API概览](https://github.com/Tencent/tinker/wiki/Tinker-API%E6%A6%82%E8%A7%88)<br>
[Tinker 常见问题](https://github.com/Tencent/tinker/wiki/Tinker-%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)<br>


