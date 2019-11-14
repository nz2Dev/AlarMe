package com.mycompany.alarme.views.drawers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.mycompany.alarme.views.CircleContextDrawer;
import com.mycompany.alarme.views.CircleLayout;
import com.mycompany.alarme.views.R;

public class HandlersRangeContextDrawer extends View implements CircleContextDrawer {

    public static final int ARC_STROKE_DEFAULT_VALUE = 20;

    private SparseArray<DrawerParams> paramsArrayMap = new SparseArray<>();

    private int arcStrokeWidth;
    private RectF arcRect = new RectF();
    private Paint rangeArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint boundsArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int boundsArcColor;
    private float centerX;
    private float centerY;

    private int[] gradientColors;
    private float[] gradientPositions;

    public HandlersRangeContextDrawer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HandlersRangeContextDrawer);
        arcStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.HandlersRangeContextDrawer_clock_arcWidth, ARC_STROKE_DEFAULT_VALUE);
        boundsArcColor = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_clock_boundsArcColor, Color.GRAY);
        typedArray.recycle();

        rangeArcPaint.setStrokeWidth(arcStrokeWidth);
        rangeArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setStrokeWidth(arcStrokeWidth);
        boundsArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setColor(boundsArcColor);
    }

    private void applyParams(int viewId, Consumer<DrawerParams> paramsConsumer) {
        DrawerParams drawerParams = paramsArrayMap.get(viewId);
        if (drawerParams == null) {
            drawerParams = new DrawerParams();
            paramsArrayMap.append(viewId, drawerParams);
        }
        paramsConsumer.accept(drawerParams);
    }

    @Override
    public void onRecordLayoutParams(int viewId, @NonNull CircleLayout.LayoutParams layoutParams) {
        applyParams(viewId, drawerParams -> {
            drawerParams.degree = layoutParams.getDegree();
            // but additionally the layout params could be checked for type
            // and additional logic that rely on that could be implemented there
        });
    }

    @Override
    public void onRecordContextParams(int viewId, @NonNull Resources.Theme theme, int contextParamsStyleId) {
        applyParams(viewId, drawerParams -> {
            final TypedArray typedArray = theme.obtainStyledAttributes(contextParamsStyleId, R.styleable.HandlersRangeContextDrawer_Params);
            drawerParams.color = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_Params_handlerRangeColor, randomColor());
            typedArray.recycle();
        });
    }

    @Override
    public void onRecordingFinished() {
        gradientColors = new int[paramsArrayMap.size()];
        gradientPositions = new float[paramsArrayMap.size()];
        for (int handlerDrawerParamIndex = 0; handlerDrawerParamIndex < paramsArrayMap.size(); handlerDrawerParamIndex++) {
            DrawerParams params = paramsArrayMap.valueAt(handlerDrawerParamIndex);
            gradientColors[handlerDrawerParamIndex] = params.color;
            gradientPositions[handlerDrawerParamIndex] = params.degree / 360f;
        }

        updateRangeArcPaintShader();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int radius = Math.min(w, h) / 2;
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        final float halfOfArchStrokeWidth = arcStrokeWidth / 2f;
        arcRect.set(centerX - (radius - halfOfArchStrokeWidth),
                centerY - (radius - halfOfArchStrokeWidth),
                centerX + (radius - halfOfArchStrokeWidth),
                centerY + (radius - halfOfArchStrokeWidth));

        updateRangeArcPaintShader();
    }

    private void updateRangeArcPaintShader() {
        if (gradientColors.length >= 2) {
            rangeArcPaint.setShader(new SweepGradient(getWidth() / 2, getHeight() / 2, gradientColors, gradientPositions));
        }
    }

    private static int randomColor() {
        return Color.argb(
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (paramsArrayMap.size() <= 1) {
            // not enough handlers to draw range
            return;
        }

        final DrawerParams lastParams = paramsArrayMap.valueAt(paramsArrayMap.size() - 1);
        canvas.drawArc(arcRect, 0, (float) lastParams.degree, false, rangeArcPaint);
        canvas.drawArc(arcRect, (float) lastParams.degree, (float) (360 - lastParams.degree), false, boundsArcPaint);
    }

    private static class DrawerParams {
        private int color;
        private int degree;
    }

}
