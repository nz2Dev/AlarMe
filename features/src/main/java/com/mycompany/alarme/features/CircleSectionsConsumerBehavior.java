package com.mycompany.alarme.features;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            dependencySectionData = new SectionData(dependency.getId());
            sectionsData.put(dependency.getId(), dependencySectionData);
        }

        dependencySectionData.rotation = contributorBehaviour.getCurrentDegree();
        dependencySectionData.color = contributorBehaviour.getDrawerColor();

        onContributorsDataChanged(new HashSet<>(sectionsData.values()), child);
        return false;
    }

    protected abstract void onContributorsDataChanged(Set<SectionData> sectionsDataSet, V child);

    public static class SectionData {

        public final int sectionViewId;
        private int rotation;
        private int color;

        public SectionData(int sectionViewId) {
            this.sectionViewId = sectionViewId;
        }

        public int getRotation() {
            return rotation;
        }

        public int getColor() {
            return color;
        }
    }

}
