package com.example.host;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.host.manager.ActivityManager;
import com.example.host.resources.LoadedResource;
import com.example.host.resources.ResourceManager;
import com.example.host.tools.StartUtils;


public class MainActivity extends Activity {

    ImageView imageView;
    ActivityManager mPluginManager = ActivityManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ResourceManager.init(this);
        mPluginManager.init(this);
        imageView = (ImageView) findViewById(R.id.imageView);

    }

    /**
     * 加载已安装APK资源
     *
     * @param v
     */
    public void loadInstalledBundle(View v) {
        Drawable drawable = ResourceManager.installed().getDrawable("cn.com.bestbuy.rteam", "check_selected");
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * 加载未安装APK资源
     *
     * @param v
     */
    public void loadUninstalledBundle(View v) {
        LoadedResource loadResource = ResourceManager.unInstalled().loadResource("/storage/sdcard0/plugin.apk");
        Drawable drawable = ResourceManager.unInstalled().getDrawable(loadResource.packageName, "plugin_img");
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }


    /**
     * 加载未安装的APK的Activity
     *
     * @param v
     */
    public void loadUninstalledActivity(View v) {
        StartUtils.startActivity("/storage/sdcard0/plugin.apk" , "com.example.plugin.MainActivity");
    }

    /**
     * 开启未安装的APK的service
     * @param v
     */
    public void loadUninstalledService(View v){
        StartUtils.startService(this , "/storage/sdcard0/plugin.apk" , "com.example.plugin.PluginService");
    }

    /**
     * 停止未安装的APK的service
     * @param v
     */
    public void stopUninstalledService(View v){
        StartUtils.stopService(this , "/storage/sdcard0/plugin.apk" , "com.example.plugin.PluginService");
    }
}
