import java.util.*;

/*
 * Title: Count Container Pairs Within Fill Difference
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A shipping company records the current fill level of each storage container as an integer
 * percentage from 0 to 100. For a balancing operation, two containers are considered compatible
 * if the absolute difference between their fill levels is at most k.
 *
 * You are given an integer array fills, where fills[i] is the fill level of the i-th container,
 * and an integer k. Return the number of index pairs (i, j) such that i < j and
 * |fills[i] - fills[j]| <= k.
 *
 * Because the number of valid pairs can be large, your algorithm should be efficient enough
 * for large inputs. A brute-force O(n^2) solution will not pass the largest test cases.
 *
 * Your task is to count all compatible pairs, not to list them.
 *
 * Constraints:
 * - 1 <= fills.length <= 2 * 10^5
 * - 0 <= fills[i] <= 10^9
 * - 0 <= k <= 10^9
 * - The answer may exceed 32-bit integer range, so use a 64-bit integer type where needed.
 *
 * Example 1:
 * Input: fills = [10, 13, 15, 19], k = 3
 * Output: 2
 *
 * Explanation:
 * Pair differences:
 * - |10 - 13| = 3  -> valid
 * - |10 - 15| = 5  -> invalid
 * - |10 - 19| = 9  -> invalid
 * - |13 - 15| = 2  -> valid
 * - |13 - 19| = 6  -> invalid
 * - |15 - 19| = 4  -> invalid
 * Total valid pairs = 2
 *
 * Example 2:
 * Input: fills = [7, 7, 8, 12, 13], k = 1
 * Output: 4
 *
 * Explanation:
 * Valid pairs are:
 * - (7 at index 0, 7 at index 1)
 * - (7 at index 0, 8)
 * - (7 at index 1, 8)
 * - (12, 13)
 * Total valid pairs = 4
 *
 * Efficient Idea:
 * 1. Sort the array.
 * 2. Use two pointers to maintain a window where the difference between the current right value
 *    and the leftmost value is at most k.
 * 3. For each right pointer, all elements between left and right - 1 form valid pairs with right.
 * 4. Add that count to the answer.
 */

public class Solution {

    /**
     * Counts the number of index pairs (i, j) such that i < j and
     * |fills[i] - fills[j]| <= k.
     *
     * The method sorts the values first, then uses a sliding window / two-pointer approach.
     * After sorting, for each position "right", we move "left" forward until the window
     * satisfies:
     *     fills[right] - fills[left] <= k
     *
     * Once that is true, every index from left to right - 1 forms a valid pair with right,
     * because the array is sorted and all those values are within k of fills[right].
     *
     * @param fills the array of container fill levels
     * @param k the maximum allowed absolute difference between two fill levels
     * @return the total number of valid pairs as a long
     * Time complexity: O(n log n) due to sorting, where n is fills.length
     * Space complexity: O(n) because we copy the array before sorting
     */
    public long countCompatiblePairs(int[] fills, int k) {
        // Defensive handling for completeness.
        // With fewer than 2 elements, no pair can exist.
        if (fills == null || fills.length < 2) {
            return 0L;
        }

        // We copy the input array so that the original data remains unchanged.
        // This is often a beginner-friendly and safer practice in demonstration code.
        int[] sorted = Arrays.copyOf(fills, fills.length);

        // Sorting is the key step that makes the two-pointer technique possible.
        // After sorting:
        // - values are in non-decreasing order
        // - for a fixed right pointer, if sorted[right] - sorted[left] <= k,
        //   then every element between left and right also works with right
        Arrays.sort(sorted);

        long pairCount = 0L;

        // "left" marks the start of the current valid window.
        int left = 0;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < sorted.length; right++) {

            // If the current window is invalid, move "left" forward until it becomes valid.
            //
            // Why does this work?
            // Because the array is sorted, sorted[right] is the largest value in the window.
            // So the maximum difference inside the window with respect to "right" is
            // sorted[right] - sorted[left].
            //
            // If that difference is greater than k, then sorted[left] cannot pair with
            // sorted[right], so we must exclude it by incrementing left.
            while (sorted[right] - sorted[left] > k) {
                left++;
            }

            // At this point, the window [left, right] is valid with respect to "right":
            // sorted[right] - sorted[left] <= k
            //
            // Since the array is sorted, every index i in [left, right - 1] satisfies:
            // sorted[right] - sorted[i] <= k
            //
            // Therefore, all those elements form valid pairs with "right".
            //
            // Number of such pairs added now:
            // right - left
            //
            // Example:
            // If left = 2 and right = 5, then indices 2, 3, 4 can pair with 5.
            // That is 3 pairs = 5 - 2.
            pairCount += (right - left);
        }

        return pairCount;
    }

    /**
     * A simple brute-force method used only for demonstration and verification on small inputs.
     * This checks every pair directly.
     *
     * This method is not efficient enough for large inputs, but it is useful for validating
     * the optimized solution on examples.
     *
     * @param fills the array of container fill levels
     * @param k the maximum allowed absolute difference
     * @return the total number of valid pairs as a long
     * Time complexity: O(n^2)
     * Space complexity: O(1) extra space
     */
    public long countCompatiblePairsBruteForce(int[] fills, int k) {
        if (fills == null || fills.length < 2) {
            return 0L;
        }

        long count = 0L;

        for (int i = 0; i < fills.length; i++) {
            for (int j = i + 1; j < fills.length; j++) {
                if (Math.abs((long) fills[i] - fills[j]) <= k) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * It also prints the brute-force result for the same samples so that the optimized
     * answer can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the demo inputs; dominated by the called methods
     * Space complexity: Depends on the called methods
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] fills1 = {10, 13, 15, 19};
        int k1 = 3;

        long result1 = solution.countCompatiblePairs(fills1, k1);
        long brute1 = solution.countCompatiblePairsBruteForce(fills1, k1);

        System.out.println("Sample 1:");
        System.out.println("fills = " + Arrays.toString(fills1) + ", k = " + k1);
        System.out.println("Optimized result = " + result1);
        System.out.println("Brute-force result = " + brute1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample 2
        int[] fills2 = {7, 7, 8, 12, 13};
        int k2 = 1;

        long result2 = solution.countCompatiblePairs(fills2, k2);
        long brute2 = solution.countCompatiblePairsBruteForce(fills2, k2);

        System.out.println("Sample 2:");
        System.out.println("fills = " + Arrays.toString(fills2) + ", k = " + k2);
        System.out.println("Optimized result = " + result2);
        System.out.println("Brute-force result = " + brute2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick sanity checks
        int[] fills3 = {5};
        int k3 = 10;
        System.out.println("Sanity Check 1:");
        System.out.println("fills = " + Arrays.toString(fills3) + ", k = " + k3);
        System.out.println("Result = " + solution.countCompatiblePairs(fills3, k3));
        System.out.println("Expected = 0");
        System.out.println();

        int[] fills4 = {1, 1, 1, 1};
        int k4 = 0;
        System.out.println("Sanity Check 2:");
        System.out.println("fills = " + Arrays.toString(fills4) + ", k = " + k4);
        System.out.println("Result = " + solution.countCompatiblePairs(fills4, k4));
        System.out.println("Expected = 6");
        System.out.println();

        int[] fills5 = {1, 10, 20, 30};
        int k5 = 100;
        System.out.println("Sanity Check 3:");
        System.out.println("fills = " + Arrays.toString(fills5) + ", k = " + k5);
        System.out.println("Result = " + solution.countCompatiblePairs(fills5, k5));
        System.out.println("Expected = 6");
    }
}