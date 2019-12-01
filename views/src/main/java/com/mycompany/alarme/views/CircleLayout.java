package com.mycompany.alarme.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.util.Consumer;

import com.mycompany.alarme.views.interactions.RotationObjectInteraction;

public class CircleLayout extends ViewGroup {

    private final ViewConfiguration viewConfiguration;

    // todo implement injection
    private CircleObjectInteraction interaction = new RotationObjectInteraction();
    private ViewUpdatesControllerImpl viewUpdatesControllerImpl;
    private HostViewContext hostViewContext;
    private View contactView;

    private int radius;
    private int startX;
    private int startY;

    public CircleLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewConfiguration = ViewConfiguration.get(context);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (getChildRole(child).equals(ViewRole.ContextDrawer)) {
            if (!(child instanceof CircleContextDrawer)) {
                throw new RuntimeException(String.format("View with role %s should implement %s interface", ViewRole.ContextDrawer, CircleContextDrawer.class));
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // check inside onViewAdded ensures that view has proper type
        forEachChildWithRole(ViewRole.ContextDrawer, child -> {
            final CircleContextDrawer contextDrawer = (CircleContextDrawer) child;

            forEachChildWithRole(ViewRole.Handler, childToRecord -> {
                final LayoutParams childParams = getChildParams(childToRecord);
                contextDrawer.onRecordLayoutParams(childToRecord.getId(), childParams);
                contextDrawer.onRecordContextParams(childToRecord.getId(), childToRecord.getContext().getTheme(), childParams.drawerParamsStyleId);
                contextDrawer.onRecordViewParams(childToRecord.getId(), childToRecord.getTranslationX(), childToRecord.getTranslationY(), childToRecord.getRotation());
            });

            contextDrawer.onRecordingFinished();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            final View child = getChildAt(childIndex);
            switch (getChildRole(child)) {
                case Handler:
                    measureChildByRadiusStrategy(child, widthMeasureSpec, heightMeasureSpec);
                    break;
                case ContextDrawer:
                case Layer:
                    measureChildByBoxStrategy(child, widthMeasureSpec, heightMeasureSpec);
                    break;
            }
        }

        // can also take into account and use max(getSuggestedMinWidth(), getMaxMeasuredChildSize() * 2)
        setMeasuredDimension(resolveSize(getMaxMeasuredChildSize(ViewRole.Handler) * 2, widthMeasureSpec),
                resolveSize(getMaxMeasuredChildSize(ViewRole.Handler) * 2, heightMeasureSpec));
    }

    private void measureChildByBoxStrategy(View child, int widthMeasureSpec, int heightMeasureSpec) {
        int minSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

        int constrainedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.getMode(widthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.getMode(heightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    // we constraint child to be not bigger that radius on horizontal and vertical dimensions
    private void measureChildByRadiusStrategy(View child, int widthMeasureSpec, int heightMeasureSpec) {
        int radiusSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)) / 2;
        // child radius margin should decrees this radius size value

        int constrainedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(radiusSize, MeasureSpec.getMode(widthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(radiusSize, MeasureSpec.getMode(heightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @SuppressWarnings("SameParameterValue")
    private int getMaxMeasuredChildSize(ViewRole viewRole) {
        int maxSize = 0;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View childAt = getChildAt(childIndex);
            final CircleLayout.LayoutParams childParams = (LayoutParams) childAt.getLayoutParams();
            if (childParams.role.equals(viewRole)) {
                maxSize = Math.max(maxSize, Math.max(childAt.getMeasuredWidth(), childAt.getMeasuredHeight()));
            }
        }
        return maxSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startX = w / 2;
        startY = h / 2;
        radius = Math.min(w, h) / 2;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            final View child = getChildAt(childIndex);
            switch (getChildRole(child)) {
                case Handler:
                    layoutChildByRadiusStrategy(child);
                    break;
                case ContextDrawer:
                case Layer:
                    layoutChildByBoxStrategy(child);
                    break;
            }
        }
    }

    private void layoutChildByRadiusStrategy(View child) {
        final LayoutParams childParams = (LayoutParams) child.getLayoutParams();
        // final int childOuterRadius = (int) Math.sqrt(child.getMeasuredWidth() * child.getMeasuredWidth() + child.getMeasuredHeight() * child.getMeasuredHeight()) / 2;
        final int childInnerRadius = Math.min(child.getMeasuredWidth(), child.getMeasuredHeight()) / 2;
        final float startToChildCenterVectorLength = radius - /*childOuterRadius*/ childInnerRadius;
        final int childCenterX = (int) (startX + Math.cos(childParams.angle) * startToChildCenterVectorLength);
        final int childCenterY = (int) (startY + Math.sin(childParams.angle) * startToChildCenterVectorLength);

        child.layout(childCenterX - child.getMeasuredWidth() / 2, childCenterY - child.getMeasuredHeight() / 2,
                childCenterX + child.getMeasuredWidth() / 2, childCenterY + child.getMeasuredHeight() / 2);
    }

    private void layoutChildByBoxStrategy(View child) {
        child.layout(startX - child.getMeasuredWidth() / 2, startY - child.getMeasuredHeight() / 2,
                startX + child.getMeasuredWidth() / 2, startY + child.getMeasuredHeight() / 2);
    }

//    @Override
//    todo add support for intercepting
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            // if the view is clickable and receive going to receive an onTouchEvent,
//            // before it does that, only then this method will be called, right?
//            // if so, we than should check if we start moving, and still the event from the view
//            // because user probably wont click on that event
//            downX = ev.getX();
//            downY = ev.getY();
//
//            forEachChildWithRole(ViewRole.Handler, view -> {
//                view.getHitRect(hitRectBuffer);
//                if (hitRectBuffer.contains((int) downX, (int) downY)) {
//                    if (contactView != null) {
//                        Log.d(getClass().getSimpleName(), String.format("onInterceptTouchEvent: touched view overwritten, was %s, now %s", contactView, view));
//                    }
//
//                    contactView = view;
//                }
//            });
//
//            return false;
//        }
//
//        // and if we didn't clicked on proper view with ViewRole.Handler, we wouldn't even try to still events
//        // and otherwise, we will try so
//        if (contactView != null && ev.getAction() == MotionEvent.ACTION_MOVE) {
//            float dx = ev.getX() - downX;
//            float dy = ev.getY() - downY;
//
//            if (dx > viewConfiguration.getScaledTouchSlop() || dy > viewConfiguration.getScaledTouchSlop()) {
//                // we started the transformation, so let's intercept all the rest events
//                return true;
//            } else {
//                // we still not in scrolling state, maybe user only want to click on item
//                return false;
//            }
//        } else {
//            // no child that need transformation were touched, or we not in move action
//            return false;
//        }
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ViewParamsProviderImpl paramsProvider = new ViewParamsProviderImpl();
            hostViewContext = new HostViewContext();
            hostViewContext.set(getLeft(), getTop(), getWidth(), getHeight());

            forEachChildWithRole(ViewRole.Handler, view -> {
                paramsProvider.targetView = view;
                if (interaction.hasContact(view.getId(), hostViewContext, paramsProvider, event)) {
                    if (contactView != null) {
                        Log.d(getClass().getSimpleName(), String.format("onInterceptTouchEvent: contact view overwritten, was %s, now %s", contactView, view));
                    }
                    contactView = view;
                }
            });

            if (contactView != null) {
                viewUpdatesControllerImpl = new ViewUpdatesControllerImpl(contactView);
            }

            // if we has contact with something, then we want to receive a move event
            return contactView != null;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // so we can assume that the view is a ViewRole.Handler
            // because it's checked in on intercept method
            if (contactView == null) {
                // we started to intercept touch event, so we should have touched view according to onInterceptTouchEvent
                throw new RuntimeException("Should exist");
            }

            interaction.updateContact(contactView.getId(), hostViewContext, viewUpdatesControllerImpl, event);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            interaction.endContact(contactView.getId(), hostViewContext, event);
            contactView = null;
            hostViewContext = null;
            viewUpdatesControllerImpl = null;
            return true;
        }

        return true;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CircleLayout.LayoutParams;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LayoutParams getChildParams(View child) {
        return ((LayoutParams) child.getLayoutParams());
    }

    private ViewRole getChildRole(View child) {
        return ((LayoutParams) child.getLayoutParams()).role;
    }

    private void forEachChildWithRole(ViewRole viewRole, Consumer<View> viewWithRoleConsumer) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (getChildRole(child).equals(viewRole)) {
                viewWithRoleConsumer.accept(child);
            }
        }
    }

    public enum ViewRole {
        Layer,
        Handler,
        ContextDrawer
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        private int degree;
        private double angle;
        private ViewRole role;

        /**
         * Specify the id to the style that contains {@link CircleContextDrawer}
         * attributes items. Only applicable for views with {@link ViewRole#Handler} role
         */
        @StyleRes
        private final int drawerParamsStyleId;

        private LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray typedArray = c.obtainStyledAttributes(attrs, R.styleable.CircleLayout_Layout);
            degree = typedArray.getInteger(R.styleable.CircleLayout_Layout_layout_handlerDegree, 0);
            role = ViewRole.values()[typedArray.getInt(R.styleable.CircleLayout_Layout_layout_role, ViewRole.Handler.ordinal())];
            drawerParamsStyleId = typedArray.getResourceId(R.styleable.CircleLayout_Layout_layout_handlerContextParams, 0);
            typedArray.recycle();

            angle = Math.toRadians(degree);
        }

        private LayoutParams(int width, int height) {
            super(width, height);
            drawerParamsStyleId = 0;
        }

        private LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            drawerParamsStyleId = 0;
        }

        public int getDegree() {
            return degree;
        }

        public double getAngle() {
            return angle;
        }

        public ViewRole getRole() {
            return role;
        }
    }

}
