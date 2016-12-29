package javine.com.designproject.amsproxy;

import android.os.Handler;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by KuangYu on 2016/12/28 0028.
 */
public class AMSProxyHelper {
    public static void hookActivityManagerNative(){
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            //获取singleton对象
            Object gDefault = gDefaultField.get(null);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object mInstance = mInstanceField.get(gDefault);

            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");

            Object proxyAm = Proxy.newProxyInstance(activityManagerNativeClass.getClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},new AMSHookHandler(mInstance));
            mInstanceField.set(gDefault,proxyAm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hookActivityThread(){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThreadObject = currentActivityThread.invoke(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(activityThreadObject);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH,new TargetHandlerCallback(mH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
