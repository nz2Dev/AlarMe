package com.mycompany.alarme.features;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.Objects;

import static android.view.ViewGroup.getChildMeasureSpec;

public class TimeContextDrawerBehavior extends CoordinatorLayout.Behavior<TimeContextDrawer> {

    private int parentRadius;

    public TimeContextDrawerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull TimeContextDrawer child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int minSize = Math.min(View.MeasureSpec.getSize(parentWidthMeasureSpec), View.MeasureSpec.getSize(parentHeightMeasureSpec));
        parentRadius = minSize / 2;

        int constrainedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(minSize, View.MeasureSpec.getMode(parentWidthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(minSize, View.MeasureSpec.getMode(parentHeightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        return true;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull TimeContextDrawer child, int layoutDirection) {
        int startX = parentRadius;
        int startY = parentRadius;
        child.layout(startX - child.getMeasuredWidth() / 2, startY - child.getMeasuredHeight() / 2,
                startX + child.getMeasuredWidth() / 2, startY + child.getMeasuredHeight() / 2);

        return true;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull TimeContextDrawer child, @NonNull View dependency) {
        return ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior() instanceof TimeItemCircleBehavior;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull TimeContextDrawer child, @NonNull View dependency) {
        if (dependency.getId() == View.NO_ID) {
            return false;
        }

        TimeItemCircleBehavior timeItemBehavior = Objects.requireNonNull((TimeItemCircleBehavior) ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior());
        child.updateTimeItemParams(dependency.getId(), new TimeContextDrawer.TimeItemDrawerParams(timeItemBehavior.getInitialDegree(), timeItemBehavior.getDrawerColor()));

        return false;
    }

}
