package com.mycompany.alarme.views;

public class NotesPlaceholder {

    private void unsued() {
        // tries to apply some measuring logic to specific child, taking into account only our measure specs
        // uses getChildMeasureSpec() to get measure spec for specific view
        // fixme measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec); // method, calls child.measure()
        // iterate over all views, and apply above method for each of them, ignoring views that has been gone.
        // fixme measureChildren(widthMeasureSpec, heightMeasureSpec);
        // tries to apply some measuring logic to specific child,
        // taking into account our measure specs and width/height that the child can fit into,
        // uses getChildMeasureSpec() to get measure spec for specific view
        // fixme measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUser);
        // all methods above is a predefined default logic that we should write ourselves.
        // _fixme_ is only used there to highlight names


        // tries to find out measureSpec for child using our measureSpec and its layoutParams
        // and take into account already used space (padding, previous calculation)
        // fixme getChildMeasureSpec(spec, padding, childDimension); // :int, MeasureSpec integer for the child to pass into child.measure()
        // so I would name params: spec, allocatedSize, sizeDeclaredInLayoutParamsOrWantedSize.
        // This method will be used after calculation of childDimension our from layoutParams and allocatedSize.

        // util method for final calculations of our size
        // tries to fit size passed to the params, will get bigger whenever possible
        // fixme getDefaultSize(size, measureSpec); // :int, Size converted if needed by imposed measure spec constrain
        // can be used if needed

        // util for combining two integers bits (Mode and Size)
        // fixme MeasureSpec.makeMeasureSpec(size, mode); // :int, MeasureSpec
        // can be used if needed

        // tries to resolve our final size using calculated value and parent constraints,
        // child measure state should represent merged state of all children if any of them has such.
        // fixme resolveSizeAndState(size, measureSpec, childMeasuredState); // :int, resolved Size & State according to parent constraints with optional State bits.
        // fixme resolveSize(size, measureSpec); // :int, same as above, but without State information
        // should be used for setMeasureDimension on ourselves.
    }

}
