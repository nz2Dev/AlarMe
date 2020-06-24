package com.mycompany.alarme.features;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import java.util.List;

import io.reactivex.Observable;

public class TimeRangeSweepGradientBehaviour extends CircleSectionsConsumerBehavior<SweepGradientView> {

    public TimeRangeSweepGradientBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onContributorsDataChanged(List<SectionData> sections, SweepGradientView child) {
        final List<SectionData> sequentialSections = Observable.fromIterable(sections)
                .map(SectionSegmentWrapper::new)
                .toList()
                .map(wrappers -> new SegmentsLinker().linkInOrder(wrappers))
                .flattenAsObservable(ordered -> ordered)
                .map(wrapper -> ((SectionSegmentWrapper) wrapper).sectionData)
                .toList()
                .blockingGet();

        onSequentialSectionsChanged(sequentialSections, child);
    }

    protected void onSequentialSectionsChanged(List<SectionData> sequentialSections, SweepGradientView child) {
        if (sequentialSections.size() < 2)
            return;

        final int initialRotation = sequentialSections.get(0).getRotation();
        child.setGradientStartDegree(initialRotation);
        final List<SweepGradientBuilder.Item> processedSections = Observable.fromIterable(sequentialSections)
                .map(section -> new SweepGradientBuilder.Item(section.getRotation(), section.getColor()))
                .doOnNext(item -> item.setRotation(item.getRotation() - initialRotation))
                .toSortedList((o1, o2) -> o1.getRotation() - o2.getRotation())
                .blockingGet();

        child.setGradient(Observable.fromIterable(processedSections)
                .collect(SweepGradientBuilder::new, SweepGradientBuilder::addSection)
                .doOnSuccess(builder -> {
                    final int lastItemRotation = processedSections.get(processedSections.size() - 1).getRotation();
                    builder.addSection(lastItemRotation, Color.TRANSPARENT);
                })
                .blockingGet());
    }

    static class SectionSegmentWrapper implements SegmentsLinker.Segment {
        private SectionData sectionData;
        SectionSegmentWrapper(SectionData sectionData) {
            this.sectionData = sectionData;
        }
        @Override
        public int anchor() {
            return sectionData.getSectionViewId();
        }

        @Override
        public int dependency() {
            return sectionData.getDependsOnViewId();
        }
        public SectionData getSectionData() {
            return sectionData;
        }
    }

}
