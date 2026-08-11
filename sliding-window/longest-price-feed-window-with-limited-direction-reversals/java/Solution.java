import java.util.*;

/*
 * Title: Longest Price Feed Window With Limited Direction Reversals
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] is the observed price of an asset at time i,
 * and an integer k. Consider any contiguous window prices[l..r]. For every adjacent pair inside the
 * window, define its direction as increasing if prices[i] < prices[i+1], decreasing if prices[i] > prices[i+1],
 * and flat if prices[i] == prices[i+1]. Flat steps do not contribute to direction changes.
 *
 * A window is called smooth if, after ignoring all flat steps, the sequence of remaining directions changes
 * between increasing and decreasing at most k times. In other words, if the non-flat comparisons inside the
 * window form directions like [+,+,-,-,+], then this window has 2 direction reversals.
 *
 * Return the length of the longest smooth contiguous window.
 *
 * This problem is harder than a standard sliding window because the validity of a window depends on transitions
 * between adjacent comparisons, not just frequencies of values. An efficient solution should process the array
 * in linear time by maintaining a moving window over the comparison sequence and counting how many times
 * consecutive non-zero directions differ.
 *
 * Constraints:
 * - 1 <= prices.length <= 200000
 * - -10^9 <= prices[i] <= 10^9
 * - 0 <= k <= prices.length
 *
 * Example 1:
 * Input: prices = [5,7,9,8,6,6,10,12], k = 1
 * Output: 6
 *
 * Example 2:
 * Input: prices = [4,4,4,3,2,5,7,6,1], k = 2
 * Output: 9
 */
public class Solution {

    /**
     * Computes the length of the longest smooth contiguous window.
     *
     * Core idea:
     * 1. Convert adjacent price pairs into a direction array:
     *    - +1 for increasing
     *    - -1 for decreasing
     *    -  0 for flat
     *
     * 2. A price window prices[l..r] corresponds to a direction subarray dir[l..r-1].
     *    We must count how many times the non-zero directions change sign inside that subarray.
     *
     * 3. We use a sliding window over the direction array.
     *    The difficult part is that zeros must be ignored when counting reversals.
     *
     * 4. To support O(1) updates while moving the left boundary, we precompute:
     *    nextNonZero[i] = index of the first non-zero direction at or after i, or m if none exists.
     *
     * 5. While expanding the right boundary:
     *    - If the new direction is non-zero, and the previous non-zero direction inside the current
     *      direction window has opposite sign, then reversals++.
     *
     * 6. While the window has too many reversals:
     *    - Move the left boundary of the direction window forward by one.
     *    - If the removed direction was non-zero, we check whether it formed a reversal with the next
     *      non-zero direction still inside the window. If yes, that reversal disappears, so reversals--.
     *
     * 7. If the current valid direction window is [leftDir .. rightDir], then the corresponding price
     *    window length is:
     *       (rightDir - leftDir + 1) directions  =>  (rightDir - leftDir + 2) prices
     *
     *    Also, a single price alone is always valid, so the answer is at least 1.
     *
     * @param prices the observed asset prices over time
     * @param k the maximum allowed number of direction reversals after ignoring flat steps
     * @return the maximum length of a smooth contiguous window
     * Time complexity: O(n), where n is prices.length
     * Space complexity: O(n) for the direction array and helper array
     */
    public int longestSmoothWindow(int[] prices, int k) {
        int n = prices.length;

        // A single element window has no adjacent comparisons, so it is always smooth.
        if (n <= 1) {
            return n;
        }

        // There are n - 1 adjacent comparisons between n prices.
        int m = n - 1;
        int[] dir = new int[m];

        // Build the direction array.
        // dir[i] describes the comparison between prices[i] and prices[i + 1].
        for (int i = 0; i < m; i++) {
            if (prices[i] < prices[i + 1]) {
                dir[i] = 1;
            } else if (prices[i] > prices[i + 1]) {
                dir[i] = -1;
            } else {
                dir[i] = 0;
            }
        }

        // nextNonZero[i] = first index j >= i such that dir[j] != 0, or m if no such index exists.
        // This lets us quickly find the next meaningful direction when removing the left edge.
        int[] nextNonZero = new int[m + 1];
        nextNonZero[m] = m;
        for (int i = m - 1; i >= 0; i--) {
            if (dir[i] != 0) {
                nextNonZero[i] = i;
            } else {
                nextNonZero[i] = nextNonZero[i + 1];
            }
        }

        // leftDir and rightDir define the current window in the direction array.
        int leftDir = 0;

        // reversals = number of sign changes among non-zero directions inside dir[leftDir..rightDir].
        int reversals = 0;

        // prevNonZeroDir stores the value (+1 or -1) of the most recent non-zero direction
        // seen while expanding the right boundary.
        int prevNonZeroDir = 0;

        // Best answer in terms of number of prices, not directions.
        int best = 1;

        for (int rightDir = 0; rightDir < m; rightDir++) {
            // Step 1: include dir[rightDir] into the current direction window.
            if (dir[rightDir] != 0) {
                // If there was a previous non-zero direction and the sign changes,
                // then we have discovered one additional reversal.
                if (prevNonZeroDir != 0 && prevNonZeroDir != dir[rightDir]) {
                    reversals++;
                }

                // Update the last seen non-zero direction.
                prevNonZeroDir = dir[rightDir];
            }

            // Step 2: shrink from the left until the window becomes valid again.
            while (reversals > k) {
                // We are about to remove dir[leftDir] from the window.

                if (dir[leftDir] != 0) {
                    // Find the next non-zero direction that remains after removing leftDir.
                    int nextIndex = nextNonZero[leftDir + 1];

                    // If such a next non-zero direction exists inside the current right boundary,
                    // then dir[leftDir] and dir[nextIndex] were consecutive non-zero directions
                    // in the current window before removal.
                    //
                    // If their signs differ, they contributed exactly one reversal.
                    // Once dir[leftDir] is removed, that reversal disappears.
                    if (nextIndex <= rightDir && dir[nextIndex] != dir[leftDir]) {
                        reversals--;
                    }
                }

                // Actually move the left boundary forward by one direction.
                leftDir++;
            }

            // Step 3: compute the corresponding price window length.
            //
            // A direction window [leftDir..rightDir] contains:
            //   rightDir - leftDir + 1 adjacent comparisons
            // Therefore it spans:
            //   rightDir - leftDir + 2 prices
            int currentPriceLength = rightDir - leftDir + 2;
            if (currentPriceLength > best) {
                best = currentPriceLength;
            }

            // Important subtlety:
            // prevNonZeroDir may become stale after leftDir moves, but that does not hurt correctness.
            // It is used only when extending rightDir, where we need the previous non-zero direction
            // among all directions seen so far in the current effective window ending at rightDir.
            //
            // Because we only ever append to the right, the previous non-zero direction before a new
            // non-zero dir[rightDir] is exactly the last non-zero direction encountered during expansion.
            // Shrinking from the left does not affect this "last non-zero on the right side" fact.
        }

        return best;
    }

