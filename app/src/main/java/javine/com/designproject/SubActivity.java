package javine.com.designproject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;

import java.lang.reflect.Proxy;

/**
 * Created by KuangYu on 2016/12/21 0021.
 */
public class SubActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        getWindow().setReturnTransition(new Fade().setDuration(1000));
    }
}
