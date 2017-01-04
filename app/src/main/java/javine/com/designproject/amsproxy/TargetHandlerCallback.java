package javine.com.designproject.amsproxy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javine.com.designproject.util.HookHelper;

/**
 * Created by KuangYu on 2016/12/29 0029.
 * 拦截LAUNCH_ACTIVITY消息，将SubActivity替换为我们的TargetActivity
 * 然后，创建Activity对象
 */
public class TargetHandlerCallback implements Handler.Callback {

    private static final String TAG = "TargetHandlerCallback";
    Handler baseHandler;
    int msg_code;

    public TargetHandlerCallback(Handler baseHandler) {
        this.baseHandler = baseHandler;
        try {
            Class<?> HClass = Class.forName("android.app.ActivityThread$H");
            Field LAUNCH_ACTIVITY_FIELD = HClass.getDeclaredField("LAUNCH_ACTIVITY");
            LAUNCH_ACTIVITY_FIELD.setAccessible(true);
            msg_code = (int) LAUNCH_ACTIVITY_FIELD.get(null);
            Log.d(TAG, "reflect msg_code = "+msg_code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case 100:
                handleLaunchActivity(msg);
                break;
        }
        baseHandler.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj;
        try {
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent targetIntent = (Intent) intentField.get(obj);
            Intent originIntent = targetIntent.getParcelableExtra(HookHelper.EXTRA_TARGET_INTENT);
            targetIntent.setComponent(originIntent.getComponent());

            Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
            activityInfoField.setAccessible(true);
            ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);
            activityInfo.applicationInfo.packageName = originIntent.getPackage() == null?
                    originIntent.getComponent().getPackageName():originIntent.getPackage();
            hookPackageManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hookPackageManager() throws Exception{
        //initializeJavaContextClassLoader方法内部检查了包是否在系统安装，需要绕过这个检查
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        //获取原始的sPackageManager
        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        //生成代理对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new PMSHookHandler(sPackageManager));
        //替换对象
        sPackageManagerField.set(currentActivityThread,proxy);
    }
}
