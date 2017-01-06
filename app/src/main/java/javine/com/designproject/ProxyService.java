package javine.com.designproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import javine.com.designproject.serviceproxy.HookServiceHelper;
import javine.com.designproject.util.HookHelper;

/**
 * Created by KuangYu on 2017/1/6 0006.
 */
public class ProxyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Javine", "ProxyService is started by command!");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        HookServiceHelper.onStart(intent, startId);
        Log.d("Javine", "ProxyService is started!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Javine", "ProxyService is created!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
