package com.example.host.resources;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * 插件资源加载器
 * Created by liming on 2018/5/16.
 */
public class ResourceManager {

    private static final String TAG = "ResourceManager";

    private ResourceManager() {}

    public static void init(Context context) {
        UnInstalled.sManager.init(context);
        Installed.sManager.init(context);
    }

    public static UnInstalled unInstalled() {
        return UnInstalled.sManager;
    }

    public static Installed installed() {
        return Installed.sManager;
    }

    /**
     * 针对于未安装应用
     */
    public static class UnInstalled {

        static final UnInstalled sManager = new UnInstalled();

        private Context mContext;
        private Map<String, LoadedResource> mRescources = new HashMap<String, LoadedResource>();
        private String mDexDir;

        private UnInstalled() {

        }

        /**
         * 初始化
         *
         * @param context
         */
        public void init(Context context) {
            mContext = context.getApplicationContext();
            File dexDir = mContext.getDir("dex", Context.MODE_PRIVATE);
            if (!dexDir.exists()) {
                dexDir.mkdir();
            }
            mDexDir = dexDir.getAbsolutePath();
        }

        /**
         * 获取未安装应用资源的ID
         *
         * @param packageName
         * @param fieldName
         * @return
         */
        public int getResourceID(String packageName, String type, String fieldName) {
            int resID = 0;
            LoadedResource recource = getUnInstalledRecource(packageName);
            String rClassName = packageName + ".R$" + type;
            try {
                Class cls = recource.classLoader.loadClass(rClassName);
                resID = (Integer) cls.getField(fieldName).get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resID;
        }

        /**
         * 获取未安装应用Drawable
         *
         * @param packageName
         * @param fieldName
         * @return
         */
        public Drawable getDrawable(String packageName, String fieldName) {
            Drawable drawable = null;
            int resourceID = getResourceID(packageName, "drawable", fieldName);
            LoadedResource recource = getUnInstalledRecource(packageName);
            if (recource != null) {
                drawable = recource.resources.getDrawable(resourceID);
            }
            return drawable;
        }

        /**
         * 加载未安装应用资源包
         *
         * @param resourcePath
         * @return
         */
        public LoadedResource loadResource(String resourcePath) {

            LoadedResource loadResource = null;

            PackageInfo info = queryPackageInfo(resourcePath);
            if (info != null) {
                loadResource = mRescources.get(info.packageName);
                if (loadResource == null) {
                    try {
                        AssetManager assetManager = AssetManager.class.newInstance();
                        Class cls = AssetManager.class;
                        Method method = cls.getMethod("addAssetPath", String.class);
                        method.invoke(assetManager, resourcePath);// 反射设置资源加载路径
                        Resources resources = new Resources(assetManager, mContext.getResources().getDisplayMetrics(),
                                mContext.getResources().getConfiguration());
                        loadResource = new LoadedResource();
                        loadResource.resources = resources;
                        loadResource.packageName = info.packageName;
                        loadResource.classLoader = new DexClassLoader(resourcePath, mDexDir, null,
                                mContext.getClassLoader());
                        mRescources.put(info.packageName, loadResource);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return loadResource;

        }

        /**
         * 获取未安装应用PackageInfo
         *
         * @param resourcePath
         * @return
         */
        private PackageInfo queryPackageInfo(String resourcePath) {
            return mContext.getPackageManager().getPackageArchiveInfo(resourcePath, PackageManager.GET_ACTIVITIES);
        }

        /**
         * 获取未安装应用LoadResource
         *
         * @param packageName
         * @return
         */
        public LoadedResource getUnInstalledRecource(String packageName) {
            LoadedResource loadResource = mRescources.get(packageName);
            if (loadResource == null) {
                Log.e(TAG, "resource " + packageName + " not founded");
            }
            return loadResource;
        }

    }

    /**
     * 针对于已安装应用
     */
    public static class Installed {
        static final Installed sManager = new Installed();

        private Context mContext;
        private Map<String, LoadedResource> mResources = new HashMap<String, LoadedResource>();

        private Installed() {

        }

        /**
         * 初始化
         *
         * @param context
         */
        public void init(Context context) {
            mContext = context.getApplicationContext();
        }

        /**
         * 获取已安装应用资源
         *
         * @param packageName
         */
        public LoadedResource getInstalledResource(String packageName) {

            LoadedResource resource = mResources.get(packageName);

            if (resource == null) {
                try {
                    Context context = mContext.createPackageContext(packageName,
                            Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    resource = new LoadedResource();
                    resource.packageName = packageName;
                    resource.resources = context.getResources();
                    resource.classLoader = context.getClassLoader();
                    mResources.put(packageName, resource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return resource;
        }

        /**
         * 获取资源ID
         *
         * @param packageName
         * @param type
         * @param fieldName
         * @return
         */
        public int getResourceID(String packageName, String type, String fieldName) {

            int resID = 0;
            LoadedResource installedResource = getInstalledResource(packageName);    // 获取已安装APK的资源
            if (installedResource != null) {
                String rClassName = packageName + ".R$" + type;    // 根据匿名内部类的命名, 拼写出R文件的包名+类名
                try {
                    Class cls = installedResource.classLoader.loadClass(rClassName);    //	加载R文件
                    resID = (Integer) cls.getField(fieldName).get(null);    //	反射获取R文件对应资源名的ID
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "resource is null:" + packageName);
            }
            return resID;
        }

        /**
         * 获取已加载应用Drawable
         *
         * @param packageName
         * @param fieldName
         * @return
         */
        public Drawable getDrawable(String packageName, String fieldName) {
            Drawable drawable = null;
            int resourceID = getResourceID(packageName, "drawable", fieldName);
            LoadedResource installedResource = getInstalledResource(packageName);
            if (installedResource != null) {
                drawable = installedResource.resources.getDrawable(resourceID);
            }
            return drawable;
        }
    }

}
