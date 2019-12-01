package com.mycompany.alarme.views;

// sets the CircleLayout view context information such as CircleLayoutView's bounds rect, etc.
public class HostViewContext {

    private int left;
    private int top;
    private int width;
    private int height;
    private int centerX;
    private int centerY;

    void set(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        centerX = width / 2;
        centerY = height / 2;
    }

    // half of width
    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
