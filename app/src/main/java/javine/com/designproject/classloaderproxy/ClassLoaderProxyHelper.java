package javine.com.designproject.classloaderproxy;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import javine.com.designproject.receiverproxy.CustomClassLoader;
import javine.com.designproject.util.Utils;

/**
 * Created by KuangYu on 2017/1/3 0003.
 *
 */
public class ClassLoaderProxyHelper {

    /**
     * 将插件内容插入到host自带的ClassLoader中(方法二)
     * @param cl classLoader
     * @param apkFile 插件apk文件
     * @param optDexFile dexFile文件
     * @throws Exception
     */
    public static void patchClassLoader(ClassLoader cl, File apkFile, File optDexFile) throws Exception{
        //获取BaseDexClassLoader:pathList
        Field pathListField = DexClassLoader.class.getSuperclass().getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathListObj = pathListField.get(cl);

        //获取PathList:Element[] dexElements
        Field dexElementArray = pathListObj.getClass().getDeclaredField("dexElements");
        dexElementArray.setAccessible(true);
        Object[] dexElements = (Object[]) dexElementArray.get(pathListObj);

        //Element
        Class<?> elementClass = dexElements.getClass().getComponentType();

        //创建一个数组，替换原始数组
        Object[] newElements = (Object[]) Array.newInstance(elementClass, dexElements.length + 1);

        //构造插件Element构造函数
        Constructor<?> constructor = elementClass.getConstructor(File.class,boolean.class,File.class, DexFile.class);
        Object o = constructor.newInstance(apkFile, false, apkFile,
                DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0));

        Object[] toAddElementArray = new Object[]{o};
        //把原始的elements复制进去
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);
        //把插件的element复制进去
        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length, toAddElementArray.length);

        //替换
        dexElementArray.set(pathListObj, newElements);
    }

    public static Map<String, Object> sLoadedApk = new HashMap<>();

    /**
     * 将插件apk新建loadedApk对象，添加到mPackages中，
     * 其中loadedApk对象包含插件自己的ClassLoader，即可加载插件中的Class了
     * @param apkFile 插件的apk文件
     * @throws Exception
     */
    public static void hookLoadedApkInActivityThread(File apkFile) throws Exception{

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        //获取mPackages静态变量
        Field mPackagesField = activityThreadClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);
        Map mPackages = (Map) mPackagesField.get(currentActivityThread);

        //生成loadedApk对象，通过getPackageInfoNoCheck方法
        //需要两个参数对象
        //1.ApplicationInfo
        ApplicationInfo applicationInfo = generateApplicationInfo(apkFile);
        //2.CompatibilityInfo
        Class<?> compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
        Field defaultCompatibilityInfoField = compatibilityInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        defaultCompatibilityInfoField.setAccessible(true);
        Object defaultCompatibilityInfo = defaultCompatibilityInfoField.get(null);

        Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod("getPackageInfoNoCheck",
                ApplicationInfo.class, compatibilityInfoClass);
        Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread, applicationInfo, defaultCompatibilityInfo);

        ClassLoader classLoader = CustomClassLoader.getPluginClassLoader(apkFile, applicationInfo.packageName);
        Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(loadedApk, classLoader);

        sLoadedApk.put(applicationInfo.packageName, loadedApk);

        WeakReference weakReference = new WeakReference(loadedApk);
        mPackages.put(applicationInfo.packageName, weakReference);
    }

    /**
     * 通过插件的apk文件创建一个ApplicationInfo对象
     * @param apkFile plugin apk file
     * @return plugin applicationInfo
     * @throws Exception
     */
    public static ApplicationInfo generateApplicationInfo(File apkFile) throws Exception{
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");

        Class<?> packageParser$PackageClass = Class.forName("android.content.pm.PackageParser$Package");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Method generateApplicationInfoMethod = packageParserClass.getDeclaredMethod("generateApplicationInfo",
                packageParser$PackageClass,
                int.class,
                packageUserStateClass);
        //获取generateApplicationInfo的参数对象
        Object packageParser = packageParserClass.newInstance();
        Method parserPackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        Object packageObj = parserPackageMethod.invoke(packageParser, apkFile, 0);

        Object defaultPackageUserState = packageUserStateClass.newInstance();

        ApplicationInfo applicationInfo = (ApplicationInfo) generateApplicationInfoMethod.invoke(packageParser,
                packageObj, 0, defaultPackageUserState);
        String apkPath = apkFile.getPath();
        applicationInfo.sourceDir = apkPath;
        applicationInfo.publicSourceDir = apkPath;
        return applicationInfo;
    }

}
