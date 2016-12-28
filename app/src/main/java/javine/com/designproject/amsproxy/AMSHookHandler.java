package javine.com.designproject.amsproxy;

import android.content.ComponentName;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javine.com.designproject.SubActivity;
import javine.com.designproject.util.HookHelper;

/**
 * Created by Administrator on 2016/12/28.
 */
public class AMSHookHandler implements InvocationHandler {

    Object base;

    public AMSHookHandler(Object base) {
        this.base = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())){

            Intent originIntent = null;
            int index = 0;

            for (int i=0;i<args.length;i++){
                if (args[i] instanceof Intent){
                    originIntent = (Intent) args[i];
                    index = i;
                    break;
                }
            }

            Intent newIntent = new Intent();

            String packageString = "javine.com.designproject";
            ComponentName componentName = new ComponentName(packageString, SubActivity.class.getCanonicalName());
            newIntent.setComponent(componentName);
            newIntent.putExtra(HookHelper.EXTRA_TARGET_INTENT,originIntent);
            args[index] = newIntent;
            return method.invoke(base, args);
        }
        return method.invoke(base,args);
    }
}
