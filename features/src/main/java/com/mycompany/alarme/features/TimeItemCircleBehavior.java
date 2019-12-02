package com.mycompany.alarme.features;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static android.view.ViewGroup.getChildMeasureSpec;

public class TimeItemCircleBehavior extends CoordinatorLayout.Behavior<View> {

    private int parentRadius;

    private final int initialDegree;
    private final double initialAngle;
    private int drawerColor;

    public TimeItemCircleBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeItemCircleBehaviour_Layout);
        initialDegree = typedArray.getInteger(R.styleable.TimeItemCircleBehaviour_Layout_layout_initialDegree, 0);
        drawerColor = typedArray.getColor(R.styleable.TimeItemCircleBehaviour_Layout_layout_drawerColor, 0);
        typedArray.recycle();

        initialAngle = Math.toRadians(initialDegree);
    }

    public int getDrawerColor() {
        return drawerColor;
    }

    public int getInitialDegree() {
        return initialDegree;
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        parentRadius = Math.min(View.MeasureSpec.getSize(parentWidthMeasureSpec), View.MeasureSpec.getSize(parentHeightMeasureSpec)) / 2;
        // child radius margin should decrees this radius size value

        int constrainedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentRadius, View.MeasureSpec.getMode(parentWidthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentRadius, View.MeasureSpec.getMode(parentHeightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        return true;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        int startX = parentRadius;
        int startY = parentRadius;

        // final int childOuterRadius = (int) Math.sqrt(child.getMeasuredWidth() * child.getMeasuredWidth() + child.getMeasuredHeight() * child.getMeasuredHeight()) / 2;
        final int childInnerRadius = Math.min(child.getMeasuredWidth(), child.getMeasuredHeight()) / 2;
        final float startToChildCenterVectorLength = parentRadius - /*childOuterRadius*/ childInnerRadius;
        final int childCenterX = (int) (startX + Math.cos(initialAngle) * startToChildCenterVectorLength);
        final int childCenterY = (int) (startY + Math.sin(initialAngle) * startToChildCenterVectorLength);

        child.layout(childCenterX - child.getMeasuredWidth() / 2, childCenterY - child.getMeasuredHeight() / 2,
                childCenterX + child.getMeasuredWidth() / 2, childCenterY + child.getMeasuredHeight() / 2);

        return true;
    }

}
