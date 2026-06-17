import java.util.*;

/*
Problem Title: Minimum Recolors to Form a Centered Price Peak

Problem Description:
You are given an integer array prices representing daily prices of a product. A day i is called a centered peak of radius r if there are at least r elements on both sides of i, and the subarray from i - r to i + r forms a strict mountain centered at i. In other words, for every d from 1 to r, prices[i - d] < prices[i - d + 1] and prices[i + d - 1] > prices[i + d]. The value at index i is the unique highest value in that window.

You may recolor (change) any element to any integer value in one operation. Return the minimum number of elements that must be recolored so that the array contains at least one centered peak of exact radius k.

Only the elements inside the chosen window of length 2k + 1 matter. You may choose any valid center i such that the full window fits inside the array. A position already satisfying the required strict relation does not need to be changed.

Your task is to find the minimum number of changes over all possible windows of length 2k + 1.

Constraints:
- 1 <= prices.length <= 200000
- 0 <= prices[i] <= 1000000000
- 1 <= k
- 2 * k + 1 <= prices.length

Example 1:
Input: prices = [1, 3, 5, 4, 2, 6, 7], k = 2
Output: 0
Explanation: The window [1, 3, 5, 4, 2] already forms a strict mountain centered at index 2. No recoloring is needed.

Example 2:
Input: prices = [4, 4, 4, 4, 4, 4], k = 2
Output: 4
Explanation: Any valid window has length 5. To make a strict mountain, the two left comparisons must be strictly increasing and the two right comparisons must be strictly decreasing. One possible result is [1, 2, 5, 3, 0] inside the chosen window, which requires changing 4 elements if the center value is kept. It is impossible with fewer than 4 changes.
*/

public class Solution {

    /**
     * Returns the minimum number of recolors needed so that some window of length 2k+1
     * becomes a strict mountain centered at its middle index.
     *
     * Core idea:
     * For a chosen center c, the window [c-k, c+k] is valid if:
     * 1) The left half [c-k .. c] is strictly increasing.
     * 2) The right half [c .. c+k] is strictly decreasing.
     *
     * Since one recolor can change any value to any integer, the minimum number of recolors
     * inside a chosen window is exactly:
     *   (number of positions in the left half that can stay unchanged in some strictly increasing subsequence ending at c)
     * + (number of positions in the right half that can stay unchanged in some strictly decreasing subsequence starting at c)
     * - 1 for the center counted twice
     * subtracted from the total window length.
     *
     * A crucial observation simplifies this dramatically:
     * - On the left side, the positions that can remain unchanged are exactly the maximal suffix
     *   ending at c that is already strictly increasing by adjacent comparisons.
     *   If a break occurs at some adjacent pair, then no earlier position can be kept unchanged,
     *   because every kept position must connect through all intermediate kept positions in order.
     * - Symmetrically, on the right side, the positions that can remain unchanged are exactly the
     *   maximal prefix starting at c that is already strictly decreasing by adjacent comparisons.
     *
     * Therefore, for each center c:
     *   keepLeft  = min(k + 1, length of increasing run ending at c)
     *   keepRight = min(k + 1, length of decreasing run starting at c)
     *   kept      = keepLeft + keepRight - 1
     *   changes   = (2k + 1) - kept
     *
     * We compute:
     * - incEnd[i] = length of the longest contiguous strictly increasing run ending at i
     * - decStart[i] = length of the longest contiguous strictly decreasing run starting at i
     *
     * Then scan all valid centers and take the minimum.
     *
     * @param prices the array of daily prices
     * @param k the exact radius of the centered peak
     * @return the minimum number of recolors required
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minimumRecolors(int[] prices, int k) {
        int n = prices.length;

        // incEnd[i] = how many consecutive elements ending at i form a strictly increasing chain.
        // Example:
        // prices = [1, 3, 5, 4]
        // incEnd = [1, 2, 3, 1]
        //
        // Why this matters:
        // For a center c, the left side must be strictly increasing all the way into c.
        // The only positions we can keep unchanged on the left are the final contiguous part
        // that already satisfies these adjacent "<" relations.
        int[] incEnd = new int[n];
        incEnd[0] = 1;

        for (int i = 1; i < n; i++) {
            // If the current adjacent pair already satisfies strict increase,
            // we extend the increasing run ending at i-1.
            if (prices[i - 1] < prices[i]) {
                incEnd[i] = incEnd[i - 1] + 1;
            } else {
                // Otherwise the increasing run must restart at this single element.
                incEnd[i] = 1;
            }
        }

        // decStart[i] = how many consecutive elements starting at i form a strictly decreasing chain.
        // Example:
        // prices = [5, 4, 2, 6]
        // decStart = [3, 2, 1, 1]
        //
        // Why this matters:
        // For a center c, the right side must be strictly decreasing away from c.
        // The only positions we can keep unchanged on the right are the initial contiguous part
        // that already satisfies these adjacent ">" relations.
        int[] decStart = new int[n];
        decStart[n - 1] = 1;

        for (int i = n - 2; i >= 0; i--) {
            // If prices[i] > prices[i+1], then the decreasing run starting at i
            // can extend through the run starting at i+1.
            if (prices[i] > prices[i + 1]) {
                decStart[i] = decStart[i + 1] + 1;
            } else {
                // Otherwise it restarts at length 1.
                decStart[i] = 1;
            }
        }

        int windowLength = 2 * k + 1;
        int answer = Integer.MAX_VALUE;

        // Every valid center must have at least k elements on both sides.
        for (int center = k; center <= n - k - 1; center++) {
            // Maximum number of unchanged positions we can keep on the left side including center.
            // We cannot keep more than k+1 positions because the window radius is exactly k.
            int keepLeft = Math.min(k + 1, incEnd[center]);

            // Maximum number of unchanged positions we can keep on the right side including center.
            int keepRight = Math.min(k + 1, decStart[center]);

            // Center is counted in both keepLeft and keepRight, so subtract 1 once.
            int kept = keepLeft + keepRight - 1;

            // Every other position in the chosen window must be recolored.
            int changes = windowLength - kept;

            answer = Math.min(answer, changes);
        }

        return answer;
    }

    /**
     * Convenience wrapper matching a common interview-style naming convention.
     *
     * @param prices the array of daily prices
     * @param k the exact radius of the centered peak
     * @return the minimum number of recolors required
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int solve(int[] prices, int k) {
        return minimumRecolors(prices, k);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] prices1 = {1, 3, 5, 4, 2, 6, 7};
        int k1 = 2;
        int result1 = solution.minimumRecolors(prices1, k1);
        System.out.println(result1); // Expected: 0

        int[] prices2 = {4, 4, 4, 4, 4, 4};
        int k2 = 2;
        int result2 = solution.minimumRecolors(prices2, k2);
        System.out.println(result2); // Expected: 4

        // Additional small sanity checks.
        int[] prices3 = {1, 2, 3};
        int k3 = 1;
        int result3 = solution.minimumRecolors(prices3, k3);
        System.out.println(result3); // Expected: 1 (need right side decreasing)

        int[] prices4 = {3, 2, 1};
        int k4 = 1;
        int result4 = solution.minimumRecolors(prices4, k4);
        System.out.println(result4); // Expected: 1 (need left side increasing)

        int[] prices5 = {1, 5, 2};
        int k5 = 1;
        int result5 = solution.minimumRecolors(prices5, k5);
        System.out.println(result5); // Expected: 0
    }
}