package javine.com.designproject;

import android.app.Application;
import android.content.Context;

import javine.com.designproject.util.HookHelper;

/**
 * Created by KuangYu on 2017/1/3 0003.
 */
public class UPFApplication extends Application {

    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
        HookHelper.installHook(base);
    }

    public static Context getContext(){
        return context;
    }
}
