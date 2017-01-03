package javine.com.designproject.receiverproxy;

import java.io.File;
import java.io.IOException;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import javine.com.designproject.UPFApplication;
import javine.com.designproject.util.Utils;

/**
 * Created by KuangYu on 2017/1/3 0003.
 */
public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    public static CustomClassLoader getPluginClassLoader(File plugin, String packageName) throws IOException{
        return new CustomClassLoader(plugin.getPath(),
                Utils.getPluginOptDexDir(packageName).getPath(),
                Utils.getPluginLibDir(packageName).getPath(),
                UPFApplication.getContext().getClassLoader());
    }
}
