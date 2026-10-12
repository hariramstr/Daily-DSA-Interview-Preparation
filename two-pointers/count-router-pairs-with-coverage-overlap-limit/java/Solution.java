import java.util.*;

/*
 * Title: Count Router Pairs With Coverage Overlap Limit
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given a sorted array `positions` where `positions[i]` is the location of the
 * `i`-th Wi-Fi router installed along a straight road. For network planning, two routers
 * are considered a compatible pair if the distance between them is at most `maxGap`.
 * Your task is to count how many index pairs `(i, j)` with `0 <= i < j < n` satisfy
 * `positions[j] - positions[i] <= maxGap`.
 *
 * Because the router locations are already sorted, a brute-force solution that checks
 * all pairs would be too slow for large inputs. Design an efficient algorithm using
 * the two-pointers technique.
 *
 * Return the total number of compatible pairs.
 *
 * Constraints:
 * - `1 <= positions.length <= 2 * 10^5`
 * - `0 <= positions[i] <= 10^9`
 * - `positions` is sorted in non-decreasing order
 * - `0 <= maxGap <= 10^9`
 * - The answer may exceed 32-bit integer range, so use a 64-bit integer type when needed.
 *
 * Example 1:
 * Input: positions = [1, 3, 4, 9, 10], maxGap = 3
 * Output: 4
 * Explanation: Valid pairs are `(1,3)`, `(1,4)`, `(3,4)`, and `(9,10)` by value,
 * corresponding to index pairs `(0,1)`, `(0,2)`, `(1,2)`, and `(3,4)`.
 *
 * Example 2:
 * Input: positions = [2, 2, 5, 6, 8], maxGap = 2
 * Output: 3
 * Explanation:
 * - (0,1): 2 - 2 = 0 <= 2, valid
 * - (2,3): 6 - 5 = 1 <= 2, valid
 * - (3,4): 8 - 6 = 2 <= 2, valid
 * All other pairs have distance greater than 2.
 *
 * Efficient Idea:
 * Since the array is already sorted, for each left index `i`, we can move a right pointer
 * as far as possible while the distance stays within `maxGap`.
 * If the farthest valid right boundary is `right - 1`, then every index from `i + 1`
 * through `right - 1` forms a valid pair with `i`.
 * That contributes `(right - i - 1)` pairs.
 *
 * Because each pointer only moves forward, the total running time is linear.
 */

public class Solution {

    /**
     * Counts how many index pairs (i, j) satisfy i < j and positions[j] - positions[i] <= maxGap.
     *
     * This method uses the classic two-pointers / sliding window technique:
     * - `left` marks the first router in the current window.
     * - `right` expands to the farthest router that still stays within `maxGap` from `left`.
     * - Once `right` stops, every index between `left + 1` and `right - 1` forms a valid pair with `left`.
     * - Then we move `left` forward and continue.
     *
     * Important observation:
     * Because `positions` is sorted, once a router at index `right` is too far from `left`,
     * it will also be too far for any earlier left index. Therefore, `right` never needs to move backward.
     *
     * @param positions the sorted array of router positions along the road
     * @param maxGap the maximum allowed distance between two routers to be considered compatible
     * @return the total number of valid index pairs as a long
     *
     * Time complexity: O(n), where n is the number of routers, because each pointer moves forward at most n times.
     * Space complexity: O(1), ignoring the input array, because only a few variables are used.
     */
    public long countCompatiblePairs(int[] positions, int maxGap) {
        int n = positions.length;

        // We store the answer in a long because the number of pairs can be large.
        // For example, if all pairs are valid and n = 200,000, then the number of pairs is:
        // n * (n - 1) / 2 = 19,999,900,000, which does not fit in a 32-bit int.
        long totalPairs = 0L;

        // `right` will represent the first index that is NOT valid for the current `left`.
        // So the valid range for a given `left` is [left + 1, right - 1].
        int right = 0;

        // Try every index as the left endpoint of a pair.
        for (int left = 0; left < n; left++) {

            // Ensure `right` is always at least one step ahead of `left`.
            // This avoids counting pairs where i == j.
            if (right < left + 1) {
                right = left + 1;
            }

            // Expand `right` as long as:
            // 1. it is still inside the array, and
            // 2. the pair (left, right) is valid.
            //
            // Because the array is sorted, once positions[right] - positions[left] becomes too large,
            // every later index will also be too large for this same `left`.
            while (right < n && (long) positions[right] - positions[left] <= maxGap) {
                right++;
            }

            // At this point, `right` is the first invalid index.
            // Therefore, the valid indices paired with `left` are:
            // left + 1, left + 2, ..., right - 1
            //
            // Count of such indices:
            // (right - 1) - (left + 1) + 1 = right - left - 1
            totalPairs += (right - left - 1);
        }

        return totalPairs;
    }

