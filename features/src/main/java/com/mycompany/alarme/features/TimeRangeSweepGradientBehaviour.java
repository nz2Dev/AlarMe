package com.mycompany.alarme.features;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import java.util.List;
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

        final List<SweepGradientBuilder.Item> processedSections = Observable
                .fromIterable(sectionsDataSet)
                .firstOrError()
                .flatMapObservable(forwardSection -> {
                    final int initialRotation = forwardSection.getRotation();
                    child.setGradientStartDegree(initialRotation);
                    return Observable.fromIterable(sectionsDataSet)
                            .map(section -> new SweepGradientBuilder.Item(section.getRotation(), section.getColor()))
                            .doOnNext(item -> item.setRotation(item.getRotation() - initialRotation));
                })
                .sorted((o1, o2) -> o1.getRotation() - o2.getRotation())
                .toList()
                .blockingGet();

        child.setGradient(Observable.fromIterable(processedSections)
                .collect(SweepGradientBuilder::new, SweepGradientBuilder::addSection)
                .doOnSuccess(builder -> {
                    final SweepGradientBuilder.Item lastItem =
                            processedSections.get(processedSections.size() - 1);

                    builder.addSection(
                            lastItem.getRotation(), Color.TRANSPARENT);
                })
                .blockingGet());
    }

}
