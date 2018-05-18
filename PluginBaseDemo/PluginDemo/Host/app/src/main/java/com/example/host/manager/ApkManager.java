/*
 * Copyright (c) 2014, 张涛, lody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.host.manager;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.example.host.bean.AndroidPackageBean;
import com.example.host.reflect.Reflect;
import com.example.host.tools.CommonTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * APK manager管理类
 * Created by liming on 2018/5/16.
 */
public final class ApkManager {

    private static final Map<String, AndroidPackageBean> apks = new ConcurrentHashMap<String, AndroidPackageBean>();
    private static final String TAG = ApkManager.class.getSimpleName();

    public static AndroidPackageBean get(String apkPath) {
        AndroidPackageBean apk;
        apk = apks.get(apkPath);
        if (apk == null) {
            apk = new AndroidPackageBean();
            apk.attach(apkPath);
            apks.put(apkPath, apk);
        }
        return apk;
    }

    public static void initApk(AndroidPackageBean apk, Context ctx) {
        String apkPath = apk.pluginPath;
        File file = new File(apkPath);
        if (!file.exists())
            try {
                throw new FileNotFoundException(apkPath);
            } catch (FileNotFoundException e) {
            }
        if (!apk.canUse()) {
            fillPluginInfo(apk, ctx);
            fillPluginRes(apk, ctx);
            fillPluginApplication(apk, ctx);
        } else {
            Log.i(TAG, "Plugin have been init.");
        }

    }

    private static void fillPluginInfo(AndroidPackageBean apk, Context ctx) {
        PackageInfo info ;
        try {
            info = CommonTool.getAppInfo(ctx, apk.pluginPath);
        } catch (Exception e) {
            throw new RuntimeException("file not found" + apk.pluginPath);
        }
        if (info == null) {
            throw new RuntimeException("Can't create Plugin from :"
                    + apk.pluginPath);
        }
        apk.setPluginPkgInfo(info);
        apk.setApplicationName(info.applicationInfo.className);
    }

    private static void fillPluginRes(AndroidPackageBean apk, Context ctx) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Reflect assetRef = Reflect.on(assetManager);
            assetRef.call("addAssetPath", apk.pluginPath);
            apk.setPluginAssets(assetManager);

            Resources pluginRes = new Resources(assetManager, ctx
                    .getResources().getDisplayMetrics(), ctx.getResources()
                    .getConfiguration());
            apk.setPluginRes(pluginRes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fillPluginApplication(AndroidPackageBean apk, Context ctx) {
        String applicationName = apk.applicationName;
        if (applicationName == null)
            return;
        if (applicationName.isEmpty())
            return;

        ClassLoader loader = apk.pluginLoader;
        if (loader == null)
            throw new RuntimeException("Not found ClassLoader in plugin!");
        try {
            Application pluginApp = (Application) loader.loadClass(applicationName).newInstance();
            Reflect.on(pluginApp).call("attachBaseContext", ctx.getApplicationContext());
            apk.pluginApplication = pluginApp;
            pluginApp.onCreate();

        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }
    }

}
