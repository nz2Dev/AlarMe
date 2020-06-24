package com.mycompany.alarme.features;

import java.util.ArrayList;
import java.util.List;

public class SegmentsLinker {

    public List<Segment> linkInOrder(List<? extends Segment> segments) {
        final List<Segment> path = new ArrayList<>();
        final List<Segment> rawPool = new ArrayList<>();
        rawPool.addAll(segments);

        Segment target = segments.get(0);
        boolean addLast = true;
        do {
            if (addLast) {
                path.add(target);
            } else {
                path.add(0, target);
            }
            rawPool.remove(target);

            final Result result = findDependent(rawPool, path);
            if (result == null) {
                target = null;
            } else {
                target = result.section;
                addLast = result.addLast;
            }
        } while (target != null);

        return path;
    }

    Result findDependent(List<Segment> selections, List<Segment> path) {
        final Segment end = path.get(path.size() - 1);
        final Segment start = path.get(0);
        for (final Segment section : selections) {
            if (end.dependency() == section.anchor()) {
                return new Result(section, true);
            }
            if (section.dependency() == start.anchor()) {
                return new Result(section, false);
            }
        }
        return null;
    }

    public static class Result {
        private final Segment section;
        private final boolean addLast;
        public Result(Segment section, boolean addLast) {
            this.section = section;
            this.addLast = addLast;
        }
    }

    public interface Segment {
        int anchor();
        int dependency();
    }

}
