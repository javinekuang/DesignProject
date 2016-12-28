package javine.com.designproject.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import javine.com.designproject.R;

/**
 * Created by KuangYu on 2016/12/14 0014.
 */
public class FollowBehavior extends CoordinatorLayout.Behavior {
    private int targetId;

    public FollowBehavior(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Follow);
        targetId = a.getResourceId(R.styleable.Follow_target,-1);
        a.recycle();
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        child.setY(dependency.getY() + dependency.getHeight());
        return true;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency.getId() == targetId;
    }
}
