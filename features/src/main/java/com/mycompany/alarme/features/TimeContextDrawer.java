package com.mycompany.alarme.features;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class TimeContextDrawer extends View {

    public static final int ARC_STROKE_DEFAULT_VALUE = 20;

    private int arcStrokeWidth = ARC_STROKE_DEFAULT_VALUE;
    private RectF arcRect = new RectF();
    private Paint rangeArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint boundsArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int boundsArcColor = Color.GREEN;

    private SparseArray<TimeItemDrawerParams> paramsArray = new SparseArray<>();
    private int[] gradientColors;
    private float[] gradientPositions;
    private TimeItemDrawerParams lastParams;

    public TimeContextDrawer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        rangeArcPaint.setStrokeWidth(arcStrokeWidth);
        rangeArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setStrokeWidth(arcStrokeWidth);
        boundsArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setColor(boundsArcColor);
    }

    private void updateGradientShader() {
        if (gradientColors == null || gradientColors.length <= 1) {
            return;
        }

        rangeArcPaint.setShader(new SweepGradient(getWidth() / 2, getHeight() / 2, gradientColors, gradientPositions));
    }

    private void updateGradientState() {
        if (paramsArray.size() < 0) {
            return;
        }

        gradientColors = new int[paramsArray.size()];
        gradientPositions = new float[paramsArray.size()];
        lastParams = paramsArray.valueAt(0);

        for (int paramIndex = 0; paramIndex < paramsArray.size(); paramIndex++) {
            TimeItemDrawerParams params = paramsArray.valueAt(paramIndex);
            gradientColors[paramIndex] = params.color;
            gradientPositions[paramIndex] = (float) (params.angle / Math.PI);

            if (params.degree > lastParams.degree) {
                lastParams = params;
            }
        }
    }

    void updateTimeItemParams(int viewId, TimeItemDrawerParams params) {
        paramsArray.append(viewId, params);
        updateGradientState();
        updateGradientShader();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int radius = Math.min(w, h) / 2;
        final float centerX = getWidth() / 2;
        final float centerY = getHeight() / 2;

        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        final float halfOfArchStrokeWidth = arcStrokeWidth / 2f;
        arcRect.set(centerX - (radius - halfOfArchStrokeWidth),
                centerY - (radius - halfOfArchStrokeWidth),
                centerX + (radius - halfOfArchStrokeWidth),
                centerY + (radius - halfOfArchStrokeWidth));

        updateGradientShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lastParams == null) {
            // not enough handlers to draw range
            return;
        }

        canvas.drawArc(arcRect, 0, (float) lastParams.degree, false, rangeArcPaint);
        canvas.drawArc(arcRect, (float) lastParams.degree, (float) (360 - lastParams.degree), false, boundsArcPaint);
    }

    public static class TimeItemDrawerParams {
        private double angle;
        private int degree;
        private int color;

        public TimeItemDrawerParams(int degree, int color) {
            this.angle = Math.toRadians(degree);
            this.degree = degree;
            this.color = color;
        }
    }

}
