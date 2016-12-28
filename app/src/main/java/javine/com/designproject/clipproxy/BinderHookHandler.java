package javine.com.designproject.clipproxy;

import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by KuangYu on 2016/12/28 0028.
 */
public class BinderHookHandler implements InvocationHandler{

    IBinder base;
    Class<?> stub;
    Class<?> iinterface;
    public BinderHookHandler(IBinder base) {
        this.base = base;
        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("queryLocalInterface".equals(method.getName())){
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class<?>[]{IBinder.class,iinterface, IInterface.class},new ClipBinderHookHandler(base, stub));
        }
        return method.invoke(base, args);
    }
}
