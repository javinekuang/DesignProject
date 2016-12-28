package javine.com.designproject.amsproxy;

import java.lang.reflect.Field;

/**
 * Created by KuangYu on 2016/12/28 0028.
 */
public class AMSProxyHelper {
    public static void hookActivityManagerNative(){
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
