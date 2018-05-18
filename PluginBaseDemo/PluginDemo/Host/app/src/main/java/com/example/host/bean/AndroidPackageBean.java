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
package com.example.host.bean;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.example.host.manager.PluginClassLoader;

import dalvik.system.DexClassLoader;

/**包信息
 * Created by liming on 2018/5/16.
 */
public class AndroidPackageBean {

    /**
     * 插件的Application名
     */
    public String applicationName;
    /**
     * 插件的Application
     */
    public Application pluginApplication;
    /**
     * 插件路径
     */
    public String pluginPath;
    /**
     * 插件资源管理器
     */
    public AssetManager pluginAssets;
    /**
     * 插件资源
     */
    public Resources pluginRes;
    /**
     * 插件包信息
     */
    public PackageInfo pluginPkgInfo;
    /**
     * 插件加载器
     */
    public DexClassLoader pluginLoader;

    /**
     * 绑定apk路径
     * 
     * @param apkPath
     */
    public void attach(String apkPath) {
        pluginPath = apkPath;
    }

    public void setPluginPkgInfo(PackageInfo pluginPkgInfo) {
        this.pluginPkgInfo = pluginPkgInfo;
    }

    public void setPluginRes(Resources pluginRes) {
        this.pluginRes = pluginRes;
    }

    public void setPluginAssets(AssetManager pluginAssets) {
        this.pluginAssets = pluginAssets;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 是否能够使用？
     * 
     * @return
     */
    public boolean canUse() {
        return pluginPkgInfo != null && pluginLoader != null
                && pluginPath != null;
    }

    /**
     * 与一个类加载器绑定
     * 
     * @param ctx
     */
    public void bindDexLoader(Context ctx) {
        pluginLoader = PluginClassLoader.getClassLoader(pluginPath, ctx,
                PluginClassLoader.getSystemLoader() == null ? ctx.getClassLoader()
                        : PluginClassLoader.getSystemLoader());
    }
}
