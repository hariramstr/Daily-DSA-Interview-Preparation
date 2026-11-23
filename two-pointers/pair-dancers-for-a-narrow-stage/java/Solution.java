/*
 * Title: Pair Dancers for a Narrow Stage
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A performance team is preparing dancers for a stage act. Each dancer has a body width
 * represented by an integer in the array widths, where widths[i] is the width of the i-th dancer.
 * A stage mark can hold at most two dancers at the same time, and the combined width of any pair
 * assigned to the same mark must be less than or equal to stageLimit. A dancer may also perform
 * alone on a mark.
 *
 * Your task is to determine the minimum number of stage marks needed so that every dancer can be
 * assigned to exactly one mark.
 *
 * You may reorder the dancers in any way before assigning them. The goal is not to count the number
 * of valid pairings, but to minimize the total number of marks used. This makes greedy pairing
 * important: using a wide dancer with the best possible narrow partner can reduce the total number
 * of marks.
 *
 * Return the minimum number of stage marks required.
 *
 * Constraints:
 * - 1 <= widths.length <= 100000
 * - 1 <= widths[i] <= stageLimit <= 1000000000
 * - Every dancer must be assigned to exactly one mark
 * - Each mark can hold one or two dancers only
 *
 * Example 1:
 * Input: widths = [2, 3, 4, 5], stageLimit = 6
 * Output: 3
 * Explanation: Pair dancers with widths 2 and 4 on one mark. The dancers with widths 3 and 5
 * must stand alone. So the minimum number of marks is 3.
 *
 * Example 2:
 * Input: widths = [1, 2, 2, 3, 4], stageLimit = 4
 * Output: 3
 * Explanation: One optimal arrangement is (1,3), (2,2), and (4). This uses 3 marks, which is
 * the minimum possible.
 */

import java.util.*;

public class Solution {

    /**
     * Computes the minimum number of stage marks needed to place all dancers.
     *
     * The idea is:
     * 1. Sort the widths.
     * 2. Try to pair the narrowest remaining dancer with the widest remaining dancer.
     * 3. If they fit together, place them on one mark.
     * 4. If they do not fit, the widest dancer must stand alone, because that widest dancer
     *    cannot pair with any other remaining dancer either (all others are at least as wide
     *    as the current narrowest candidate or larger than it).
     *
     * This greedy strategy is optimal and is a classic two-pointer pattern.
     *
     * @param widths the array of dancer widths
     * @param stageLimit the maximum total width allowed on one stage mark
     * @return the minimum number of stage marks required
     * Time complexity: O(n log n) due to sorting, where n is widths.length
     * Space complexity: O(1) extra space beyond the sort's internal usage (ignoring input storage)
     */
    public int minStageMarks(int[] widths, int stageLimit) {
        // Defensive handling for completeness.
        // The problem guarantees at least one dancer, but this makes the method safer and beginner-friendly.
        if (widths == null || widths.length == 0) {
            return 0;
        }

        // Sort widths so we can efficiently pair the smallest and largest remaining dancers.
        Arrays.sort(widths);

        // left points to the narrowest dancer not yet assigned.
        int left = 0;

        // right points to the widest dancer not yet assigned.
        int right = widths.length - 1;

        // This will count how many stage marks we use.
        int marks = 0;

        // Continue until every dancer has been assigned.
        while (left <= right) {
            // Every iteration assigns the widest remaining dancer.
            // Therefore, every iteration uses exactly one stage mark.
            marks++;

            // Step 1:
            // Check whether the narrowest and widest remaining dancers can share one mark.
            //
            // Why this works:
            // - If the widest dancer can pair with the narrowest dancer, we should do it,
            //   because that saves one mark and leaves the middle dancers for later.
            // - If the widest dancer cannot pair with the narrowest dancer, then the widest
            //   dancer cannot pair with anyone else either, because everyone else is wider
            //   than or equal to the narrowest dancer.
            if (left < right && widths[left] + widths[right] <= stageLimit) {
                // They fit together, so assign both on the same mark.
                left++;
                right--;
            } else {
                // They do not fit together, or only one dancer remains.
                // In either case, the widest dancer takes a mark alone.
                right--;
            }
        }

        return marks;
    }

    /**
     * Helper method to print an example in a clean, readable format.
     *
     * @param widths the array of dancer widths
     * @param stageLimit the maximum total width allowed on one stage mark
     * @return the computed minimum number of stage marks
     * Time complexity: O(n log n) because it delegates to the main algorithm
     * Space complexity: O(1) extra space beyond the sort's internal usage
     */
    public int demonstrateExample(int[] widths, int stageLimit) {
        // We copy the array before solving so the original example remains unchanged for printing.
        int[] copy = Arrays.copyOf(widths, widths.length);
        int result = minStageMarks(copy, stageLimit);

        System.out.println("Widths: " + Arrays.toString(widths));
        System.out.println("Stage limit: " + stageLimit);
        System.out.println("Minimum stage marks needed: " + result);
        System.out.println();

        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demonstration inputs; each call is O(n log n)
     * Space complexity: O(1) extra space beyond sorting and copied arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // widths = [2, 3, 4, 5], stageLimit = 6
        // Sorted: [2, 3, 4, 5]
        // Pair 2 + 5 = 7 -> too large, so 5 alone
        // Pair 2 + 4 = 6 -> fits
        // 3 alone
        // Total = 3
        int result1 = solution.demonstrateExample(new int[]{2, 3, 4, 5}, 6);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println();

        // Sample Example 2
        // widths = [1, 2, 2, 3, 4], stageLimit = 4
        // Sorted: [1, 2, 2, 3, 4]
        // Pair 1 + 4 = 5 -> too large, so 4 alone
        // Pair 1 + 3 = 4 -> fits
        // Pair 2 + 2 = 4 -> fits
        // Total = 3
        int result2 = solution.demonstrateExample(new int[]{1, 2, 2, 3, 4}, 4);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println();

        // Additional beginner-friendly checks
        int result3 = solution.demonstrateExample(new int[]{3}, 3);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        int result4 = solution.demonstrateExample(new int[]{1, 1, 1, 1}, 2);
        System.out.println("Expected: 2, Actual: " + result4);
        System.out.println();

        int result5 = solution.demonstrateExample(new int[]{5, 5, 5}, 5);
        System.out.println("Expected: 3, Actual: " + result5);
    }
}