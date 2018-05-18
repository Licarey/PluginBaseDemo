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


import android.app.Service;

import com.example.host.manager.ApkManager;

/**
 * 加载service 信息实体
 * Created by liming on 2018/5/16.
 */
public class ServicePluginBean {
    /**
     * 插件所属apk
     */
    AndroidPackageBean from;
    /**
     * 插件实体Service
     */
    private Service CurrentPluginService;
    /**
     * 插件的第一个Service
     */
    private String topServiceName = null;

    public ServicePluginBean(Service proxyParent, String apkPath) {
        from = ApkManager.get(apkPath);
        from.bindDexLoader(proxyParent);
    }

    public AndroidPackageBean from() {
        return from;
    }

    public void setCurrentPluginService(Service currentPluginService) {
        CurrentPluginService = currentPluginService;
    }

    /**
     * @return 当前的插件Service
     */
    public Service getCurrentPluginService() {
        return CurrentPluginService;
    }

    /**
     * 设置当前的插件Service
     * 
     */
    public void setTopServiceName(String topServiceName) {
        this.topServiceName = topServiceName;
    }

    public String getTopServiceName() {
        return topServiceName;
    }
}
