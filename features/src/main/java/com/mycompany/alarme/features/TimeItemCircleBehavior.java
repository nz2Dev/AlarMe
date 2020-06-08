package com.mycompany.alarme.features;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import static android.view.ViewGroup.getChildMeasureSpec;
import static java.util.Objects.requireNonNull;

public class TimeItemCircleBehavior extends CoordinatorLayout.Behavior<View> {

    private int parentRadius;

    private final int initialDegree;
    private final double initialAngle;
    private final int drawerColor;

    @IdRes private int dependsOnTimeItemViewId;
    private int currentDegree;
    private int degreeDistance;

    private double startToTouchViewCenterDistance;
    private float touchedViewCenterX;
    private float touchedViewCenterY;

    public TimeItemCircleBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeItemCircleBehaviour_Layout);
        initialDegree = typedArray.getInteger(R.styleable.TimeItemCircleBehaviour_Layout_layout_initialDegree, 0);
        drawerColor = typedArray.getColor(R.styleable.TimeItemCircleBehaviour_Layout_layout_drawerColor, 0);
        dependsOnTimeItemViewId = typedArray.getResourceId(R.styleable.TimeItemCircleBehaviour_Layout_layout_dependsOn, 0);
        degreeDistance = typedArray.getInt(R.styleable.TimeItemCircleBehaviour_Layout_layout_degreeDistance, 10);
        typedArray.recycle();

        initialAngle = Math.toRadians(initialDegree);
        currentDegree = initialDegree;
    }

    public int getDrawerColor() {
        return drawerColor;
    }

    public int getInitialDegree() {
        return initialDegree;
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    final Rect hitRect = new Rect();

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
        child.getHitRect(hitRect);
        final boolean contact = hitRect.contains((int) ev.getX(), (int) ev.getY());
        final boolean touch = ev.getAction() == MotionEvent.ACTION_DOWN;

        if (touch && contact) {
            touchedViewCenterX = child.getLeft() + child.getWidth() / 2f;
            touchedViewCenterY = child.getTop() + child.getHeight() / 2f;

            // then find distance to the center of touched view from layout center
            float startToViewCenterVectorX = touchedViewCenterX - parent.getWidth() / 2f;
            float startToViewCenterVectorY = touchedViewCenterY - parent.getHeight() / 2f;
            startToTouchViewCenterDistance = Math.sqrt(startToViewCenterVectorX * startToViewCenterVectorX + startToViewCenterVectorY * startToViewCenterVectorY);
        }

        return touch && contact;
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            final float touchX = ev.getX();
            final float touchY = ev.getY();

            // now we should calculate what degree current touch point is facing
            // todo then get the initial degree, and interpolate from there to current by some delta time
            float touchVectorX = touchX - parent.getWidth() / 2f;
            float touchVectorY = touchY - parent.getHeight() / 2f;
            double touchVectorAngle = Math.atan2(touchVectorY, touchVectorX);

            // multiply that distance by final degree angle and add layout center coordinate
            final double shiftedViewCenterX = Math.cos(touchVectorAngle) * startToTouchViewCenterDistance + (parent.getWidth() / 2f);
            final double shiftedViewCenterY = Math.sin(touchVectorAngle) * startToTouchViewCenterDistance + (parent.getHeight() / 2f);

            // shifted x and y subtract from touched view original x and y, those will be view new translation
            currentDegree = (int) Math.toDegrees(touchVectorAngle);
            child.setTranslationX((float) (shiftedViewCenterX - touchedViewCenterX));
            child.setTranslationY((float) (shiftedViewCenterY - touchedViewCenterY));
        }
        return true;
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

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependsOnTimeItemViewId != 0
                && getViewBehaviorAsTimeItem(dependency) != null
                && dependency.getId() == dependsOnTimeItemViewId;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        int startX = parentRadius;
        int startY = parentRadius;

        TimeItemCircleBehavior dependentBehaviour = requireNonNull(getViewBehaviorAsTimeItem(dependency));
        currentDegree = dependentBehaviour.getCurrentDegree() - degreeDistance;
        final double newAngle = Math.toRadians(currentDegree);
        final int dependenciesCenterX = dependency.getLeft() + dependency.getWidth() / 2;
        final int dependenciesCenterY = dependency.getTop() + dependency.getHeight() / 2;
        final double lengthToDependenciesCenter = Math.sqrt((dependenciesCenterX - startX) * (dependenciesCenterX - startX) + (dependenciesCenterY - startY) * (dependenciesCenterY - startY));

        final double newX = Math.cos(newAngle) * lengthToDependenciesCenter + startX;
        final double newY = Math.sin(newAngle) * lengthToDependenciesCenter + startY;
        final int childCenterX = child.getLeft() + child.getWidth() / 2;
        final int childCenterY = child.getTop() + child.getHeight() / 2;
        child.setTranslationX((float) (newX - childCenterX));
        child.setTranslationY((float) (newY - childCenterY));

        return true;
    }

    private static TimeItemCircleBehavior getViewBehaviorAsTimeItem(View view) {
        final CoordinatorLayout.Behavior viewBehavior = ((CoordinatorLayout.LayoutParams) view.getLayoutParams()).getBehavior();
        return viewBehavior instanceof TimeItemCircleBehavior ? (TimeItemCircleBehavior) viewBehavior : null;
    }

}
