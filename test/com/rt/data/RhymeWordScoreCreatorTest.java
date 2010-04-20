package com.rt.data;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RhymeWordScoreCreatorTest {

    private RhymeWordRatingCalculator creator;

    @Before public void setUp(){
        creator = new RhymeWordRatingCalculator();
    }

    private List<RhymePartData> makeRhymeData(int ... values){
        List<RhymePartData> res = Lists.newArrayList();
        for(int value : values){
            res.add(new RhymePartData(null, new ArrayList<String>(), new ArrayList<String>(), value,  new ArrayList<String>()));
        }
        return res;
    }

    @Test public void testCalculate()throws Exception{
         Assert.assertEquals(97, creator.calculate(makeRhymeData(5, 5, 2, 2, 2)));
        //50 + 25 + 10 + 10 + 2
    }

    @Test(expected = RuntimeException.class)
    public void testThrowsException(){
         creator.calculate(makeRhymeData(2, 5, 5, 2, 2, 2));
        //50 + 25 + 10 + 10 + 2
    }
}