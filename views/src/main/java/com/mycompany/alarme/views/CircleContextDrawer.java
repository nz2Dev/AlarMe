package com.mycompany.alarme.views;

import android.content.res.Resources.Theme;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.mycompany.alarme.views.CircleLayout.LayoutParams;

public interface CircleContextDrawer {

    // context drawer should record information about view's layout and view's drawer params
    default void onRecordLayoutParams(int viewId, @NonNull LayoutParams layoutParams) {
        // could be optional
    }

    // will be used to update specifically layout params of the view.
    // note however, the initial idea is when animating, the layout phase will be faked by reflecting View.TRANSLATE_X, View.TRANSLATE_Y params
    // so context drawer should think about params as if they was applied as regular, but in reality
    // the copy of the params is not set via setLayoutParams(LayoutParams). and stored to be applied
    // after the animation.
    default void onUpdateLayoutParams(int viewId, @NonNull LayoutParams newLayoutParams) {
        // could be optional
    }

    default void onRecordContextParams(int viewId, @NonNull Theme theme, @StyleRes int drawerParamsStyleId) {
        // could be optional
    }

    default void onUpdateContextParams(int viewId, @NonNull Theme theme, @StyleRes int newDrawerParamsStyleId) {
        // could be optional
    }

    // However, by using LayoutParams we are tied to specific implementation of CircleLayout.
    // If we instead could have possibility to rely purely on View's properties such as (X, Y, Rotation, etc)
    // we then can write drawer that could be used across different layout implementation
    // So here we record such params, and the method below updates them.
    // the reason why we are not passing a View there, because we would fake those values at some moment
    // or to have some flexibility, etc. And also some sort of encapsulation is applied there.
    // todo add the rest of properties
    default void onRecordViewParams(int viewId, float translateX, float translateY, float rotation) {
        // could be optional
    }

    default void onUpdateViewParams(int viewId, int newTranslateX, int newTranslateY, int newRotation) {
        // could be optional
    }

    // when initializing, this method indicates that there will be no more recording
    // and that view could calculate its initial result there.
    // note however, that for updating, events could happen individually
    // so the view should be updated after the method is called.
    void onRecordingFinished();

}
