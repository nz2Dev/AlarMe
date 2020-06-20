package com.mycompany.alarme.features;

import android.content.Context;
import android.graphics.Color;
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

        child.setGradientStartDegree(0);
        child.setGradient(Observable.fromIterable(sectionsDataSet)
                .sorted((s1, s2) -> s1.rotation - s2.rotation)
                .collectInto(new SweepGradientBuilder(), (gb, section) -> gb.addCircular(section.rotation, section.color))
                .flatMap(gradientBuilder -> {
                    return Observable.fromIterable(sectionsDataSet)
                            .sorted((s1, s2) -> s1.rotation - s2.rotation)
                            .lastElement()
                            .map(lastSection -> gradientBuilder.addCircular(lastSection.rotation, Color.TRANSPARENT))
                            .toSingle(gradientBuilder);
                })
                .blockingGet());
    }

}
