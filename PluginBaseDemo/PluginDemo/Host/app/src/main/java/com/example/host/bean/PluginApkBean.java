package com.example.host.bean;

import android.content.pm.PackageInfo;
import android.content.res.Resources;

/**
 * 用来存放已经加载的插件APK信息
 * Created by liming on 2018/5/16.
 */
public class PluginApkBean {
    /**
     * 包信息
     */
    public PackageInfo packageInfo;
    /**
     * 资源
     */
    public Resources resources;
    /**
     * 类加载器
     */
    public ClassLoader classLoader;

    public PluginApkBean(Resources resources) {
        this.resources = resources;
    }
}
