package javine.com.designproject.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javine.com.designproject.UPFApplication;

/**
 * Created by KuangYu on 2017/1/3 0003.
 */
public class Utils {
    /**
     * 把Assets里面的文件复制到/data/data/files 目录下
     * @param context
     * @param sourceName
     */
    public static void extractAsserts(Context context, String sourceName){
        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = am.open(sourceName);
            File extractFile = context.getFileStreamPath(sourceName);
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0){
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            closeSilently(is);
            closeSilently(fos);
        }
    }

    private static void closeSilently(Closeable closeable){
        if (closeable == null){
            return;
        }
        try {
            closeable.close();
        }catch (Throwable e){
            // ignore
        }
    }

    public static File getPluginOptDexDir(String packageName){
        return enforceDirExists(new File(getPluginBaseDir(packageName), "odex"));
    }

    public static File getPluginLibDir(String packageName){
        return enforceDirExists(new File(getPluginBaseDir(packageName), "lib"));
    }

    private static File sBaseDir;
    private static File getPluginBaseDir(String packageName){
        if (sBaseDir == null){
            sBaseDir = UPFApplication.getContext().getFileStreamPath("plugin");
            enforceDirExists(sBaseDir);
        }
        return enforceDirExists(new File(sBaseDir, packageName));
    }

    private static synchronized File enforceDirExists(File sBaseDir){
        if (!sBaseDir.exists()){
            boolean ret = sBaseDir.mkdir();
            if (!ret){
                throw new RuntimeException("create dir "+sBaseDir+" failed");
            }
        }
        return sBaseDir;
    }

}
