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

public class CircleSectionContributorBehaviour extends CoordinatorLayout.Behavior<View> {

    private final int drawerColor;

    private int childRadius;
    @IdRes private int dependsOnContributorChild;
    private int currentDegree;
    private int degreeDistance;

    private double startToTouchViewCenterDistance;
    private float touchedViewCenterX;
    private float touchedViewCenterY;
    private final Rect hitRect = new Rect();

    public CircleSectionContributorBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeItemCircleBehaviour_Layout);
        currentDegree = typedArray.getInteger(R.styleable.TimeItemCircleBehaviour_Layout_layout_initialDegree, 0);
        drawerColor = typedArray.getColor(R.styleable.TimeItemCircleBehaviour_Layout_layout_drawerColor, 0);
        dependsOnContributorChild = typedArray.getResourceId(R.styleable.TimeItemCircleBehaviour_Layout_layout_dependsOn, 0);
        degreeDistance = typedArray.getInt(R.styleable.TimeItemCircleBehaviour_Layout_layout_degreeDistance, -1);
        typedArray.recycle();
    }

    public int getDrawerColor() {
        return drawerColor;
    }

    public int getCurrentDegree() {
        return currentDegree;
    }

    @IdRes
    public int getDependsOnContributorChild() {
        return dependsOnContributorChild;
    }

    private boolean hasContact(View child, MotionEvent ev) {
        child.getHitRect(hitRect);
        return hitRect.contains((int) ev.getX(), (int) ev.getY());
    }

    private void touchChildView(@NonNull CoordinatorLayout parent, @NonNull View child) {
        touchedViewCenterX = child.getLeft() + child.getWidth() / 2f;
        touchedViewCenterY = child.getTop() + child.getHeight() / 2f;

        // then find distance to the center of touched view from layout center
        float startToViewCenterVectorX = touchedViewCenterX - parent.getWidth() / 2f;
        float startToViewCenterVectorY = touchedViewCenterY - parent.getHeight() / 2f;
        startToTouchViewCenterDistance = Math.sqrt(startToViewCenterVectorX * startToViewCenterVectorX + startToViewCenterVectorY * startToViewCenterVectorY);
    }

    private void translateTouchedViewBy(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
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
        child.setTranslationX((float) (shiftedViewCenterX - touchedViewCenterX));
        child.setTranslationY((float) (shiftedViewCenterY - touchedViewCenterY));

        rotateTo(parent, (int) Math.toDegrees(touchVectorAngle));
    }

    private void rotateTo(CoordinatorLayout parent, int degree) {
        currentDegree = CircleSectionUtils.to360Range(degree);

        if (dependsOnContributorChild != 0) {
            CircleSectionContributorBehaviour dependentBehaviour = getViewBehaviorAsTimeItem(parent.findViewById(dependsOnContributorChild));
            if (dependentBehaviour != null) {
                degreeDistance = dependentBehaviour.getCurrentDegree() - currentDegree;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
        // in order to prevent child view take over on touch event
        // so we would start stealing events from child if child returns true from it's onTouchEvent
        return ev.getActionMasked() == MotionEvent.ACTION_DOWN && hasContact(child, ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
        // double checking whether the child has contact because CoordinatorLayout would
        // call onTouchEvent on every behaviour after it detects that there are no behaviour that wants to intercept touch events
        // in order to find active behaviour for touch events, so it will set the first behaviour that would return true from it's onTouchEvent
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && hasContact(child, ev)) {
            child.dispatchTouchEvent(MotionEvent.obtain(ev));
            touchChildView(parent, child);
            return true;
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            translateTouchedViewBy(parent, child, ev);
            return true;
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            child.dispatchTouchEvent(MotionEvent.obtain(ev));
            return true;
        }

        return false;
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        childRadius = Math.min(View.MeasureSpec.getSize(parentWidthMeasureSpec), View.MeasureSpec.getSize(parentHeightMeasureSpec)) / 2;
        // child radius margin should decrees this radius size value

        int constrainedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(childRadius, View.MeasureSpec.getMode(parentWidthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childRadius, View.MeasureSpec.getMode(parentHeightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        return true;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        if (degreeDistance == -1) {
            rotateTo(parent, currentDegree);
        }

        int startX = childRadius;
        int startY = childRadius;

        // final int childOuterRadius = (int) Math.sqrt(child.getMeasuredWidth() * child.getMeasuredWidth() + child.getMeasuredHeight() * child.getMeasuredHeight()) / 2;
        final int childInnerRadius = Math.min(child.getMeasuredWidth(), child.getMeasuredHeight()) / 2;
        final float startToChildCenterVectorLength = childRadius - /*childOuterRadius*/ childInnerRadius;
        final double currentAngle = Math.toRadians(currentDegree);
        final int childCenterX = (int) (startX + Math.cos(currentAngle) * startToChildCenterVectorLength);
        final int childCenterY = (int) (startY + Math.sin(currentAngle) * startToChildCenterVectorLength);

        child.layout(childCenterX - child.getMeasuredWidth() / 2, childCenterY - child.getMeasuredHeight() / 2,
                childCenterX + child.getMeasuredWidth() / 2, childCenterY + child.getMeasuredHeight() / 2);

        return true;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependsOnContributorChild != 0
                && getViewBehaviorAsTimeItem(dependency) != null
                && dependency.getId() == dependsOnContributorChild;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        int startX = childRadius;
        int startY = childRadius;

        final CircleSectionContributorBehaviour dependentBehaviour = requireNonNull(getViewBehaviorAsTimeItem(dependency));
        currentDegree = CircleSectionUtils.to360Range(dependentBehaviour.getCurrentDegree() - degreeDistance);
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

    private static CircleSectionContributorBehaviour getViewBehaviorAsTimeItem(View view) {
        final CoordinatorLayout.Behavior viewBehavior = ((CoordinatorLayout.LayoutParams) view.getLayoutParams()).getBehavior();
        return viewBehavior instanceof CircleSectionContributorBehaviour ? (CircleSectionContributorBehaviour) viewBehavior : null;
    }

}
