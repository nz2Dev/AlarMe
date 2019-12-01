package com.mycompany.alarme.views;

import android.graphics.Rect;
import android.view.View;

class ViewParamsProviderImpl implements CircleObjectInteraction.ViewParamsProvider {

    private Rect hitRectBuffer = new Rect();
    View targetView;

    @Override
    public Rect getHitRect() {
        targetView.getHitRect(hitRectBuffer);
        return hitRectBuffer;
    }

    @Override
    public ViewParams getViewParams() {
        return new ViewParams(targetView.getLeft(), targetView.getTop(), targetView.getWidth(), targetView.getHeight(),
                targetView.getTranslationX(), targetView.getTranslationY(), targetView.getRotation());
    }

    @Override
    public CircleLayout.LayoutParams getLayoutParams() {
        return (CircleLayout.LayoutParams) targetView.getLayoutParams();
    }

    @Override
    public Object getDrawerParams() {
        throw new RuntimeException("not supported yet");
    }
}
