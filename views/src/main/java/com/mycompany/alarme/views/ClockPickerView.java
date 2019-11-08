package com.mycompany.alarme.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class ClockPickerView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF circleRect = new RectF();
    private int radius;
    private int startX;
    private int startY;

    private float gradientRange;
    private int gradientStartDegree;
    private int circleStrokeSize;
    private float circleStrokePercentageOfRadius = 0.1f;
    private Paint debugPaint;

    public ClockPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.STROKE);
        circlePaint.setStyle(Paint.Style.STROKE);
        debugPaint = new Paint(paint);
        debugPaint.setColor(Color.BLACK);

        setupGradientShader(0.4f, 270);
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
        radius = Math.min(w, h) / 2;
        circleStrokeSize = ((int) (radius * circleStrokePercentageOfRadius));
        circlePaint.setStrokeWidth(circleStrokeSize);

        startX = w / 2;
        startY = h / 2;
        final float halfOfCircleStrokeSize = circleStrokeSize / 2f;
        // set the circle rect for gradient arc, shrink it by a half of stroke size (width) in order to fit inside view bounds
        circleRect.set(startX - (radius - halfOfCircleStrokeSize),
                startY - (radius - halfOfCircleStrokeSize),
                startX + (radius - halfOfCircleStrokeSize),
                startY + (radius - halfOfCircleStrokeSize));

        updateCirclePaintShader();
    }

    private void updateCirclePaintShader() {
        circlePaint.setShader(new SweepGradient(startX, startY, new int[]{
                Color.BLUE, Color.YELLOW
        }, new float[]{
                0.0f, gradientRange
        }));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int segmentsCount = 12;
        final int segmentSize = 360 / segmentsCount;
        final double lineSize = 20;
        int x = startX, y = startY;

        // debug draw measured bounds
        canvas.drawRect(startX - radius, startY - radius, startX + radius - 1, startY + radius - 1, debugPaint);

        paint.setColor(Color.BLACK);
        for (int segment = 0; segment < segmentsCount; segment++) {
            double angle = Math.toRadians(segment * segmentSize);
            double angleSin = Math.sin(angle);
            double angleCos = Math.cos(angle);

            canvas.drawLine(((float) (x + angleCos * (radius - lineSize))), ((float) (y + angleSin * (radius - lineSize))),
                    ((float) (x + angleCos * radius)), ((float) (y + angleSin * radius)), paint);
        }

        canvas.rotate(gradientStartDegree, startX, startY);
        canvas.drawArc(circleRect, 0, 360 * gradientRange, false, circlePaint);
    }

}
