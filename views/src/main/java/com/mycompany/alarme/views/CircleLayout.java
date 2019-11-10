package com.mycompany.alarme.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class CircleLayout extends ViewGroup {

    public static final int ARC_STROKE_DEFAULT_VALUE = 20;
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

    private RectF arcRect = new RectF();
    private int arcStrokeWidth;
    private int gradientStartDegree;
    private float gradientRange;
    private Paint rangeArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int rangeArcStartColor;
    private @ColorInt int rangeArcEndColor;
    private Paint boundsArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int boundsArcColor;

    private int radius;
    private int startX;
    private int startY;

    public CircleLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout);
        arcStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_arcWidth, ARC_STROKE_DEFAULT_VALUE);
        rangeArcStartColor = typedArray.getColor(R.styleable.CircleLayout_clock_rangeArcStartColor, Color.BLUE);
        rangeArcEndColor = typedArray.getColor(R.styleable.CircleLayout_clock_rangeArcEndColor, Color.YELLOW);
        boundsArcColor = typedArray.getColor(R.styleable.CircleLayout_clock_boundsArcColor, Color.GRAY);
        segmentDivisionLineLength = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionLineLength, SEGMENT_DIVISION_DEFAULT_LINE_SIZE);
        segmentDivisionOffset = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionOffset, SEGMENT_DIVISION_DEFAULT_OFFSET);
        segmentDivisionLineColor = typedArray.getColor(R.styleable.CircleLayout_clock_segmentsDivisionLineColor, Color.GRAY);
        segmentDivisionLineWidth = typedArray.getDimensionPixelSize(R.styleable.CircleLayout_clock_segmentsDivisionLineWidth, SEGMENT_DIVISION_DEFAULT_LINE_WIDTH);
        typedArray.recycle();

        rangeArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setColor(boundsArcColor);

        segmentDivisionPaint.setStyle(Paint.Style.STROKE);
        segmentDivisionPaint.setColor(segmentDivisionLineColor);
        segmentDivisionPaint.setStrokeWidth(segmentDivisionLineWidth);

        debugPaint = new Paint();
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setColor(Color.BLACK);

        setWillNotDraw(false);
        updateCirclePaintShader();
        if (isInEditMode()) {
            setupGradientShader(0.4f, 270);
        }
    }

    public void setupGradientShader(@FloatRange(from = 0f, to = 1f) float range, @IntRange(from = 0, to = 360) int gradientStart) {
        gradientRange = MathUtils.clamp(range, 0, 1);
        gradientStartDegree = gradientStart;
        updateCirclePaintShader();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // tries to apply some measuring logic to specific child, taking into account only our measure specs
        // uses getChildMeasureSpec() to get measure spec for specific view
        measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec); // method, calls child.measure()
        // iterate over all views, and apply above method for each of them, ignoring views that has been gone.
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        // tries to apply some measuring logic to specific child,
        // taking into account our measure specs and width/height that the child can fit into,
        // uses getChildMeasureSpec() to get measure spec for specific view
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUser);
        // all methods above is a predefined default logic that we should write ourselves.


        // tries to find out measureSpec for child using our measureSpec and its layoutParams
        // and take into account already used space (padding, previous calculation)
        getChildMeasureSpec(spec, padding, childDimension); // :int, MeasureSpec integer for the child to pass into child.measure()
        // so I would name params: spec, allocatedSize, sizeDeclaredInLayoutParamsOrWantedSize.
        // This method will be used after calculation of childDimension our from layoutParams and allocatedSize.

        // util method for final calculations of our size
        // tries to fit size passed to the params, will get bigger whenever possible
        getDefaultSize(size, measureSpec); // :int, Size converted if needed by imposed measure spec constrain
        // can be used if needed

        // util for combining two integers bits (Mode and Size)
        MeasureSpec.makeMeasureSpec(size, mode); // :int, MeasureSpec
        // can be used if needed

        // tries to resolve our final size using calculated value and parent constraints,
        // child measure state should represent merged state of all children if any of them has such.
        resolveSizeAndState(size, measureSpec, childMeasuredState); // :int, resolved Size & State according to parent constraints with optional State bits.
        resolveSize(size, measureSpec); // :int, same as above, but without State information
        // should be used for setMeasureDimension on ourselves.
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startX = w / 2;
        startY = h / 2;
        radius = Math.min(w, h) / 2;

        rangeArcPaint.setStrokeWidth(arcStrokeWidth);
        boundsArcPaint.setStrokeWidth(arcStrokeWidth);

        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        final float halfOfArchStrokeWidget = arcStrokeWidth / 2f;
        arcRect.set(startX - (radius - halfOfArchStrokeWidget),
                startY - (radius - halfOfArchStrokeWidget),
                startX + (radius - halfOfArchStrokeWidget),
                startY + (radius - halfOfArchStrokeWidget));

        segmentDivisionEndRadius = radius - (arcStrokeWidth + segmentDivisionOffset);

        updateCirclePaintShader();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // todo implement.
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // debug draw measured bounds
        // canvas.drawRect(startX - radius, startY - radius, startX + radius - 1, startY + radius - 1, debugPaint);

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

        final float rangeSweepAngle = 360 * gradientRange;
        canvas.rotate(gradientStartDegree, startX, startY);
        canvas.drawArc(arcRect, 0, rangeSweepAngle, false, rangeArcPaint);

        canvas.drawArc(arcRect, rangeSweepAngle, 360 - rangeSweepAngle, false, boundsArcPaint);
    }

    private void updateCirclePaintShader() {
        rangeArcPaint.setShader(new SweepGradient(startX, startY, new int[]{
                rangeArcStartColor, rangeArcEndColor
        }, new float[]{
                0.0f, gradientRange
        }));
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
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        } else {
            return new LayoutParams(p);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
    }

    public static class LayoutParams extends MarginLayoutParams {

        private int degree;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray typedArray = c.obtainStyledAttributes(attrs, R.styleable.CircleLayout_Layout);
            degree = typedArray.getInteger(R.styleable.CircleLayout_Layout_layout_degree, 0);
            typedArray.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

}
