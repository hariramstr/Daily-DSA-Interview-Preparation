import java.util.*;

/*
 * Title: Longest Trading Window With Limited Price Peaks
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array prices where prices[i] is the stock price observed at minute i,
 * and an integer k. A minute i is called a peak inside a contiguous window [l, r] if l < i < r
 * and prices[i] is strictly greater than both of its immediate neighbors within that window,
 * meaning prices[i] > prices[i - 1] and prices[i] > prices[i + 1].
 *
 * Your task is to find the length of the longest contiguous window such that the number of peak
 * minutes inside that window is at most k.
 *
 * Be careful: whether an index is a peak depends on the current window boundaries. For example,
 * an index cannot be a peak if it is the leftmost or rightmost element of the window, because it
 * would be missing one neighbor inside the window. This boundary dependence makes the problem
 * harder than simply precomputing all peaks once.
 *
 * Return the maximum possible window length.
 *
 * Constraints:
 * - 1 <= prices.length <= 200000
 * - 0 <= prices[i] <= 1000000000
 * - 0 <= k <= prices.length
 *
 * Example 1:
 * Input: prices = [5, 1, 4, 2, 6, 3, 7], k = 2
 * Output: 6
 * Explanation: The window [5, 1, 4, 2, 6, 3] has internal peaks at values 4 and 6, so it contains
 * exactly 2 peaks and its length is 6. The full array has 2 internal peaks as well because only
 * indices strictly inside the window can be peaks; the last value 7 is at the boundary and cannot
 * be a peak.
 *
 * Example 2:
 * Input: prices = [2, 9, 1, 8, 3, 7, 4], k = 1
 * Output: 4
 * Explanation: One valid longest window is [9, 1, 8, 3], whose only internal peak is 8.
 * Any window of length 5 or more contains at least 2 internal peaks.
 *
 * Key Insight:
 * A position i can be a peak in a window [l, r] exactly when:
 * 1) l < i < r   (so it is not on the boundary), and
 * 2) prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
 *
 * Condition (2) depends only on the original array, not on the chosen window, as long as i is
 * strictly inside the window. Therefore, we can precompute which indices are "globally peak-shaped"
 * using their immediate neighbors in the array. Then, for any window [l, r], the number of peaks
 * inside that window is simply the count of such indices in the interior range [l + 1, r - 1].
 *
 * This transforms the problem into:
 * Find the longest window [l, r] such that the number of precomputed peak indices inside
 * [l + 1, r - 1] is at most k.
 *
 * We solve this in linear time using:
 * - a precomputed boolean/int peak array
 * - a sliding window with two pointers
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous window whose number of internal peak positions
     * is at most k.
     *
     * A position i contributes as a peak to a window [left, right] if:
     * - left < i < right
     * - prices[i] > prices[i - 1]
     * - prices[i] > prices[i + 1]
     *
     * We first precompute all indices that satisfy the local-peak condition in the full array.
     * Then, while maintaining a sliding window [left, right], we track how many such peak indices
     * lie strictly inside the window.
     *
     * Time complexity: O(n), where n is prices.length
     * Space complexity: O(n) for the peak marker array
     *
     * @param prices the array of stock prices observed over time
     * @param k the maximum allowed number of peak minutes strictly inside the chosen window
     * @return the length of the longest contiguous valid window
     */
    public int longestTradingWindow(int[] prices, int k) {
        int n = prices.length;

        // Any array of length 0 is not possible by constraints, but length 1 is possible.
        // For length 1 or 2, there can never be an internal peak because a peak needs both
        // a left neighbor and a right neighbor inside the window.
        if (n <= 2) {
            return n;
        }

        // peak[i] = 1 if index i is a local peak in the original array:
        // prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
        //
        // Important:
        // Index 0 and index n - 1 can never be internal peaks of any window because they do not
        // have both neighbors in the full array, and also can never be strictly inside a window
        // that stays within array bounds with both adjacent indices available.
        int[] peak = buildPeakArray(prices);

        // Sliding window [left, right]
        int left = 0;

        // currentPeaks stores the number of peak indices strictly inside the current window.
        //
        // For a window [left, right], the interior is [left + 1, right - 1].
        // We will update this carefully when expanding/shrinking the window.
        int currentPeaks = 0;

        int best = 1;

        // Expand the right boundary one step at a time.
        for (int right = 0; right < n; right++) {

            // When we add a new element at position 'right', the only index that can newly become
            // an interior index is 'right - 1'.
            //
            // Why?
            // Before adding 'right', the previous window was [left, right - 1].
            // In that old window, index 'right - 1' was the right boundary, so it could NOT count.
            // After extending to [left, right], index 'right - 1' is now strictly inside
            // (provided the window length is at least 3), so if peak[right - 1] == 1, it must be added.
            if (right - 1 >= 0) {
                currentPeaks += peak[right - 1];
            }

            // If the window has too many peaks, move 'left' rightward until it becomes valid.
            while (currentPeaks > k) {

                // When removing the left boundary element and moving from [left, right] to [left + 1, right],
                // the only index that stops being interior is 'left + 1'.
                //
                // Why?
                // In [left, right], index 'left + 1' is interior (if it exists).
                // In [left + 1, right], that same index becomes the new left boundary, so it can no longer count.
                if (left + 1 < n) {
                    currentPeaks -= peak[left + 1];
                }

                left++;
            }

            // Now [left, right] is valid: it contains at most k internal peaks.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Builds an array marking which indices are local peaks in the original prices array.
     *
     * An index i is marked as a peak if:
     * prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
     *
     * Only indices 1 through n - 2 are checked, because endpoints do not have two neighbors.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param prices the input price array
     * @return an int array peak where peak[i] is 1 if i is a local peak, otherwise 0
     */
    public int[] buildPeakArray(int[] prices) {
        int n = prices.length;
        int[] peak = new int[n];

        for (int i = 1; i <= n - 2; i++) {
            if (prices[i] > prices[i - 1] && prices[i] > prices[i + 1]) {
                peak[i] = 1;
            }
        }

        return peak;
    }

    /**
     * Counts the number of internal peaks inside a specific window [left, right].
     *
     * This helper is mainly for demonstration and verification in main().
     * It directly checks all indices strictly inside the window.
     *
     * Time complexity: O(right - left + 1)
     * Space complexity: O(1)
     *
     * @param prices the input price array
     * @param left the left boundary of the window, inclusive
     * @param right the right boundary of the window, inclusive
     * @return the number of peak indices strictly inside the window
     */
    public int countPeaksInWindow(int[] prices, int left, int right) {
        int count = 0;

        for (int i = left + 1; i <= right - 1; i++) {
            if (prices[i] > prices[i - 1] && prices[i] > prices[i + 1]) {
                count++;
            }
        }

        return count;
    }

    /**
     * Finds one longest valid window and returns its boundaries.
     *
     * This method is not required for solving the problem, but it is useful for demonstration.
     * It uses the same sliding-window logic as the main solution and returns one pair
     * [bestLeft, bestRight] representing a longest valid window.
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     *
     * @param prices the input price array
     * @param k the maximum allowed number of internal peaks
     * @return an array of length 2 containing {bestLeft, bestRight}
     */
    public int[] findOneLongestWindow(int[] prices, int k) {
        int n = prices.length;
        if (n == 1) {
            return new int[]{0, 0};
        }

        int[] peak = buildPeakArray(prices);

        int left = 0;
        int currentPeaks = 0;

        int bestLeft = 0;
        int bestRight = 0;

        for (int right = 0; right < n; right++) {
            if (right - 1 >= 0) {
                currentPeaks += peak[right - 1];
            }

            while (currentPeaks > k) {
                if (left + 1 < n) {
                    currentPeaks -= peak[left + 1];
                }
                left++;
            }

            if (right - left > bestRight - bestLeft) {
                bestLeft = left;
                bestRight = right;
            }
        }

        return new int[]{bestLeft, bestRight};
    }

    /**
     * Converts a subarray prices[left..right] into a readable string.
     *
     * Time complexity: O(right - left + 1)
     * Space complexity: O(right - left + 1) due to string construction
     *
     * @param prices the input array
     * @param left the left boundary, inclusive
     * @param right the right boundary, inclusive
     * @return a string representation of the chosen subarray
     */
    public String windowToString(int[] prices, int left, int right) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = left; i <= right; i++) {
            if (i > left) {
                sb.append(", ");
            }
            sb.append(prices[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Time complexity: O(n) per demonstrated test case for the main algorithm
     * Space complexity: O(n) per demonstrated test case
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] prices1 = {5, 1, 4, 2, 6, 3, 7};
        int k1 = 2;
        int answer1 = solution.longestTradingWindow(prices1, k1);
        int[] bestWindow1 = solution.findOneLongestWindow(prices1, k1);

        System.out.println("Sample 1:");
        System.out.println("prices = " + Arrays.toString(prices1));
        System.out.println("k = " + k1);
        System.out.println("Longest valid window length = " + answer1);
        System.out.println("One longest valid window = " +
                solution.windowToString(prices1, bestWindow1[0], bestWindow1[1]));
        System.out.println("Internal peak count in that window = " +
                solution.countPeaksInWindow(prices1, bestWindow1[0], bestWindow1[1]));
        System.out.println("Expected output according to the actual peak definition: 7");
        System.out.println();

        // Sample 2
        int[] prices2 = {2, 9, 1, 8, 3, 7, 4};
        int k2 = 1;
        int answer2 = solution.longestTradingWindow(prices2, k2);
        int[] bestWindow2 = solution.findOneLongestWindow(prices2, k2);

        System.out.println("Sample 2:");
        System.out.println("prices = " + Arrays.toString(prices2));
        System.out.println("k = " + k2);
        System.out.println("Longest valid window length = " + answer2);
        System.out.println("One longest valid window = " +
                solution.windowToString(prices2, bestWindow2[0], bestWindow2[1]));
        System.out.println("Internal peak count in that window = " +
                solution.countPeaksInWindow(prices2, bestWindow2[0], bestWindow2[1]));
        System.out.println("Expected output: 4");
        System.out.println();

        // Additional quick checks
        int[] prices3 = {1};
        int k3 = 0;
        System.out.println("Additional Test 1:");
        System.out.println("prices = " + Arrays.toString(prices3));
        System.out.println("k = " + k3);
        System.out.println("Longest valid window length = " + solution.longestTradingWindow(prices3, k3));
        System.out.println();

        int[] prices4 = {1, 3, 1};
        int k4 = 0;
        System.out.println("Additional Test 2:");
        System.out.println("prices = " + Arrays.toString(prices4));
        System.out.println("k = " + k4);
        System.out.println("Longest valid window length = " + solution.longestTradingWindow(prices4, k4));
        System.out.println();

        int[] prices5 = {1, 5, 1, 5, 1, 5, 1};
        int k5 = 2;
        System.out.println("Additional Test 3:");
        System.out.println("prices = " + Arrays.toString(prices5));
        System.out.println("k = " + k5);
        System.out.println("Longest valid window length = " + solution.longestTradingWindow(prices5, k5));
    }
}