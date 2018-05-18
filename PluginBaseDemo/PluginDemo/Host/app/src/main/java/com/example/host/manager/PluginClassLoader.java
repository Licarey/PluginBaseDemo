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

import android.content.Context;

import com.example.host.reflect.Reflect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

/**
 * 插件核心加载器
 * Created by liming on 2018/5/16.
 */
public class PluginClassLoader extends DexClassLoader {

    private static final Map<String, PluginClassLoader> pluginLoader = new ConcurrentHashMap<String, PluginClassLoader>();
    public static String finalApkPath;

    protected PluginClassLoader(String dexPath, String optimizedDirectory,
                                String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        finalApkPath = dexPath;
    }

    /**
     * 返回apk对应的加载器，不会重复加载同样的资源
     */
    public static PluginClassLoader getClassLoader(String dexPath, Context cxt, ClassLoader parent) {
        PluginClassLoader pluginDexLoader = pluginLoader.get(dexPath);
        if (pluginDexLoader == null) {
            // 获取到app的启动路径
            final String dexOutputPath = cxt.getDir("plugin", Context.MODE_PRIVATE).getAbsolutePath();
            final String libOutputPath = cxt.getDir("plugin_lib", Context.MODE_PRIVATE).getAbsolutePath();

            pluginDexLoader = new PluginClassLoader(dexPath, dexOutputPath, libOutputPath, parent);
            pluginLoader.put(dexPath, pluginDexLoader);
        }
        return pluginDexLoader;
    }

    public static ClassLoader getSystemLoader() {
        Context context = Reflect.on("android.app.ActivityThread")
                .call("currentActivityThread").call("getSystemContext").get();
        return context == null ? null : context.getClassLoader();
    }
}
