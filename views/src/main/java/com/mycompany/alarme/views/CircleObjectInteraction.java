package com.mycompany.alarme.views;

import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.core.util.Consumer;

import com.mycompany.alarme.views.CircleLayout.LayoutParams;

public interface CircleObjectInteraction {

    // Check contact by layout params
    // or maybe by other components params
    // all of them can be provided by the params provider interface.
    // ParamsProvider interface should leave only during the contact checking phase
    // so it should be deleted by the layout after work is done
    boolean hasContact(int viewId, HostViewContext host, ViewParamsProvider paramsProvider, MotionEvent motionEvent);

    // Same thing regarding lifetime of updates controller here,
    // it should be created before hasContact is called, and be deleted after contact is ended
    void updateContact(int viewId, HostViewContext host, ViewUpdatesController updatesController, MotionEvent motionEvent);

    void endContact(int viewId, HostViewContext host, MotionEvent motionEvent);

    interface ViewParamsProvider {

        Rect getHitRect();

        ViewParams getViewParams();

        LayoutParams getLayoutParams();

        Object getDrawerParams();

    }

    interface ViewUpdatesController {

        void updateViewParams(Consumer<ViewParams> viewParamsUpdater);

        void updateLayoutParams(Consumer<LayoutParams> layoutParamsUpdater);

        void updateDrawerParams(Consumer<Object> drawerParamsUpdater);

    }

}
