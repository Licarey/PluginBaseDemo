package com.example.host.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.example.host.ProxyActivity;
import com.example.host.lifecycle.LifeCircleController;
import com.example.host.bean.PluginApkBean;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Activity 加载器管理工具
 * Created by liming on 2018/5/16.
 */
public class ActivityManager {

    private Context mContext;

    Map<String, PluginApkBean> sMap = new HashMap<String, PluginApkBean>();

    private ActivityManager() {}

    static class PluginManagerHolder {
        static ActivityManager sManager = new ActivityManager();
    }

    public static ActivityManager getInstance() {
        return PluginManagerHolder.sManager;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 加载并创建APK的信息
     *
     * @param apkPath
     * @return
     */
    public PluginApkBean loadApk(String apkPath) {
        PackageInfo packageInfo = queryPackageInfo(apkPath);    // 获取未安装的插件APK包信息
        if (packageInfo == null || TextUtils.isEmpty(packageInfo.packageName)) {
            return null;
        }
        PluginApkBean pluginApkBean = sMap.get(packageInfo.packageName);    // 从缓存中获取
        if (pluginApkBean == null) {
            pluginApkBean = createApk(apkPath); // 缓存中不存在, 则开始创建APK信息
            if (pluginApkBean != null) {
                // 缓存
                pluginApkBean.packageInfo = packageInfo;
                sMap.put(packageInfo.packageName, pluginApkBean);
            } else {
                throw new NullPointerException("plugin apk is null");
            }
        }
        return pluginApkBean;
    }

    public PluginApkBean getPluginApk(String packageName) {
        return sMap.get(packageName);
    }

    /**
     * 创建一个Entity保存APK的信息
     *
     * @param apkPath
     * @return
     */
    private PluginApkBean createApk(String apkPath) {
        PluginApkBean pluginApkBean = null;
        try {
            // 事实就是跟前面那样动态加载资源的原理是一样的
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);
            Resources resources = new Resources(assetManager, mContext.getResources().getDisplayMetrics(),
                    mContext.getResources().getConfiguration());
            pluginApkBean = new PluginApkBean(resources);
            pluginApkBean.classLoader = createDexClassLoader(apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pluginApkBean;
    }

    /**
     * 根据apk路径创建对应的ClassLoader
     *
     * @param apkPath
     * @return
     */
    private ClassLoader createDexClassLoader(String apkPath) {
        File dexOptDir = mContext.getDir("dex", Context.MODE_PRIVATE);
        if (!dexOptDir.exists()) {
            dexOptDir.mkdir();
        }
        DexClassLoader dexClassLoader = new DexClassLoader(apkPath, dexOptDir.getAbsolutePath(), null,
                mContext.getClassLoader());
        return dexClassLoader;
    }

    /**
     * 查询APK的包信息
     *
     * @param apkPath
     * @return
     */

    private PackageInfo queryPackageInfo(String apkPath) {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return packageInfo;
    }

    /**
     * 打开一个Activity
     *
     * @param pluginActivityClzz
     * @param apkPath
     */
    public void startActivity(String pluginActivityClzz, String apkPath) {
        PluginApkBean pluginApkBean = ActivityManager.getInstance().loadApk(apkPath);
        Intent intent = new Intent(mContext, ProxyActivity.class);
        intent.putExtra(LifeCircleController.KEY_PLUGIN_CLASS_NAME, pluginActivityClzz);
        intent.putExtra(LifeCircleController.KEY_PACKAGE_NAME, pluginApkBean.packageInfo.packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
