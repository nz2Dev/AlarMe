package com.mycompany.alarme.features;

import org.junit.Assert;
import org.junit.Test;

public class CircleSectionUtilsTest {

    @Test
    public void to360RangeTest() {
        Assert.assertEquals(270, CircleSectionUtils.to360Range(-90));
        Assert.assertEquals(180, CircleSectionUtils.to360Range(-180));
        Assert.assertEquals(90, CircleSectionUtils.to360Range(-270));
        Assert.assertEquals(0, CircleSectionUtils.to360Range(-360));
        Assert.assertEquals(270, CircleSectionUtils.to360Range(-450));
        Assert.assertEquals(90, CircleSectionUtils.to360Range(90));
        Assert.assertEquals(180, CircleSectionUtils.to360Range(180));
        Assert.assertEquals(270, CircleSectionUtils.to360Range(270));
        Assert.assertEquals(359, CircleSectionUtils.to360Range(359));
        Assert.assertEquals(1, CircleSectionUtils.to360Range(361));
    }

}