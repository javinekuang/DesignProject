package javine.com.designproject;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kogitune.activity_transition.ActivityTransitionLauncher;

import javine.com.designproject.amsproxy.AMSProxyHelper;
import javine.com.designproject.clipproxy.ClipProxyHelper;

public class MainActivity extends Activity {
    TextView tv_first;
    View v_second;
    private float beginY;
    Toolbar toolbar;
    ImageView imageView;
    AppBarLayout appBarLayout;
    EditText editText;

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
        getWindow().setEnterTransition(new Fade().setDuration(1000));
        getWindow().setExitTransition(new Fade().setDuration(1000));
        ClipProxyHelper.hookClipBoardService();
        AMSProxyHelper.hookActivityManagerNative();
        AMSProxyHelper.hookActivityThread();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("editText",editText.getText().toString());
    }

    private void goNextActivity() {
        Intent intent = new Intent(this,TargetActivity.class);
        startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this,imageView,"image").toBundle());
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
