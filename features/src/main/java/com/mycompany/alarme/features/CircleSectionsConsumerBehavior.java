package com.mycompany.alarme.features;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.getChildMeasureSpec;
import static java.util.Objects.requireNonNull;

public abstract class CircleSectionsConsumerBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private int childRadius;
    private Map<Integer, SectionData> sectionsData = new HashMap<>();

    public CircleSectionsConsumerBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull V child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int minSize = Math.min(View.MeasureSpec.getSize(parentWidthMeasureSpec), View.MeasureSpec.getSize(parentHeightMeasureSpec));
        childRadius = minSize / 2;

        int constrainedWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(minSize, View.MeasureSpec.getMode(parentWidthMeasureSpec));
        int childWidthMeasureSpec = getChildMeasureSpec(constrainedWidthMeasureSpec, /*todo support padding*/0, child.getLayoutParams().width);

        int constrainedHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(minSize, View.MeasureSpec.getMode(parentHeightMeasureSpec));
        int childHeightMeasureSpec = getChildMeasureSpec(constrainedHeightMeasureSpec, 0, child.getLayoutParams().height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        return true;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
        int startX = childRadius;
        int startY = childRadius;
        child.layout(startX - child.getMeasuredWidth() / 2, startY - child.getMeasuredHeight() / 2,
                startX + child.getMeasuredWidth() / 2, startY + child.getMeasuredHeight() / 2);

        return true;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        return ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior() instanceof CircleSectionContributorBehaviour;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
        if (dependency.getId() == View.NO_ID) {
            return false;
        }

        final CircleSectionContributorBehaviour contributorBehaviour =
                requireNonNull((CircleSectionContributorBehaviour)
                        ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams())
                        .getBehavior());

        SectionData dependencySectionData = sectionsData.get(dependency.getId());
        if (dependencySectionData == null) {
            dependencySectionData = new SectionData(dependency.getId(), contributorBehaviour.getDependsOnContributorChild());
            sectionsData.put(dependency.getId(), dependencySectionData);
        }

        dependencySectionData.rotation = contributorBehaviour.getCurrentDegree();
        dependencySectionData.color = contributorBehaviour.getDrawerColor();

        final ArrayList<SectionData> sections = new ArrayList<>(sectionsData.values());
        Collections.sort(sections, (o1, o2) -> o1.getRotation() - o2.getRotation());
        onContributorsDataChanged(sections, child);
        return false;
    }

    /**
     * Notifies that some contributed sections are changed.
     * Sections are ordered so that first section is the one
     * who's rotation is the smallest in range [0, 360].
     * Section data might contain additional info about it's context
     */
    protected abstract void onContributorsDataChanged(List<SectionData> sections, V child);

    // todo delegate representation of section state to the contributor behaviour
    // so that consumer will only be an aggregator of that, and will produce
    // sorted sequential section data based on current rotation of section
    public static class SectionData {

        public final int sectionViewId;
        public final int dependsOnViewId;
        private int rotation;
        private int color;

        public SectionData(int sectionViewId, int dependsOnViewId) {
            this.sectionViewId = sectionViewId;
            this.dependsOnViewId = dependsOnViewId;
        }

        // this and
        public int getSectionViewId() {
            return sectionViewId;
        }

        // this is the context of the section data
        // e.g it tels that the section is "dependable"
        // so it provides more "context" to the consumer that might use that for additional logic
        // this should be extendable, so should be controlled by the contributor via interface
        public int getDependsOnViewId() {
            return dependsOnViewId;
        }

        public int getRotation() {
            return rotation;
        }

        public int getColor() {
            return color;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("{%s, %s}",
                    sectionViewId, dependsOnViewId);
        }
    }

}
