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
package com.example.host.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * 常用工具类
 * Created by liming on 2018/5/15.
 */
public class CommonTool {
    /**
     * 获取一个apk的信息
     * 
     * @param cxt
     *            应用上下文
     * @param apkPath
     *            apk所在绝对路径
     * @return
     */
    public static PackageInfo getAppInfo(Context cxt, String apkPath){
        PackageManager pm = cxt.getPackageManager();
        PackageInfo pkgInfo ;
        pkgInfo = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        return pkgInfo;
    }
}
