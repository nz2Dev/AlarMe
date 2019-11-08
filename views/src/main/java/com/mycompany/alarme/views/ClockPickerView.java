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

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class ClockPickerView extends View {

    public static final float ARC_STROKE_DEFAULT_VALUE = 0.05f;
    public static final int SEGMENT_DIVISION_DEFAULT_LINE_SIZE = 20;
    public static final int SEGMENT_DIVISION_DEFAULT_OFFSET = 50;
    public static final int SEGMENT_DIVISION_DEFAULT_LINE_WIDTH = 5;

    private Paint debugPaint;

    private Paint segmentDivisionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double segmentDivisionLineSize; // todo convert to percentage value
    private @ColorInt int segmentDivisionLineColor;
    private int segmentsCount = 12;
    private int segmentDegree = 360 / segmentsCount;
    private int segmentDivisionOffset;
    private int segmentDivisionEndRadius;
    private int segmentDivisionLineWidth;

    private RectF arcRect = new RectF();
    private float arcStrokeValue;
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

    public ClockPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockPickerView);
        arcStrokeValue = typedArray.getFloat(R.styleable.ClockPickerView_clock_arcSize, ARC_STROKE_DEFAULT_VALUE);
        rangeArcStartColor = typedArray.getColor(R.styleable.ClockPickerView_clock_rangeArcStartColor, Color.BLUE);
        rangeArcEndColor = typedArray.getColor(R.styleable.ClockPickerView_clock_rangeArcEndColor, Color.YELLOW);
        boundsArcColor = typedArray.getColor(R.styleable.ClockPickerView_clock_boundsArcColor, Color.GRAY);
        segmentDivisionLineSize = typedArray.getDimensionPixelSize(R.styleable.ClockPickerView_clock_segmentsDivisionLineSize, SEGMENT_DIVISION_DEFAULT_LINE_SIZE);
        segmentDivisionOffset = typedArray.getInt(R.styleable.ClockPickerView_clock_segmentsDivisionOffset, SEGMENT_DIVISION_DEFAULT_OFFSET);
        segmentDivisionLineColor = typedArray.getColor(R.styleable.ClockPickerView_clock_segmentsDivisionLineColor, Color.GRAY);
        segmentDivisionLineWidth = typedArray.getInt(R.styleable.ClockPickerView_clock_segmentsDivisionLineWidth, SEGMENT_DIVISION_DEFAULT_LINE_WIDTH);
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
        setMeasuredDimension(
                resolveSizeAndState(getSuggestedMinimumWidth() /*todo support padding + getPaddingLeft() + getPaddingRight()*/, widthMeasureSpec, 0),
                resolveSizeAndState(getSuggestedMinimumHeight() /*+ getPaddingTop() + getPaddingBottom()*/, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startX = w / 2;
        startY = h / 2;
        radius = Math.min(w, h) / 2;

        final int rangeArcStrokeWidth = ((int) (radius * arcStrokeValue));
        rangeArcPaint.setStrokeWidth(rangeArcStrokeWidth);
        boundsArcPaint.setStrokeWidth(rangeArcStrokeWidth);

        final float halfOfRangeArchStrokeWidget = rangeArcStrokeWidth / 2f;
        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        arcRect.set(startX - (radius - halfOfRangeArchStrokeWidget),
                startY - (radius - halfOfRangeArchStrokeWidget),
                startX + (radius - halfOfRangeArchStrokeWidget),
                startY + (radius - halfOfRangeArchStrokeWidget));

        segmentDivisionEndRadius = radius - (rangeArcStrokeWidth + segmentDivisionOffset);

        updateCirclePaintShader();
    }

    private void updateCirclePaintShader() {
        rangeArcPaint.setShader(new SweepGradient(startX, startY, new int[]{
                rangeArcStartColor, rangeArcEndColor
        }, new float[]{
                0.0f, gradientRange
        }));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // debug draw measured bounds
        // canvas.drawRect(startX - radius, startY - radius, startX + radius - 1, startY + radius - 1, debugPaint);

        for (int segment = 0; segment < segmentsCount; segment++) {
            double angle = Math.toRadians(segment * segmentDegree);
            double angleSin = Math.sin(angle);
            double angleCos = Math.cos(angle);

            canvas.drawLine(
                    ((float) (startX + angleCos * (segmentDivisionEndRadius - segmentDivisionLineSize))),
                    ((float) (startY + angleSin * (segmentDivisionEndRadius - segmentDivisionLineSize))),
                    ((float) (startX + angleCos * segmentDivisionEndRadius)),
                    ((float) (startY + angleSin * segmentDivisionEndRadius)),
                    segmentDivisionPaint);
        }

        final float rangeSweepAngle = 360 * gradientRange;
        canvas.rotate(gradientStartDegree, startX, startY);
        canvas.drawArc(arcRect, 0, rangeSweepAngle, false, rangeArcPaint);

        canvas.drawArc(arcRect, rangeSweepAngle, 360 - rangeSweepAngle, false, boundsArcPaint);
    }

}
