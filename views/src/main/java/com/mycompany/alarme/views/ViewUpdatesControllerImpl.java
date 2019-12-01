package com.mycompany.alarme.views;

import android.view.View;

import androidx.core.util.Consumer;

class ViewUpdatesControllerImpl implements CircleObjectInteraction.ViewUpdatesController {

    private final View targetView;

    ViewUpdatesControllerImpl(View targetView) {
        this.targetView = targetView;
    }

    @Override
    public void updateViewParams(Consumer<ViewParams> viewParamsUpdater) {
        ViewParams params = new ViewParams(targetView.getLeft(), targetView.getTop(), targetView.getWidth(), targetView.getHeight(),
                targetView.getTranslationX(), targetView.getTranslationY(), targetView.getRotation());
        viewParamsUpdater.accept(params);
        targetView.setTranslationX(params.getTranslationX());
        targetView.setTranslationY(params.getTranslationY());
        targetView.setRotation(params.getRotation());
    }

    @Override
    public void updateLayoutParams(Consumer<CircleLayout.LayoutParams> layoutParamsUpdater) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void updateDrawerParams(Consumer<Object> drawerParamsUpdater) {
        throw new RuntimeException("not implemented");
    }
}
