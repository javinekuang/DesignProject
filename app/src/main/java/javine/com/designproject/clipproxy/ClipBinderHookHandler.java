package javine.com.designproject.clipproxy;

import android.content.ClipData;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by KuangYu on 2016/12/28 0028.
 */
public class ClipBinderHookHandler implements InvocationHandler {

    private final String TAG = "ClipBinderHookHandler";

    Object base;

    public ClipBinderHookHandler(IBinder base, Class<?> subClass) {
        try {
            Method asInterface = subClass.getDeclaredMethod("asInterface",IBinder.class);
            //IBinder对象需要调用asInterface返回IClipboard对象，才能进行剪切板操作
            //传递进来的base是原对象，所以此处调用的asInterface方法中的queryLocalInterface方法是没有被hook的
            this.base = asInterface.invoke(null,base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getPrimaryClip".equals(method.getName())){
            Log.d(TAG, "hook getPrimaryClip");
            return ClipData.newPlainText(null, "you are hooked");
        }
        if ("hasPrimaryClip".equals(method.getName())){
            return true;
        }
        return method.invoke(base,args);
    }
}
