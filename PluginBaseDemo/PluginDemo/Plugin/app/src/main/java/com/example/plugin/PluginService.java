package com.example.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by liming on 2018/5/17.
 * email liming@finupgroup.com
 */
public class PluginService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this , "PluginService oncreate", 0).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this , "PluginService onDestroy", 0).show();
    }
}
