package com.example.host;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.example.host.lifecycle.LifeCircleController;

/**
 * 代理activity
 * Created by liming on 2018/5/14.
 */
public class ProxyActivity extends Activity {

    /**
     * 代理生命周期
     */
    private LifeCircleController mPluginController = new LifeCircleController(this);    //  用于管理代理Activity生命周倩和资源的类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPluginController.onCreate(getIntent().getExtras());

        /**
         * 弹出插件string
         */
        Toast.makeText(this , getResources().getString(getResources().getIdentifier("app_name" , "string" , "com.example.plugin")) , 0).show();
    }

    @Override
    public Resources getResources() {
        Resources resources = mPluginController.getResources();
        return null != resources ? resources : super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        AssetManager assets = mPluginController.getAssets();
        return null != assets ? mPluginController.getAssets() : super.getAssets();
    }

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader loader = mPluginController.getClassLoader();
        return null != loader ? loader : super.getClassLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPluginController.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPluginController.onDestroy();
    }
}
