package javine.com.designproject.receiverproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by KuangYu on 2017/1/3 0003.
 */
public class HookReceiverHelper {
    private static final String TAG = "ReceiverHelper";

    public static Map<ActivityInfo, List<? extends IntentFilter>> sCache =
            new HashMap<>();

    private static List<BroadcastReceiver> sRegisteredReceiver = new ArrayList<>();

    private static void parseReceivers(File apkFile) throws Exception {
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        Object packageParser = packageParserClass.newInstance();

        Object packageObj = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_RECEIVERS);
        Field receiversFiled = packageObj.getClass().getDeclaredField("receivers");
        List receivers = (List) receiversFiled.get(packageObj);

        Class<?> packageParser$ActivityClass = Class.forName("android.content.pm.PackageParser$Activity");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Class<?> userHandler = Class.forName("android.os.UserHandle");
        Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
        int userId = (int) getCallingUserIdMethod.invoke(null);
        Object defaultUserState = packageUserStateClass.newInstance();

        Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
        Field intentsFiled = componentClass.getDeclaredField("intents");

        Method generateReceiverInfo = packageParserClass.getDeclaredMethod("generateActivityInfo",
                packageParser$ActivityClass, int.class, packageUserStateClass, int.class);

        for (Object receiver : receivers){
            ActivityInfo info = (ActivityInfo) generateReceiverInfo.invoke(packageParser, receiver, 0, defaultUserState, userId);
            List<? extends IntentFilter> filters = (List<? extends IntentFilter>) intentsFiled.get(receiver);
            sCache.put(info, filters);
        }
    }

    public static void preLoadReceiver(Context context, File apk) throws Exception {
        parseReceivers(apk);

        ClassLoader cl = null;
        for (ActivityInfo activityInfo : HookReceiverHelper.sCache.keySet()){
            Log.i(TAG, "preload receiver: "+ activityInfo.name);
            List<? extends IntentFilter> intentFilters = sCache.get(activityInfo);
            if (cl == null){
                cl = CustomClassLoader.getPluginClassLoader(apk, activityInfo.packageName);
            }

            for (IntentFilter intentFilter : intentFilters){
                BroadcastReceiver receiver = (BroadcastReceiver) cl.loadClass(activityInfo.name).newInstance();
                context.registerReceiver(receiver, intentFilter);
                sRegisteredReceiver.add(receiver);
            }
        }
    }

    public static void unregisterReceiver(Context context){
        for (BroadcastReceiver receiver : sRegisteredReceiver){
            context.unregisterReceiver(receiver);
        }
        sRegisteredReceiver.clear();
        sCache.clear();
    }
}
