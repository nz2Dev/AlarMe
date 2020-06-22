package com.mycompany.alarme.features;

import android.graphics.SweepGradient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class SweepGradientBuilder {

    private List<Item> items = new ArrayList<>();

    public SweepGradientBuilder addSection(int degree, int color) {
        items.add(new Item(degree, color));
        return this;
    }

    public SweepGradientBuilder addSection(Item item) {
        items.add(item);
        return this;
    }

    SweepGradientBuilder copy() {
        final SweepGradientBuilder copy = new SweepGradientBuilder();
        copy.items = new ArrayList<>(this.items);
        return copy;
    }

    SweepGradient build(float cx, float cy) {
        final int[] colors = new int[items.size()];
        final float[] positions = new float[items.size()];

        Observable.fromIterable(items)
                .sorted((o1, o2) -> o1.getRotation() - o2.getRotation())
                .zipWith(Observable.range(0, items.size()), (item, index) -> {
                    positions[index] = item.getPosition();
                    colors[index] = item.getColor();
                    return index;
                })
                .ignoreElements()
                .blockingAwait();

        return new SweepGradient(cx, cy, colors, positions);
    }

    public static class Item {

        private int rotation;
        private int color;

        public Item(int rotation, int color) {
            this.color = color;
            setRotation(rotation);
        }

        public void setRotation(int rotation) {
            this.rotation = CircleSectionUtils.to360Range(rotation);
        }

        public int getRotation() {
            return rotation;
        }

        private float getPosition() {
            return rotation / 360f;
        }

        public int getColor() {
            return color;
        }
    }

}
