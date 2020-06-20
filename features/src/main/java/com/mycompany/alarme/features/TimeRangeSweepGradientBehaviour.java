package com.mycompany.alarme.features;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Set;

import io.reactivex.Observable;

public class TimeRangeSweepGradientBehaviour extends CircleSectionsConsumerBehavior<SweepGradientView> {

    public TimeRangeSweepGradientBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onContributorsDataChanged(Set<SectionData> sectionsDataSet, SweepGradientView child) {
        if (sectionsDataSet.size() < 2) {
            return;
        }

        child.setGradient(Observable.fromIterable(sectionsDataSet)
                .collectInto(new SweepGradientBuilder(), (gb, section) -> gb.addCircular(section.rotation, section.color))
                .blockingGet());
    }

}
