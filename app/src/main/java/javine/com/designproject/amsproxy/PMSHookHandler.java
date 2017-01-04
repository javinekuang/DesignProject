package javine.com.designproject.amsproxy;

import android.content.pm.PackageInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by KuangYu on 2017/1/4 0004.
 */
public class PMSHookHandler implements InvocationHandler {

    private Object mBase;

    public PMSHookHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")){
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
