package com.rt.data;

import java.util.Iterator;
import java.util.List;

public class RhymeWordRatingCalculator {

    /**
     * Takes first 10 items and muliplies them by:
     * 10 5 5 5 1 1 ..
     *
     * Assumes rhymeData list is sorted with larges first!
     */
    public int calculate(List<RhymePartData> rhymeData){
        validateOrdering(rhymeData);
        MultiplierIterator iter = new MultiplierIterator();
        int value = 0;
        for(RhymePartData r:rhymeData){
            value += (iter.next() * r.getRating());
        }
        return value;
    }

    private void validateOrdering(List<RhymePartData> rhymeData){
        int last = Integer.MAX_VALUE;
        for(RhymePartData d : rhymeData){
            if(d.getRating() > last){
                throw new RuntimeException("rhyme data was not sorted");
            }
            last = d.getRating();
        }
    }

    public static final int[] MULTIPLIERS = new int[]{5, 5, 5, 2, 2};

    private static class MultiplierIterator implements Iterator<Integer> {

        private int index = 0;

        public boolean hasNext() {
            return true;
        }

        public Integer next() {
            if(index >= MULTIPLIERS.length){
                return 1;
            }else{
                return MULTIPLIERS[index++];
            }
        }

        public void remove() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
