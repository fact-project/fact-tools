package fact.features.singlePulse.timeLineExtraction;

import java.util.Arrays;

public class AddFirstArrayToSecondArray {

    /**
    * Adds the first array elementwise to the second array starting at a certain
    * position. 
    *
    * @param first
    *           The first array [N].
    *
    * @param second
    *           The second array [M] is modified in place. The length M of the 
    *           second array will not be changed.
    *
    * @param at
    *           The starting position of the addition in the second array. 
    *           Can be negative.
    */    
    public static void at(double[] first, double[] second, int at) {
        
        if(at > second.length) return;
            // injection slice is behind second array's end. No overlap.
            //                                     [...first...]
            //                   [...second...]
            //                                      ^at

        int end = at + first.length;
        if(end < 0) return;
            // first array is completley before second. No overlap.
            //    [...first...]
            //                   [...second...]
            //     ^at

        if(end >= second.length) end = second.length;
            // end is limited to length of second
            // first array is completley before second. No overlap.
            //    [.............first...................]
            //                   [...second...]
            //     ^at                                 ^end 

        if(end > first.length + at) end = first.length + at;
            //              [...first...]
            //                   [...second...]
            //               ^at

        int start = at;
        if(start < 0) start = 0;

        for(int i=start; i<end; i++) {
            int acc1 = i-at;
            second[i] += first[acc1];        
        }
    }     
}