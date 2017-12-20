package fact.photonstream.timeSeriesExtraction;

/**
 * Utility class for adding elements of two arrays
 */
public class AddFirstArrayToSecondArray {

    /**
     * Adds the first array element-wise to the second array starting at a certain
     * position.
     *
     * @param first  The first array [N].
     * @param second The second array [M] is modified in place. The length M of the
     *               second array will not be changed.
     * @param at     The starting position of the addition in the second array.
     *               Can be negative.
     */
    public static void at(double[] first, double[] second, int at) {

        if (at > second.length) {
            return;
        }
        // Injection slice (at) is behind second array's end (end2)
        // No overlap.
        //                                     [...first...
        //                   [...second...]
        //                               ^end2  ^at

        int end = at + first.length;
        if (end < 0) {
            return;
        }
        // End of first array (end1) is before second array's start (start1)
        // No overlap.
        // [...first...]
        //                   [...second...]
        //  ^at       ^end1   ^start2

        if (end >= second.length) {
            end = second.length;
        }
        // End of first array goes beyond first array's end
        //                           ...first...]
        //                    ...second...]
        //                                     ^end1

        int start = at;
        if (start < 0) {
            start = 0;
        }
        //           [...first...
        //                   [...second...
        //            ^at     ^start2=0

        for (int i = start; i < end; i++) {
            second[i] += first[i - at];
        }
    }
}
