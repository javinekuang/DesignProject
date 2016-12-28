package javine.com.designproject.clipproxy;

import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by KuangYu on 2016/12/28 0028.
 */
public class ClipProxyHelper {

    final static String CLIPBOARD_SERVICE = "clipboard";

    public static void hookClipBoardService(){
        try {
            Class serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getDeclaredMethod("getService",String.class);
            IBinder clipBinder = (IBinder) getService.invoke(null,CLIPBOARD_SERVICE);

            IBinder proxyBinder = (IBinder) Proxy.newProxyInstance(serviceManager.getClassLoader(),new Class<?>[]{IBinder.class},new BinderHookHandler(clipBinder));

            Field sCache = serviceManager.getDeclaredField("sCache");
            sCache.setAccessible(true);
            Map<String, IBinder> cacheMap = (Map<String, IBinder>) sCache.get(null);
            cacheMap.put(CLIPBOARD_SERVICE,proxyBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
