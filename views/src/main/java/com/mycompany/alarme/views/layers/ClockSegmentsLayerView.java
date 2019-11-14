package com.mycompany.alarme.views.layers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.mycompany.alarme.views.R;

public class ClockSegmentsLayerView extends View {

    public static final int SEGMENT_DIVISION_DEFAULT_LINE_SIZE = 20;
    public static final int SEGMENT_DIVISION_DEFAULT_OFFSET = 50;
    public static final int SEGMENT_DIVISION_DEFAULT_LINE_WIDTH = 5;

    private Paint segmentDivisionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double segmentDivisionLineLength;
    private @ColorInt int segmentDivisionLineColor;
    private int segmentsCount = 12;
    private int segmentDegree = 360 / segmentsCount;
    private int segmentDivisionOffset;
    private int segmentDivisionEndRadius;
    private int segmentDivisionLineWidth;

    private int startX;
    private int startY;
    private int radius;

    public ClockSegmentsLayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockSegmentsLayerView);
        segmentDivisionLineLength = typedArray.getDimensionPixelSize(R.styleable.ClockSegmentsLayerView_clock_segmentsDivisionLineLength, ClockSegmentsLayerView.SEGMENT_DIVISION_DEFAULT_LINE_SIZE);
        segmentDivisionOffset = typedArray.getDimensionPixelSize(R.styleable.ClockSegmentsLayerView_clock_segmentsDivisionOffset, ClockSegmentsLayerView.SEGMENT_DIVISION_DEFAULT_OFFSET);
        segmentDivisionLineColor = typedArray.getColor(R.styleable.ClockSegmentsLayerView_clock_segmentsDivisionLineColor, Color.GRAY);
        segmentDivisionLineWidth = typedArray.getDimensionPixelSize(R.styleable.ClockSegmentsLayerView_clock_segmentsDivisionLineWidth, ClockSegmentsLayerView.SEGMENT_DIVISION_DEFAULT_LINE_WIDTH);
        typedArray.recycle();

        segmentDivisionPaint.setStyle(Paint.Style.STROKE);
        segmentDivisionPaint.setColor(segmentDivisionLineColor);
        segmentDivisionPaint.setStrokeWidth(segmentDivisionLineWidth);
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
    protected void onDraw(Canvas canvas) {
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
    }
}
