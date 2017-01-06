package javine.com.designproject.amsproxy;

import android.content.ComponentName;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javine.com.designproject.ProxyService;
import javine.com.designproject.SubActivity;
import javine.com.designproject.UPFApplication;
import javine.com.designproject.util.HookHelper;

/**
 * Created by Administrator on 2016/12/28.
 */
public class AMSHookHandler implements InvocationHandler {

    Object base;
    final String hostPackageName = UPFApplication.getContext().getPackageName();

    public AMSHookHandler(Object base) {
        this.base = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())){ //hook activity

            int index = getIntentIndex(args);
            Intent originIntent = (Intent) args[index];
            Intent newIntent = new Intent();

            String packageString = hostPackageName;
            ComponentName componentName = new ComponentName(packageString, SubActivity.class.getCanonicalName());
            newIntent.setComponent(componentName);
            newIntent.putExtra(HookHelper.EXTRA_TARGET_INTENT,originIntent);
            args[index] = newIntent;
            return method.invoke(base, args);
        }else if ("registerReceiver".equals(method.getName())){//hook receiver
            String packageName = (String) args[1];
            String hostPackageString = "javine.com.designproject";;
            if (!hostPackageString.equals(packageName)){
                args[1] = hostPackageString;
            }
        }else if ("startService".equals(method.getName())){ //hook service
            int index = getIntentIndex(args);
            Intent targetIntent = (Intent) args[index];
            String targetPackageName = targetIntent.getComponent().getPackageName();
            if (!hostPackageName.equals(targetPackageName)){
                Intent fakeIntent = new Intent();

                ComponentName fakeComponent = new ComponentName(hostPackageName, ProxyService.class.getCanonicalName());
                fakeIntent.setComponent(fakeComponent);
                fakeIntent.putExtra(HookHelper.EXTRA_TARGET_SERVICE, targetIntent);
                args[index] = fakeIntent;
                return method.invoke(base, args);
            }
        }
        return method.invoke(base,args);
    }

    private int getIntentIndex(Object[] args) {
        int index = 0;
        for (int i=0;i<args.length;i++){
            if (args[i] instanceof Intent){
                index = i;
                break;
            }
        }
        return index;
    }
}
