import java.util.*;

/*
 * Title: Longest Packing Streak With Limited Heavy Boxes
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse packs boxes in the order they arrive. You are given an integer array weights,
 * where weights[i] is the weight of the i-th box, and an integer limit.
 * A box is considered heavy if its weight is strictly greater than limit.
 *
 * Find the length of the longest contiguous packing streak such that the streak contains
 * at most k heavy boxes. In other words, you may choose one continuous subarray of weights,
 * and inside that chosen range, the number of values greater than limit must not exceed k.
 *
 * Return the maximum possible length of such a streak.
 *
 * This problem models a common operational constraint: the packing line can handle only
 * a small number of heavy boxes in any uninterrupted run before requiring rebalancing.
 * Your task is to determine the longest valid run without breaking the order of boxes.
 *
 * Constraints:
 * - 1 <= weights.length <= 200000
 * - 0 <= weights[i] <= 1000000000
 * - 0 <= limit <= 1000000000
 * - 0 <= k <= weights.length
 *
 * Example 1:
 * Input: weights = [4, 9, 3, 12, 7, 15, 2], limit = 8, k = 2
 * Output: 5
 * Explanation: Heavy boxes are 9, 12, and 15. The longest contiguous streak with at most
 * 2 heavy boxes is [4, 9, 3, 12, 7], which has length 5.
 *
 * Example 2:
 * Input: weights = [11, 5, 6, 13, 14, 2, 1], limit = 10, k = 1
 * The written explanation in the prompt is inconsistent. The correct answer for this input is 4.
 * One valid optimal streak is [14, 2, 1] with length 3, but an even better valid streak is
 * [5, 6, 13] with length 3, and the true maximum is [13, 14, 2, 1] invalid due to 2 heavy boxes.
 * Checking all contiguous ranges carefully, the best valid length is actually 4 for [5, 6, 13, 14]
 * only if limit were different, which it is not. Under the stated rule (> 10 is heavy), the correct
 * maximum contiguous valid streak is 4 for [5, 6, 13, 14]? No, that has 2 heavy boxes and is invalid.
 * The actual correct maximum for the given input is 4? Let's verify:
 * - [11, 5, 6] => 1 heavy, length 3
 * - [5, 6, 13] => 1 heavy, length 3
 * - [14, 2, 1] => 1 heavy, length 3
 * - [5, 6, 13, 14] => 2 heavy, invalid
 * - [13, 14, 2, 1] => 2 heavy, invalid
 * Therefore the mathematically correct answer for Example 2 is 3.
 *
 * This solution implements the correct algorithm for the stated problem definition.
 */

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray containing at most k heavy boxes.
     *
     * A box is heavy if its weight is strictly greater than the given limit.
     *
     * The algorithm uses the classic sliding window / two-pointer technique:
     * - Expand the right pointer one step at a time.
     * - Count how many heavy boxes are currently inside the window.
     * - If the count becomes greater than k, move the left pointer forward
     *   until the window becomes valid again.
     * - Track the largest valid window length seen so far.
     *
     * @param weights the array of box weights in arrival order
     * @param limit the threshold above which a box is considered heavy
     * @param k the maximum number of heavy boxes allowed in the chosen contiguous streak
     * @return the length of the longest contiguous streak with at most k heavy boxes
     *
     * Time complexity: O(n), where n is weights.length, because each index is visited
     * at most twice: once by the right pointer and once by the left pointer.
     * Space complexity: O(1), because only a few integer variables are used.
     */
    public int longestPackingStreak(int[] weights, int limit, int k) {
        // Left boundary of the current sliding window.
        int left = 0;

        // Number of heavy boxes currently inside the window [left, right].
        int heavyCount = 0;

        // Best valid window length found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < weights.length; right++) {
            // Step 1:
            // Include weights[right] into the current window.
            // If this newly included box is heavy, increase the heavy counter.
            if (weights[right] > limit) {
                heavyCount++;
            }

            // Step 2:
            // If the window now contains too many heavy boxes,
            // it is invalid and must be shrunk from the left.
            //
            // We keep moving the left boundary forward until the window
            // again contains at most k heavy boxes.
            while (heavyCount > k) {
                // Before removing weights[left] from the window,
                // check whether it is heavy.
                // If yes, removing it decreases the heavy count.
                if (weights[left] > limit) {
                    heavyCount--;
                }

                // Actually shrink the window by moving left forward.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed valid:
            // it contains at most k heavy boxes.
            //
            // Compute its length and update the best answer if needed.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the whole array, maxLength is the answer.
        return maxLength;
    }

    /**
     * A helper method that prints a demonstration of the sliding-window result
     * for a single test case.
     *
     * @param weights the array of box weights
     * @param limit the heavy-box threshold
     * @param k the maximum allowed number of heavy boxes
     * @return the computed longest valid streak length
     *
     * Time complexity: O(n), where n is weights.length.
     * Space complexity: O(1), excluding output formatting.
     */
    public int demonstrateCase(int[] weights, int limit, int k) {
        int result = longestPackingStreak(weights, limit, k);
        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("limit = " + limit + ", k = " + k);
        System.out.println("Longest valid packing streak = " + result);
        System.out.println();
        return result;
    }

    /**
     * Program entry point.
     *
     * Demonstrates the solution on the sample inputs and a few additional checks.
     *
     * Note:
     * The second example in the prompt contains an inconsistent explanation.
     * This main method prints the mathematically correct result produced by the algorithm.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(1), excluding the input arrays themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        int[] weights1 = {4, 9, 3, 12, 7, 15, 2};
        int limit1 = 8;
        int k1 = 2;
        int result1 = solution.demonstrateCase(weights1, limit1, k1);
        System.out.println("Expected for Example 1: 5");
        System.out.println("Actual: " + result1);
        System.out.println();

        // Sample 2 from the prompt.
        // Important correctness note:
        // For the stated input and rule "heavy if weight > limit",
        // the correct answer is 3, not 4.
        int[] weights2 = {11, 5, 6, 13, 14, 2, 1};
        int limit2 = 10;
        int k2 = 1;
        int result2 = solution.demonstrateCase(weights2, limit2, k2);
        System.out.println("Prompt's stated output for Example 2: 4");
        System.out.println("Correct output under the stated problem definition: 3");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Additional quick sanity checks.
        int[] weights3 = {1, 2, 3, 4};
        System.out.println("Additional Test 1:");
        solution.demonstrateCase(weights3, 10, 0); // No heavy boxes at all, answer should be 4.

        int[] weights4 = {100, 200, 300};
        System.out.println("Additional Test 2:");
        solution.demonstrateCase(weights4, 50, 0); // All are heavy, no heavy allowed, answer should be 0.

        int[] weights5 = {5, 11, 6, 12, 7, 13, 8};
        System.out.println("Additional Test 3:");
        solution.demonstrateCase(weights5, 10, 2); // Mixed case.
    }
}