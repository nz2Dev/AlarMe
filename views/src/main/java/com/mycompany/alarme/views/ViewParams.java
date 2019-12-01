package com.mycompany.alarme.views;

public class ViewParams {

    private float left;
    private float top;
    private float width;
    private float height;

    private float translationX;
    private float translationY;
    private float rotation;

    public ViewParams(float left, float top, float width, float height, float translationX, float translationY, float rotation) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.translationX = translationX;
        this.translationY = translationY;
        this.rotation = rotation;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setLeft(float left) {
        this.left = left;
    }

    public float getLeft() {
        return left;
    }

    public void setTop(float top) {
        this.top = top;
    }

    public float getTop() {
        return top;
    }

    public void setTranslationX(float translationX) {
        this.translationX = translationX;
    }

    public float getTranslationX() {
        return translationX;
    }

    public void setTranslationY(float translationY) {
        this.translationY = translationY;
    }

    public float getTranslationY() {
        return translationY;
    }

    public float getRotation() {
        return rotation;
    }
}
