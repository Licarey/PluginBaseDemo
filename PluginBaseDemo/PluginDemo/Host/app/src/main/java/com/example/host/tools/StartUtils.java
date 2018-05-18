package com.example.host.tools;

import android.content.Context;
import android.content.Intent;

import com.example.host.ProxyService;
import com.example.host.manager.ActivityManager;

public class StartUtils {

    /**
     * 启动插件中的指定activity
     * 
     * @param pluginPath
     * @param activityName
     *            要启动的插件的activity名
     */
    public static void startActivity(String pluginPath, String activityName) {
        ActivityManager.getInstance().startActivity(activityName, pluginPath);
    }

    /**
     * 启动插件中的指定service
     * 
     * @param context
     * @param pluginPath
     * @param serviceName
     *            要启动的插件的activity名
     */
    public static void startService(Context context, String pluginPath, String serviceName) {
        Intent i = new Intent(context, ProxyService.class);
        ProxyService.SERVICE_APK_PATH = pluginPath;
        ProxyService.SERVICE_CLASS_NAME = serviceName;
        context.startService(i);
    }

    /**
     * 停止插件中的指定service
     *
     * @param context
     * @param pluginPath
     * @param serviceName
     *            要启动的插件的activity名
     */
    public static void stopService(Context context, String pluginPath, String serviceName) {
        Intent i = new Intent(context, ProxyService.class);
        ProxyService.SERVICE_APK_PATH = pluginPath;
        ProxyService.SERVICE_CLASS_NAME = serviceName;
        context.stopService(i);
    }
}