    /**
     * A simple helper method that prints an input array in a readable format and shows the result.
     *
     * @param positions the sorted array of router positions
     * @param maxGap the maximum allowed distance
     * @return the computed number of compatible pairs
     *
     * Time complexity: O(n), due to array-to-string conversion for printing.
     * Space complexity: O(n), due to the string created for display.
     */
    public long demonstrateCase(int[] positions, int maxGap) {
        long result = countCompatiblePairs(positions, maxGap);
        System.out.println("positions = " + Arrays.toString(positions) + ", maxGap = " + maxGap);
        System.out.println("Compatible pairs = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method:
     * - demonstrates the exact examples from the prompt,
     * - prints the computed outputs,
     * - and includes a few extra sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(1) extra, excluding printing-related temporary objects.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt:
        // positions = [1, 3, 4, 9, 10], maxGap = 3
        // Valid pairs:
        // (0,1): 3 - 1 = 2 <= 3
        // (0,2): 4 - 1 = 3 <= 3
        // (1,2): 4 - 3 = 1 <= 3
        // (3,4): 10 - 9 = 1 <= 3
        // Total = 4
        long example1 = solution.demonstrateCase(new int[]{1, 3, 4, 9, 10}, 3);
        System.out.println("Expected: 4, Actual: " + example1);
        System.out.println();

        // Example 2 from the prompt:
        // positions = [2, 2, 5, 6, 8], maxGap = 2
        // Check all pairs:
        // (0,1): 2 - 2 = 0 valid
        // (0,2): 5 - 2 = 3 invalid
        // (0,3): 6 - 2 = 4 invalid
        // (0,4): 8 - 2 = 6 invalid
        // (1,2): 5 - 2 = 3 invalid
        // (1,3): 6 - 2 = 4 invalid
        // (1,4): 8 - 2 = 6 invalid
        // (2,3): 6 - 5 = 1 valid
        // (2,4): 8 - 5 = 3 invalid
        // (3,4): 8 - 6 = 2 valid
        // Total = 3
        long example2 = solution.demonstrateCase(new int[]{2, 2, 5, 6, 8}, 2);
        System.out.println("Expected: 3, Actual: " + example2);
        System.out.println();

        // Extra test 1: all equal positions, every pair is valid when maxGap = 0
        // n = 4, answer = 4 * 3 / 2 = 6
        long extra1 = solution.demonstrateCase(new int[]{7, 7, 7, 7}, 0);
        System.out.println("Expected: 6, Actual: " + extra1);
        System.out.println();

        // Extra test 2: strictly increasing, no valid pairs
        long extra2 = solution.demonstrateCase(new int[]{1, 5, 10, 20}, 2);
        System.out.println("Expected: 0, Actual: " + extra2);
        System.out.println();

        // Extra test 3: single element, no pair possible
        long extra3 = solution.demonstrateCase(new int[]{42}, 100);
        System.out.println("Expected: 0, Actual: " + extra3);
    }
}