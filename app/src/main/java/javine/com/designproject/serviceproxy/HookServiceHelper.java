package javine.com.designproject.serviceproxy;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javine.com.designproject.util.HookHelper;

/**
 * Created by KuangYu on 2017/1/6 0006.
 */
public class HookServiceHelper {

    private static Map<ComponentName, ServiceInfo> sServiceInfoMap = new HashMap<>();
    private static Map<String, Service> sServiceMap = new HashMap<>();

    public static void onStart(Intent intent, int startId){
        Intent targetIntent = intent.getParcelableExtra(HookHelper.EXTRA_TARGET_SERVICE);
        ServiceInfo serviceInfo = selectServiceInfo(targetIntent);
        if (serviceInfo != null){
            if (!sServiceMap.containsKey(serviceInfo.name)){
                try {
                    proxyCreateService(serviceInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Service service = sServiceMap.get(serviceInfo.name);
            service.onStart(targetIntent, startId);
        }
    }

    public static void preLoadService(File apkFile) throws Exception{
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        Object packageParser = packageParserClass.newInstance();

        Object packageObj = parsePackageMethod.invoke(packageParser, apkFile, PackageManager.GET_SERVICES);
        Field servicesFiled = packageObj.getClass().getDeclaredField("services");
        List services = (List) servicesFiled.get(packageObj);

        Class<?> packageParser$ServiceClass = Class.forName("android.content.pm.PackageParser$Service");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Class<?> userHandler = Class.forName("android.os.UserHandle");
        Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
        int userId = (int) getCallingUserIdMethod.invoke(null);
        Object defaultUserState = packageUserStateClass.newInstance();

        Method generateServiceInfoMethod = packageParserClass.getDeclaredMethod("generateServiceInfo",
                packageParser$ServiceClass, int.class, packageUserStateClass, int.class);

        for (Object service : services){
            ServiceInfo serviceInfo = (ServiceInfo) generateServiceInfoMethod.invoke(packageParser,service,0,defaultUserState,userId);
            sServiceInfoMap.put(new ComponentName(serviceInfo.packageName, serviceInfo.name), serviceInfo);
        }
    }

    private static ServiceInfo selectServiceInfo(Intent targetIntent){
        ComponentName targetComponent = targetIntent.getComponent();
        for (ComponentName componentName: sServiceInfoMap.keySet()){
            if (componentName.equals(targetComponent)){
                return sServiceInfoMap.get(componentName);
            }
        }
        return null;
    }

    public static void proxyCreateService(ServiceInfo info) throws Exception{
        IBinder token = new Binder();
        Class<?> createServiceDataClass = Class.forName("android.app.ActivityThread$CreateServiceData");
        Constructor<?> constructor = createServiceDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object createServiceData = constructor.newInstance();

        Field tokenField = createServiceDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(createServiceData, token);

        Field infoField = createServiceDataClass.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(createServiceData, info);

        Class<?> compatibilityClass = Class.forName("android.content.res.CompatibilityInfo");
        Field defaultCompatibilityField = compatibilityClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        Object defaultCompatibility = defaultCompatibilityField.get(null);
        Field compatInfoField = createServiceDataClass.getDeclaredField("compatInfo");
        compatInfoField.setAccessible(true);
        compatInfoField.set(createServiceData, defaultCompatibility);

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod("handleCreateService", createServiceDataClass);
        handleCreateServiceMethod.setAccessible(true);
        handleCreateServiceMethod.invoke(currentActivityThread, createServiceData);

        //从mServices中拿出新创建的service对象，转存到sServiceMap中
        Field mServicesField = activityThreadClass.getDeclaredField("mServices");
        mServicesField.setAccessible(true);
        Map mServices = (Map) mServicesField.get(currentActivityThread);
        Service service = (Service) mServices.get(token);

        // 获取到之后, 移除这个service, 我们只是借花献佛
        mServices.remove(token);
        sServiceMap.put(info.name, service);
    }

}