    /**
     * Convenience helper to print an array in a readable form.
     *
     * @param arr the integer array to convert to text
     * @return a string representation such as [1, 2, 3]
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs a single demonstration test case and prints the result.
     *
     * @param prices the input prices array
     * @param k the maximum allowed number of reversals
     * @param expected the expected answer for demonstration
     * @return the computed answer
     * Time complexity: O(n), where n is prices.length
     * Space complexity: O(n)
     */
    public int runDemo(int[] prices, int k, int expected) {
        int result = longestSmoothWindow(prices, k);
        System.out.println("prices = " + arrayToString(prices));
        System.out.println("k = " + k);
        System.out.println("Longest smooth window length = " + result);
        System.out.println("Expected = " + expected);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demo cases)
     * Space complexity: O(max input size among demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] prices1 = {5, 7, 9, 8, 6, 6, 10, 12};
        solution.runDemo(prices1, 1, 6);

        // Sample 2
        int[] prices2 = {4, 4, 4, 3, 2, 5, 7, 6, 1};
        solution.runDemo(prices2, 2, 9);

        // Additional sanity checks

        // All flat: every window is smooth because there are no non-flat directions.
        int[] prices3 = {3, 3, 3, 3};
        solution.runDemo(prices3, 0, 4);

        // Strictly increasing: zero reversals, so full length is valid for any k >= 0.
        int[] prices4 = {1, 2, 3, 4, 5};
        solution.runDemo(prices4, 0, 5);

        // Alternating directions: with k = 0, the best window can contain only one direction block.
        int[] prices5 = {1, 3, 2, 4, 3};
        solution.runDemo(prices5, 0, 2);
    }
}