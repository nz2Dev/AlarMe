package com.mycompany.alarme.features;

public class CircleSectionUtils {

    static int to360Range(int minus180toPositive180Degree) {
        final int in360Range = minus180toPositive180Degree % 360;
        final int inPositive360Range = in360Range + 360;
        return inPositive360Range % 360;
    }

}
