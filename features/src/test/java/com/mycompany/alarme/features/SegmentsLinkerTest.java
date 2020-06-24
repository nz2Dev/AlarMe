package com.mycompany.alarme.features;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SegmentsLinkerTest {

    @Test
    public void linkInOrder() {
        class ActualSegment implements SegmentsLinker.Segment {
            private int anchor, dependency;
            ActualSegment(int anchor, int dependency) {
                this.anchor = anchor;
                this.dependency = dependency;
            }
            @Override
            public int anchor() {
                return anchor;
            }

            @Override
            public int dependency() {
                return dependency;
            }
        }

        final List<SegmentsLinker.Segment> segments = Arrays.asList(
                new ActualSegment(1, 5),
                new ActualSegment(5, 8),
                new ActualSegment(8, 2),
                new ActualSegment(2, 6),
                new ActualSegment(6, 3),
                new ActualSegment(3, 4)
        );
        Collections.shuffle(segments);

        final List<SegmentsLinker.Segment> orderedSegments =
                new SegmentsLinker().linkInOrder(segments);

        orderedSegments.size();
    }

}