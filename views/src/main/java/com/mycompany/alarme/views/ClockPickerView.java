package com.mycompany.alarme.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class ClockPickerView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int radius;
    private int startX;
    private int startY;

    public ClockPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                resolveSizeAndState(getSuggestedMinimumWidth(), widthMeasureSpec, 0),
                resolveSizeAndState(getSuggestedMinimumHeight(), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.min(w, h) / 2;
        startX = w / 2;
        startY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int segmentsCount = 12;
        final int segmentSize = 360 / segmentsCount;
        final double lineSize = 20;
        int x = startX, y = startY;

        paint.setColor(Color.BLACK);
        for (int segment = 0; segment < segmentsCount; segment++) {
            double angle = Math.toRadians(segment * segmentSize);
            double angleSin = Math.sin(angle);
            double angleCos = Math.cos(angle);

            canvas.drawLine(((float) (x + angleCos * (radius - lineSize))), ((float) (y + angleSin * (radius - lineSize))),
                    ((float) (x + angleCos * radius)), ((float) (y + angleSin * radius)), paint);
        }
    }

}
