package com.mycompany.alarme.features;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SweepGradientView extends View {

    private final Paint gradientPaint;
    private final Matrix gradientLocalMatrix;
    private final RectF gradientRect;
    private int gradientRotation;

    private SweepGradientBuilder builderCache;

    public SweepGradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        gradientRect = new RectF();
        gradientLocalMatrix = new Matrix();

        gradientPaint = new Paint();
        gradientPaint.setStrokeWidth(40);
        gradientPaint.setStyle(Paint.Style.STROKE);

        if (isInEditMode()) {
            setGradientStartDegree(45);
            setGradient(new SweepGradientBuilder()
                    .addSection(0, Color.GREEN)
                    .addSection(90, Color.BLUE)
                    .addSection(90, Color.TRANSPARENT));
        }
    }

    public void setGradient(SweepGradientBuilder gradientSetup) {
        builderCache = gradientSetup.copy();

        if (!gradientRect.isEmpty()) {
            final SweepGradient gradient =
                    builderCache.build(gradientRect.centerX(), gradientRect.centerY());

            gradient.setLocalMatrix(gradientLocalMatrix);
            gradientPaint.setShader(gradient);
        }

        invalidate();
    }

    public void setGradientStartDegree(int degree) {
        this.gradientRotation = degree;
        if (!isInEditMode()) {
            gradientLocalMatrix.setRotate(degree, gradientRect.centerX(), gradientRect.centerY());
        } else {
            gradientLocalMatrix.setRotate(degree);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isInEditMode()) {
            updateGradientRect();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradientRect();
    }

    private void updateGradientRect() {
        final Rect outRect = new Rect();
        getLocalVisibleRect(outRect);

        final int minSide = Math.min(outRect.height(),
                outRect.width()) - (int) gradientPaint.getStrokeWidth();

        outRect.set(outRect.centerX() - minSide / 2,
                outRect.centerY() - minSide / 2,
                outRect.centerX() + minSide / 2,
                outRect.centerY() + minSide / 2);
        outRect.set(outRect.left + getPaddingLeft(),
                outRect.top + getPaddingTop(),
                outRect.right - getPaddingRight(),
                outRect.bottom - getPaddingBottom());

        gradientRect.set(outRect);
        if (!isInEditMode()) {
            gradientLocalMatrix.setRotate(gradientRotation, gradientRect.centerX(), gradientRect.centerY());
        }

        if (builderCache != null) {
            setGradient(builderCache);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(gradientRect, 0, 360, false, gradientPaint);
    }

}
