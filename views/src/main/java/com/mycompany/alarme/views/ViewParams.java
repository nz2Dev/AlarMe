package com.mycompany.alarme.views;

public class ViewParams {

    private float translationX;
    private float translationY;
    private float rotation;

    public ViewParams(float translationX, float translationY, float rotation) {
        this.translationX = translationX;
        this.translationY = translationY;
        this.rotation = rotation;
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
