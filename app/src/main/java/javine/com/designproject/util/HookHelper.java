package javine.com.designproject.util;

import android.content.Context;
import android.util.Log;

import java.io.File;

import javine.com.designproject.amsproxy.AMSProxyHelper;
import javine.com.designproject.classloaderproxy.ClassLoaderProxyHelper;
import javine.com.designproject.clipproxy.ClipProxyHelper;
import javine.com.designproject.receiverproxy.HookReceiverHelper;
import javine.com.designproject.serviceproxy.HookServiceHelper;

/**
 * Created by Administrator on 2016/12/28.
 */
public class HookHelper {
    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";
    public static final String EXTRA_TARGET_SERVICE = "extra_target_SERVICE";

    public static void installHook(Context context){
        Utils.extractAsserts(context, "test.apk");
        File testPlugin = context.getFileStreamPath("test.apk");
        try {
            HookReceiverHelper.preLoadReceiver(context, testPlugin);
            HookServiceHelper.preLoadService(testPlugin);
            ClassLoaderProxyHelper.hookLoadedApkInActivityThread(testPlugin);
            ClipProxyHelper.hookClipBoardService();
            AMSProxyHelper.hookActivityManagerNative();
            AMSProxyHelper.hookActivityThread();
            Log.i(context.getClass().getSimpleName(), "hook success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
