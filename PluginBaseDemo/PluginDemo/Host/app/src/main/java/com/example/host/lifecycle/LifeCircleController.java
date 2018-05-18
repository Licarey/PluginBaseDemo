package com.example.host.lifecycle;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;


import com.example.host.bean.PluginApkBean;
import com.example.host.ProxyActivity;
import com.example.host.manager.ActivityManager;

import august.plugin.PluginActivity;
import august.plugin.Pluginable;


/**
 * ProxyActivity生命周期以及资源管理类
 * Created by liming on 2018/5/16.
 */
public class LifeCircleController implements Pluginable {

    public static final String KEY_PLUGIN_CLASS_NAME = "plugin_class_name"; // 用来传递需要需要启动的Activity的类名
    public static final String KEY_PACKAGE_NAME = "package_name";   // Activity所在插件APK的包名
    Activity mActivityProxy;
    PluginActivity mPluginActivity;
    PluginApkBean mPluginApkBean;

    public LifeCircleController(ProxyActivity activityProxy) {
        this.mActivityProxy = activityProxy;
    }

    /**
     * 加载插件Activity
     *
     * @param classLoader
     * @param pluginName
     * @return
     */
    private PluginActivity loadPluginActivity(ClassLoader classLoader, String pluginName) {
        PluginActivity activity = null;
        try {
            Class cls = classLoader.loadClass(pluginName);
            activity = (PluginActivity) cls.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    /**
     * 代理获取资源
     *
     * @return
     */
    public Resources getResources() {
        return null != mPluginApkBean ? mPluginApkBean.resources : null;
    }

    /**
     * 代理获取资源
     *
     * @return
     */
    public AssetManager getAssets() {
        return mPluginApkBean.resources.getAssets();
    }

    /**
     * 类加载器
     * @return
     */
    public ClassLoader getClassLoader() {
        return mPluginApkBean.classLoader;
    }

    /**
     * 解析需要启动的Activity
     *
     * @param extras
     */
    @Override
    public void onCreate(Bundle extras) {
        String pluginName = extras.getString(KEY_PLUGIN_CLASS_NAME);
        String packageName = extras.getString(LifeCircleController.KEY_PACKAGE_NAME);
        mPluginApkBean = ActivityManager.getInstance().getPluginApk(packageName);   //  获取加载的插件APK信息
        mPluginActivity = loadPluginActivity(mPluginApkBean.classLoader, pluginName);   // 加载插件Activity
        mPluginActivity.attach(mActivityProxy); //  绑定代理Activity到插件Activity中, 使其能够调用代理Activity对应的应用资源等方法
        mPluginActivity.onCreate(null); //  代理生命周期
    }

    /**
     *代理生命周期
     */
    @Override
    public void onRestart() {
        mPluginActivity.onRestart();
    }

    @Override
    public void onStart() {
        mPluginActivity.onStart();
    }

    @Override
    public void onResume() {
        mPluginActivity.onResume();
    }

    @Override
    public void onPause() {
        mPluginActivity.onPause();
    }

    @Override
    public void onStop() {
        mPluginActivity.onStop();
    }

    @Override
    public void onDestroy() {
        mPluginActivity.onDestroy();
    }
}
