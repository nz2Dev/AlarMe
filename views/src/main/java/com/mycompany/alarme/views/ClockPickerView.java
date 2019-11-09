package com.mycompany.alarme.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import static androidx.core.content.ContextCompat.getDrawable;

public class ClockPickerView extends View {

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
    private Drawable rangeStartDrawable;
    private @ColorInt int rangeArcEndColor;
    private Paint boundsArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int boundsArcColor;

    private int radius;
    private int startX;
    private int startY;

    public ClockPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockPickerView);
        arcStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.ClockPickerView_clock_arcWidth, ARC_STROKE_DEFAULT_VALUE);
        rangeArcStartColor = typedArray.getColor(R.styleable.ClockPickerView_clock_rangeArcStartColor, Color.BLUE);
        rangeArcEndColor = typedArray.getColor(R.styleable.ClockPickerView_clock_rangeArcEndColor, Color.YELLOW);
        boundsArcColor = typedArray.getColor(R.styleable.ClockPickerView_clock_boundsArcColor, Color.GRAY);
        segmentDivisionLineLength = typedArray.getDimensionPixelSize(R.styleable.ClockPickerView_clock_segmentsDivisionLineLength, SEGMENT_DIVISION_DEFAULT_LINE_SIZE);
        segmentDivisionOffset = typedArray.getDimensionPixelSize(R.styleable.ClockPickerView_clock_segmentsDivisionOffset, SEGMENT_DIVISION_DEFAULT_OFFSET);
        segmentDivisionLineColor = typedArray.getColor(R.styleable.ClockPickerView_clock_segmentsDivisionLineColor, Color.GRAY);
        segmentDivisionLineWidth = typedArray.getDimensionPixelSize(R.styleable.ClockPickerView_clock_segmentsDivisionLineWidth, SEGMENT_DIVISION_DEFAULT_LINE_WIDTH);
        typedArray.recycle();

        rangeArcPaint.setStyle(Paint.Style.STROKE);
        rangeStartDrawable = getDrawable(context, R.drawable.ic_android_black_24dp);

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

        rangeArcPaint.setStrokeWidth(arcStrokeWidth);
        boundsArcPaint.setStrokeWidth(arcStrokeWidth);

        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        final float halfOfArchStrokeWidget = arcStrokeWidth / 2f;
        arcRect.set(startX - (radius - halfOfArchStrokeWidget),
                startY - (radius - halfOfArchStrokeWidget),
                startX + (radius - halfOfArchStrokeWidget),
                startY + (radius - halfOfArchStrokeWidget));

        segmentDivisionEndRadius = radius - (arcStrokeWidth + segmentDivisionOffset);

        int halfHandlerSize = 50;
        double rangeStartAngle = Math.toRadians(gradientStartDegree);
        rangeStartDrawable.setBounds(
                ((int) ((startX + Math.cos(rangeStartAngle) * radius) - halfHandlerSize)),
                ((int) ((startY + Math.sin(rangeStartAngle) * radius) - halfHandlerSize)),
                ((int) ((startX + Math.cos(rangeStartAngle) * radius) + halfHandlerSize)),
                ((int) ((startY + Math.sin(rangeStartAngle) * radius) + halfHandlerSize)));

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

        canvas.rotate(-gradientStartDegree, startX, startY);
        rangeStartDrawable.draw(canvas);
    }

}
