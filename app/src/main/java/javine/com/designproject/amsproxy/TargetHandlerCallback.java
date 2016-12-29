package javine.com.designproject.amsproxy;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
