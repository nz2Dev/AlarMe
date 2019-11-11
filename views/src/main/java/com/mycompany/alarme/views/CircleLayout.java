package com.mycompany.alarme.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.math.MathUtils;

public class CircleLayout extends ViewGroup {

    public static final int SEGMENT_DIVISION_DEFAULT_LINE_SIZE = 20;
    public static final int SEGMENT_DIVISION_DEFAULT_OFFSET = 50;
    public static final int SEGMENT_DIVISION_DEFAULT_LINE_WIDTH = 5;

    private Paint debugPaint;

    private Paint segmentDivisionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double segmentDivisionLineLength;
    private @ColorInt int segmentDivisionLineColor;
    private int segmentsCount = 12;
    private int segmentDegree = 360 / segmentsCount;
    private int segmentDivisionOffset;
    private int segmentDivisionEndRadius;
    private int segmentDivisionLineWidth;

    private int radius;
    private int startX;
    private int startY;

    public CircleLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout);
        segmentDivisionLineLength = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionLineLength, SEGMENT_DIVISION_DEFAULT_LINE_SIZE);
        segmentDivisionOffset = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionOffset, SEGMENT_DIVISION_DEFAULT_OFFSET);
        segmentDivisionLineColor = typedArray.getColor(R.styleable.CircleLayout_clock_segmentsDivisionLineColor, Color.GRAY);
        segmentDivisionLineWidth = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionLineWidth, SEGMENT_DIVISION_DEFAULT_LINE_WIDTH);
        typedArray.recycle();

        segmentDivisionPaint.setStyle(Paint.Style.STROKE);
        segmentDivisionPaint.setColor(segmentDivisionLineColor);
        segmentDivisionPaint.setStrokeWidth(segmentDivisionLineWidth);

        debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setColor(Color.BLACK);

        setWillNotDraw(false);

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
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            final View childAt = getChildAt(childIndex);
            if (getChildRole(childAt).equals(ViewRole.ContextDrawer)) {
                // check inside onViewAdded ensures that view with ContextDrawer role has proper type
                ((CircleContextDrawer) childAt).recordChildrenInfo(this);
            }
        }
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

    private void measureChildByRadiusStrategy(View child, int widthMeasureSpec, int heightMeasureSpec) {
        int radiusSize = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)) / 2;
        // child offset should decrees this radius size value

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

        segmentDivisionEndRadius = radius - segmentDivisionOffset;
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
        final float childToCenterVectorLength = radius - /*childOuterRadius*/ childInnerRadius;
        final int childCenterX = (int) (startX + Math.cos(childParams.angle) * childToCenterVectorLength);
        final int childCenterY = (int) (startY + Math.sin(childParams.angle) * childToCenterVectorLength);

        child.layout(childCenterX - child.getMeasuredWidth() / 2, childCenterY - child.getMeasuredHeight() / 2,
                childCenterX + child.getMeasuredWidth() / 2, childCenterY + child.getMeasuredHeight() / 2);
    }

    private void layoutChildByBoxStrategy(View child) {
        child.layout(startX - child.getMeasuredWidth() / 2, startY - child.getMeasuredHeight() / 2,
                startX + child.getMeasuredWidth() / 2, startY + child.getMeasuredHeight() / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // debug draw measured bounds
        // canvas.drawRect(startX - radius, startY - radius, startX + radius - 1, startY + radius - 1, debugPaint);

        canvas.save();
        for (int segment = 0; segment < segmentsCount; segment++) {
            double angle = Math.toRadians(segment * segmentDegree);
            double angleSin = Math.sin(angle);
            double angleCos = Math.cos(angle);

            canvas.drawLine(
                    ((float) (startX + angleCos * (segmentDivisionEndRadius - segmentDivisionLineLength))),
                    ((float) (startY + angleSin * (segmentDivisionEndRadius - segmentDivisionLineLength))),
                    ((float) (startX + angleCos * segmentDivisionEndRadius)),
                    ((float) (startY + angleSin * segmentDivisionEndRadius)),
                    segmentDivisionPaint);
        }
        canvas.restore();
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

    private ViewRole getChildRole(View child) {
        return ((LayoutParams) child.getLayoutParams()).role;
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
        public int drawerParamsStyleId;

        private LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray typedArray = c.obtainStyledAttributes(attrs, R.styleable.CircleLayout_Layout);
            degree = typedArray.getInteger(R.styleable.CircleLayout_Layout_layout_degree, 0);
            role = ViewRole.values()[typedArray.getInt(R.styleable.CircleLayout_Layout_layout_role, ViewRole.Handler.ordinal())];
            drawerParamsStyleId = typedArray.getResourceId(R.styleable.CircleLayout_Layout_layout_handlerContextParams, 0);
            typedArray.recycle();

            angle = Math.toRadians(degree);
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

        private LayoutParams(int width, int height) {
            super(width, height);
        }

        private LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}
