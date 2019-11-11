package com.mycompany.alarme.views.drawers;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import com.mycompany.alarme.views.CircleContextDrawer;
import com.mycompany.alarme.views.CircleLayout;
import com.mycompany.alarme.views.R;

public class HandlersRangeContextDrawer extends View implements CircleContextDrawer {

    public static final int ARC_STROKE_DEFAULT_VALUE = 20;

    private SparseArray<DrawerParams> drawerParams = new SparseArray<>();

    private RectF arcRect = new RectF();
    private int arcStrokeWidth;
    private int gradientStartDegree;
    private float gradientRange;
    private Paint rangeArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int rangeArcStartColor;
    private @ColorInt int rangeArcEndColor;
    private Paint boundsArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private @ColorInt int boundsArcColor;
    private float centerX;
    private float centerY;

    public HandlersRangeContextDrawer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HandlersRangeContextDrawer);
        arcStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.HandlersRangeContextDrawer_clock_arcWidth, ARC_STROKE_DEFAULT_VALUE);
        rangeArcStartColor = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_clock_rangeArcStartColor, Color.BLUE);
        rangeArcEndColor = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_clock_rangeArcEndColor, Color.YELLOW);
        boundsArcColor = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_clock_boundsArcColor, Color.GRAY);
        typedArray.recycle();

        rangeArcPaint.setStrokeWidth(arcStrokeWidth);
        rangeArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setStrokeWidth(arcStrokeWidth);
        boundsArcPaint.setStyle(Paint.Style.STROKE);
        boundsArcPaint.setColor(boundsArcColor);

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

    private void updateCirclePaintShader() {
        rangeArcPaint.setShader(new SweepGradient(getWidth() / 2, getHeight() / 2, new int[]{
                rangeArcStartColor, rangeArcEndColor
        }, new float[]{
                0.0f, gradientRange
        }));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int radius = Math.min(w, h) / 2;
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        // set the circle rect for gradient arc, shrink it by a half of stroke width in order to fit inside view bounds
        final float halfOfArchStrokeWidget = arcStrokeWidth / 2f;
        arcRect.set(centerX - (radius - halfOfArchStrokeWidget),
                centerY - (radius - halfOfArchStrokeWidget),
                centerX + (radius - halfOfArchStrokeWidget),
                centerY + (radius - halfOfArchStrokeWidget));


        updateCirclePaintShader();
    }

    @Override
    public void recordChildrenInfo(CircleLayout layout) {
        for (int childIndex = 0; childIndex < layout.getChildCount(); childIndex++) {
            View childAt = layout.getChildAt(childIndex);
            if (((CircleLayout.LayoutParams) childAt.getLayoutParams()).getRole().equals(CircleLayout.ViewRole.Handler)) {
                if (childAt.getId() != NO_ID) {
                    drawerParams.append(childAt.getId(), createParams(childAt));
                }
            }
        }
    }

    private DrawerParams createParams(View view) {
        final CircleLayout.LayoutParams layoutParams = (CircleLayout.LayoutParams) view.getLayoutParams();
        final DrawerParams drawerParams = new DrawerParams();
        @SuppressLint("CustomViewStyleable")
        final TypedArray typedArray = view.getContext().obtainStyledAttributes(layoutParams.drawerParamsStyleId, R.styleable.HandlersRangeContextDrawer_Params);
        drawerParams.color = typedArray.getColor(R.styleable.HandlersRangeContextDrawer_Params_handlerRangeColor, randomColor());
        typedArray.recycle();
        drawerParams.angle = layoutParams.getAngle();
        return drawerParams;
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
        final float rangeSweepAngle = 360 * gradientRange;
        canvas.rotate(gradientStartDegree, centerX, centerY);
        canvas.drawArc(arcRect, 0, rangeSweepAngle, false, rangeArcPaint);
        canvas.drawArc(arcRect, rangeSweepAngle, 360 - rangeSweepAngle, false, boundsArcPaint);
    }

    private static class DrawerParams {
        private int color;
        private double angle;
    }

}
