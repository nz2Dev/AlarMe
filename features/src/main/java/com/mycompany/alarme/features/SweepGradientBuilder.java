package com.mycompany.alarme.features;

import android.graphics.SweepGradient;

import androidx.core.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class SweepGradientBuilder {

    private List<Item> items = new ArrayList<>();

    public SweepGradientBuilder add(float position, int color) {
        items.add(new Item(position, color));
        return this;
    }

    public SweepGradientBuilder addCircular(int degree, int color) {
        return add(MathUtils.clamp(degree, 0, 360) / 360f, color);
    }

    SweepGradientBuilder copy() {
        final SweepGradientBuilder copy = new SweepGradientBuilder();
        copy.items = new ArrayList<>(this.items);
        return copy;
    }

    SweepGradient build(float cx, float cy) {
        int[] colors = new int[items.size()];
        float[] positions = new float[items.size()];
        for (int i = 0; i < items.size(); i++) {
            colors[i] = items.get(i).color;
            positions[i] = items.get(i).position;
        }
        return new SweepGradient(cx, cy, colors, positions);
    }

    public static class Item {

        private float position;
        private int color;

        public Item(float position, int color) {
            this.position = position;
            this.color = color;
        }
    }

}
