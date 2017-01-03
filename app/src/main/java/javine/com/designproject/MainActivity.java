package javine.com.designproject;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kogitune.activity_transition.ActivityTransitionLauncher;

import java.io.File;

import javine.com.designproject.amsproxy.AMSProxyHelper;
import javine.com.designproject.classloaderproxy.ClassLoaderProxyHelper;
import javine.com.designproject.clipproxy.ClipProxyHelper;
import javine.com.designproject.receiverproxy.HookReceiverHelper;
import javine.com.designproject.util.Utils;

public class MainActivity extends Activity {

    static final String ACTION = "com.weishu.upf.demo.app2.PLUGIN_ACTION";

    TextView tv_first;
    View v_second;
    private float beginY;
    Toolbar toolbar;
    ImageView imageView;
    AppBarLayout appBarLayout;
    EditText editText;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_first = (TextView) findViewById(R.id.first);
        imageView = (ImageView) findViewById(R.id.titleImage);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNextActivity();
            }
        });
        editText = (EditText) findViewById(R.id.edit_text);
        if (savedInstanceState != null){
            String editString = savedInstanceState.getString("editText");
            editText.setText(editString);
        }
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPluginBroadcast();
            }
        });
        getWindow().setEnterTransition(new Fade().setDuration(1000));
        getWindow().setExitTransition(new Fade().setDuration(1000));
        ClipProxyHelper.hookClipBoardService();
        AMSProxyHelper.hookActivityManagerNative();
        AMSProxyHelper.hookActivityThread();

        registerReceiver(mReceiver, new IntentFilter(ACTION));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ex_str = intent.getStringExtra("hook");
            if(ex_str == null){
                ex_str = "插件插件，我是主程序，握手完成";
            }
            Toast.makeText(context, ex_str, Toast.LENGTH_SHORT).show();
        }
    };

    private void sendToPluginBroadcast() {
        Toast.makeText(getApplicationContext(), "插件插件，收到请回答！", Toast.LENGTH_SHORT).show();
        sendBroadcast(new Intent("javine.com.pluginproject.REQUEST"));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Utils.extractAsserts(this, "test.apk");
        File testPlugin = getFileStreamPath("test.apk");
        try {
            HookReceiverHelper.preLoadReceiver(this, testPlugin);
            ClassLoaderProxyHelper.hookLoadedApkInActivityThread(testPlugin);
            Log.i(getClass().getSimpleName(), "hook success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("editText",editText.getText().toString());
    }

    private void goNextActivity() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("javine.com.pluginproject","javine.com.pluginproject.MainActivity"));
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                beginY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("Javine","move");
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }
}
