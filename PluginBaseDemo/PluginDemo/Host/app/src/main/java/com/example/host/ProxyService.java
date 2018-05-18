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
package com.example.host;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.example.host.bean.ServicePluginBean;
import com.example.host.manager.ApkManager;
import com.example.host.manager.PluginClassLoader;
import com.example.host.reflect.Reflect;


/** 代理服务类
 * Created by liming on 2018/5/14.
 */
public class ProxyService extends Service {

    /**
     * 插件信息
     */
    private ServicePluginBean remote;
    public static String SERVICE_CLASS_NAME = Service.class.getName();
    public static String SERVICE_APK_PATH = PluginClassLoader.finalApkPath;

    @Override
    public IBinder onBind(Intent i) {
        return remote.getCurrentPluginService().onBind(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!remote.getCurrentPluginService().getClass().getName().equals(SERVICE_CLASS_NAME)) {
            initService();
            remote.getCurrentPluginService().onCreate();
        }
        return remote.getCurrentPluginService().onStartCommand(intent, flags,
                startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initService();
        remote.getCurrentPluginService().onCreate();
    }

    private void initService() {
        remote = new ServicePluginBean(this, SERVICE_APK_PATH);
        remote.setTopServiceName(SERVICE_CLASS_NAME);
        if (!remote.from().canUse()) {
            ApkManager.initApk(remote.from(), this);
        }

        try {
            Service plugin = (Service) remote.from().pluginLoader.loadClass(
                    remote.getTopServiceName()).newInstance();
            remote.setCurrentPluginService(plugin);
            Reflect thiz = Reflect.on(this);
            Reflect.on(plugin).call("attach", this, thiz.get("mThread"),
                    getClass().getName(), thiz.get("mToken"), getApplication(),
                    thiz.get("mActivityManager"));

        } catch (Exception e) {
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        remote.getCurrentPluginService().onStart(intent, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        remote.getCurrentPluginService().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return remote.getCurrentPluginService().onUnbind(intent);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        remote.getCurrentPluginService().onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {

        super.onRebind(intent);
        remote.getCurrentPluginService().onRebind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        remote.getCurrentPluginService().onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        remote.getCurrentPluginService().onLowMemory();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        remote.getCurrentPluginService().onTaskRemoved(rootIntent);
    }

    @Override
    public Resources getResources() {
        if (remote == null)
            return super.getResources();
        return remote.from().pluginRes == null ? super.getResources() : remote
                .from().pluginRes;
    }

    @Override
    public AssetManager getAssets() {
        if (remote == null)
            return super.getAssets();
        return remote.from().pluginAssets == null ? super.getAssets() : remote
                .from().pluginAssets;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (remote == null) {
            return super.getClassLoader();
        }
        if (remote.from().canUse()) {
            return remote.from().pluginLoader;
        }
        return super.getClassLoader();
    }
}
