package com.mycompany.alarme.views.interactions;

import android.graphics.Rect;
import android.view.MotionEvent;

import com.mycompany.alarme.views.CircleObjectInteraction;
import com.mycompany.alarme.views.HostViewContext;
import com.mycompany.alarme.views.ViewParams;

public class RotationObjectInteraction implements CircleObjectInteraction {

    private float touchedViewCenterX;
    private float touchedViewCenterY;
    private double startToTouchViewCenterDistance;

    @Override
    public boolean hasContact(int viewId, HostViewContext host, ViewParamsProvider paramsProvider, MotionEvent motionEvent) {
        final Rect hitRect = paramsProvider.getHitRect();
        final ViewParams viewParams = paramsProvider.getViewParams();
        final boolean contact = hitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());

        if (contact) {
            touchedViewCenterX = viewParams.getLeft() + viewParams.getWidth() / 2f;
            touchedViewCenterY = viewParams.getTop() + viewParams.getHeight() / 2f;

            // then find distance to the center of touched view from layout center
            float startToViewCenterVectorX = touchedViewCenterX - host.getCenterX();
            float startToViewCenterVectorY = touchedViewCenterY - host.getCenterY();
            startToTouchViewCenterDistance = Math.sqrt(startToViewCenterVectorX * startToViewCenterVectorX + startToViewCenterVectorY * startToViewCenterVectorY);
        }

        return contact;
    }

    @Override
    public void updateContact(int viewId, HostViewContext host, ViewUpdatesController updatesController, MotionEvent motionEvent) {
        final float touchX = motionEvent.getX();
        final float touchY = motionEvent.getY();

        // now we should calculate what degree current touch point is facing
        // todo then get the initial degree, and interpolate from there to current by some delta time
        float touchVectorX = touchX - host.getCenterX();
        float touchVectorY = touchY - host.getCenterY();
        double touchVectorAngle = Math.atan2(touchVectorY, touchVectorX);

        // multiply that distance by final degree angle and add layout center coordinate
        final double shiftedViewCenterX = Math.cos(touchVectorAngle) * startToTouchViewCenterDistance + host.getCenterX();
        final double shiftedViewCenterY = Math.sin(touchVectorAngle) * startToTouchViewCenterDistance + host.getCenterY();

        // shifted x and y subtract from touched view original x and y, those will be view new translation
        updatesController.updateViewParams(viewParams -> {
            viewParams.setTranslationX((float) (shiftedViewCenterX - touchedViewCenterX));
            viewParams.setTranslationY((float) (shiftedViewCenterY - touchedViewCenterY));
        });
    }

    @Override
    public void endContact(int viewId, HostViewContext host, MotionEvent motionEvent) {

    }

}
