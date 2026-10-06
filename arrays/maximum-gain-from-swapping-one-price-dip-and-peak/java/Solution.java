import java.util.*;

/*
Title: Maximum Gain from Swapping One Price Dip and Peak
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array prices where prices[i] represents the price of an asset on day i.
You may perform at most one swap of two different elements in the array. After the swap, choose
a single buy day b and a later sell day s such that b < s. Your profit is prices[s] - prices[b].

Return the maximum profit you can achieve.

A swap is optional, and the buy/sell operation must happen after the array has been modified.
The swapped values remain in their new positions when selecting the buy and sell days.
If no profitable transaction is possible, return 0.

This problem is about reasoning over array positions, not sorting the array freely. Because only
one swap is allowed, a good solution must carefully evaluate how moving one low price earlier or
one high price later can improve the best possible transaction.

Constraints:
- 2 <= prices.length <= 2 * 10^5
- 0 <= prices[i] <= 10^9
- You may swap at most one pair of indices i and j where i != j

Example 1:
Input: prices = [8, 3, 6, 1, 9]
Output: 8
Explanation: Swap 8 and 1 to get [1, 3, 6, 8, 9]. Then buy on day 0 at 1 and sell on day 4 at 9
for profit 8. Without a swap, the best profit is 8 as well by buying at 1 and selling at 9, so
the answer remains 8.

Example 2:
Input: prices = [10, 7, 4, 6, 2]
Output: 5
Explanation: Without a swap, the best profit is 2 by buying at 4 and selling at 6.
If you swap 7 and 2, the array becomes [10, 2, 4, 6, 7]. Then buy on day 1 at 2 and sell on day 4
at 7 for profit 5. The correct output is 5.
*/

public class Solution {

    /**
     * Computes the maximum profit obtainable after performing at most one swap of two elements,
     * followed by exactly one buy-then-sell transaction with buy day strictly before sell day.
     *
     * Core idea:
     * 1. The final profit is always of the form finalPrices[s] - finalPrices[b] for some b < s.
     * 2. A single swap can affect:
     *    - the chosen buy position b,
     *    - the chosen sell position s,
     *    - or neither.
     * 3. For any final pair (b, s), the best way to maximize finalPrices[s] - finalPrices[b] is:
     *    - place the smallest value that can be moved into b using one swap,
     *    - and/or place the largest value that can be moved into s using one swap,
     *    while respecting that only one swap total is allowed.
     *
     * We split all possibilities into three exhaustive categories:
     *
     * A) No swap:
     *    Standard best single-transaction profit.
     *
     * B) Swap where one endpoint of the swap is the buy day b:
     *    We swap index b with some later index j > b.
     *    Then the value at b becomes some suffix value prices[j].
     *    To maximize profit for this fixed b, we want the minimum value in suffix (b+1..n-1),
     *    because that becomes the new buy price at b.
     *    After that swap, the best sell after b must be computed carefully:
     *      - if we swapped with the unique suffix maximum, that maximum disappears from its old place,
     *        so the best remaining sell may drop to the second suffix maximum;
     *      - otherwise the suffix maximum remains available.
     *
     * C) Swap where one endpoint of the swap is the sell day s:
     *    We swap index s with some earlier index i < s.
     *    Then the value at s becomes some prefix value prices[i].
     *    To maximize profit for this fixed s, we want the maximum value in prefix (0..s-1),
     *    because that becomes the new sell price at s.
     *    After that swap, the best buy before s must be computed carefully:
     *      - if we swapped away the unique prefix minimum, the best remaining buy may rise to the
     *        second prefix minimum;
     *      - otherwise the prefix minimum remains available.
     *
     * D) Swap where the two swapped indices are exactly the chosen buy and sell days (b, s):
     *    Then profit becomes prices[b] - prices[s].
     *    This is naturally covered by both B and C formulas, but we also compute it explicitly
     *    for clarity and completeness.
     *
     * All needed information can be precomputed in linear time:
     * - prefix minima and second minima with counts,
     * - prefix maxima and second maxima with counts,
     * - suffix minima and second minima with counts,
     * - suffix maxima and second maxima with counts.
     *
     * @param prices the array of daily prices
     * @return the maximum achievable profit after at most one swap; returns 0 if no profit is possible
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumProfit(int[] prices) {
        int n = prices.length;
        if (n < 2) {
            return 0L;
        }

        // ---------------------------------------------------------------------
        // Prefix minimum structures:
        // For each index i:
        //   prefixMinVal[i]   = smallest value in prices[0..i]
        //   prefixMinCnt[i]   = how many times that smallest value appears in prices[0..i]
        //   prefixSecondMin[i]= smallest value in prices[0..i] that is strictly greater than prefixMinVal[i]
        //
        // These arrays let us answer:
        // "If I remove one occurrence of the current minimum from the prefix, what is the new minimum?"
        // This is essential when the swap takes away the unique best buy value from the prefix.
        // ---------------------------------------------------------------------
        int[] prefixMinVal = new int[n];
        int[] prefixMinCnt = new int[n];
        int[] prefixSecondMin = new int[n];

        // ---------------------------------------------------------------------
        // Prefix maximum structures:
        // Similar logic, but for maxima.
        // Useful when we want to move the largest prefix value to a sell position.
        // ---------------------------------------------------------------------
        int[] prefixMaxVal = new int[n];
        int[] prefixMaxCnt = new int[n];
        int[] prefixSecondMax = new int[n];

        // ---------------------------------------------------------------------
        // Suffix minimum structures:
        // For each index i:
        //   suffixMinVal[i]   = smallest value in prices[i..n-1]
        //   suffixMinCnt[i]   = count of that smallest value in prices[i..n-1]
        //   suffixSecondMin[i]= second distinct minimum in prices[i..n-1]
        //
        // Useful when we want to move a cheap value earlier to become the buy.
        // ---------------------------------------------------------------------
        int[] suffixMinVal = new int[n];
        int[] suffixMinCnt = new int[n];
        int[] suffixSecondMin = new int[n];

        // ---------------------------------------------------------------------
        // Suffix maximum structures:
        // For each index i:
        //   suffixMaxVal[i]   = largest value in prices[i..n-1]
        //   suffixMaxCnt[i]   = count of that largest value in prices[i..n-1]
        //   suffixSecondMax[i]= second distinct maximum in prices[i..n-1]
        //
        // Useful when we need to know the best sell after a swap involving the buy day.
        // ---------------------------------------------------------------------
        int[] suffixMaxVal = new int[n];
        int[] suffixMaxCnt = new int[n];
        int[] suffixSecondMax = new int[n];

        buildPrefixMinInfo(prices, prefixMinVal, prefixMinCnt, prefixSecondMin);
        buildPrefixMaxInfo(prices, prefixMaxVal, prefixMaxCnt, prefixSecondMax);
        buildSuffixMinInfo(prices, suffixMinVal, suffixMinCnt, suffixSecondMin);
        buildSuffixMaxInfo(prices, suffixMaxVal, suffixMaxCnt, suffixSecondMax);

        long answer = 0L;

        // ---------------------------------------------------------------------
        // Case A: no swap at all.
        // This is the classic "best time to buy and sell stock" one-transaction problem.
        // We scan from left to right, maintaining the smallest price seen so far.
        // ---------------------------------------------------------------------
        int minSoFar = prices[0];
        for (int i = 1; i < n; i++) {
            answer = Math.max(answer, (long) prices[i] - minSoFar);
            minSoFar = Math.min(minSoFar, prices[i]);
        }

        // ---------------------------------------------------------------------
        // Case B: the swap uses the buy day b.
        //
        // We fix b. We swap prices[b] with some j > b.
        // To maximize profit, the value moved into b should be the minimum value in suffix (b+1..n-1).
        //
        // Let movedBuy = suffixMinVal[b+1].
        //
        // After the swap, what is the best sell after b?
        // - Normally it is the maximum in suffix (b+1..n-1).
        // - But if the chosen j was the unique occurrence of that suffix maximum, then moving it to b
        //   removes it from the suffix, and the best remaining sell becomes the second suffix maximum.
        //
        // There are two subcases:
        // 1) We swap with an index holding the suffix minimum that is NOT the unique suffix maximum.
        //    Then best sell remains suffix maximum.
        // 2) The chosen swapped index is forced to be the unique suffix maximum because the suffix minimum
        //    and suffix maximum are the same value and appear exactly once (single-element or all same).
        //    Then best sell becomes second suffix maximum.
        //
        // Instead of trying to identify the exact index, we can reason by values and counts.
        // ---------------------------------------------------------------------
        for (int b = 0; b <= n - 2; b++) {
            int l = b + 1;

            int movedBuy = suffixMinVal[l];
            int suffixMax = suffixMaxVal[l];
            int suffixMaxCount = suffixMaxCnt[l];
            int suffixSecond = suffixSecondMax[l];

            // Subcase B1:
            // We can keep the suffix maximum available after the swap if there exists at least one index
            // with the chosen minimum value that is not the unique maximum being removed.
            //
            // This is possible whenever:
            // - movedBuy < suffixMax: minimum and maximum are different values, so swapping a minimum
            //   does not remove the maximum.
            // - OR movedBuy == suffixMax and suffixMaxCount >= 2: all suffix values equal to same value,
            //   but removing one still leaves another maximum.
            if (movedBuy < suffixMax || (movedBuy == suffixMax && suffixMaxCount >= 2)) {
                answer = Math.max(answer, (long) suffixMax - movedBuy);
            }

            // Subcase B2:
            // If the swapped-away element is the unique suffix maximum, then the best remaining sell
            // becomes suffixSecond.
            //
            // This only matters if suffixSecond exists.
            if (suffixSecond != Integer.MIN_VALUE) {
                answer = Math.max(answer, (long) suffixSecond - movedBuy);
            }
        }

        // ---------------------------------------------------------------------
        // Case C: the swap uses the sell day s.
        //
        // We fix s. We swap prices[s] with some i < s.
        // To maximize profit, the value moved into s should be the maximum value in prefix (0..s-1).
        //
        // Let movedSell = prefixMaxVal[s-1].
        //
        // After the swap, what is the best buy before s?
        // - Normally it is the minimum in prefix (0..s-1).
        // - But if we swapped away the unique prefix minimum, then the best remaining buy becomes
        //   the second prefix minimum.
        //
        // Again, we handle both possibilities using values and counts.
        // ---------------------------------------------------------------------
        for (int s = 1; s < n; s++) {
            int r = s - 1;

            int movedSell = prefixMaxVal[r];
            int prefixMin = prefixMinVal[r];
            int prefixMinCount = prefixMinCnt[r];
            int prefixSecond = prefixSecondMin[r];

            // Subcase C1:
            // We can keep the prefix minimum available after the swap if:
            // - movedSell > prefixMin: maximum and minimum are different values, so moving a maximum
            //   does not remove the minimum.
            // - OR movedSell == prefixMin and prefixMinCount >= 2: all equal or repeated same value,
            //   removing one still leaves another minimum.
            if (movedSell > prefixMin || (movedSell == prefixMin && prefixMinCount >= 2)) {
                answer = Math.max(answer, (long) movedSell - prefixMin);
            }

            // Subcase C2:
            // If the swapped-away element is the unique prefix minimum, then the best remaining buy
            // becomes prefixSecond.
            if (prefixSecond != Integer.MAX_VALUE) {
                answer = Math.max(answer, (long) movedSell - prefixSecond);
            }
        }

        // ---------------------------------------------------------------------
        // Case D: swap exactly the chosen buy and sell positions (b, s), with b < s.
        // Then after swapping, buy at b and sell at s gives:
        //   prices[b] becomes old prices[s]
        //   prices[s] becomes old prices[b]
        // Profit = old prices[b] - old prices[s]
        //
        // This case is already implicitly covered by the previous loops, but computing it explicitly
        // is simple and makes the solution easier to reason about.
        //
        // We need the maximum value of prices[b] - prices[s] over b < s.
        // That is equivalent to scanning from left to right while keeping the maximum prefix value.
        // ---------------------------------------------------------------------
        int maxSoFar = prices[0];
        for (int s = 1; s < n; s++) {
            answer = Math.max(answer, (long) maxSoFar - prices[s]);
            maxSoFar = Math.max(maxSoFar, prices[s]);
        }

        return Math.max(0L, answer);
    }

    /**
     * Builds prefix minimum information:
     * - minimum value so far
     * - count of that minimum
     * - second distinct minimum
     *
     * @param prices input prices array
     * @param minVal output array storing prefix minimum values
     * @param minCnt output array storing counts of prefix minimum values
     * @param secondMin output array storing second distinct prefix minimum values
     * @return nothing; results are written into the provided arrays
     * Time complexity: O(n)
     * Space complexity: O(1) extra beyond output arrays
     */
    public void buildPrefixMinInfo(int[] prices, int[] minVal, int[] minCnt, int[] secondMin) {
        int best = Integer.MAX_VALUE;
        int countBest = 0;
        int second = Integer.MAX_VALUE;

        for (int i = 0; i < prices.length; i++) {
            int x = prices[i];

            if (x < best) {
                second = best;
                best = x;
                countBest = 1;
            } else if (x == best) {
                countBest++;
            } else if (x < second) {
                second = x;
            }

            minVal[i] = best;
            minCnt[i] = countBest;
            secondMin[i] = second;
        }
    }

    /**
     * Builds prefix maximum information:
     * - maximum value so far
     * - count of that maximum
     * - second distinct maximum
     *
     * @param prices input prices array
     * @param maxVal output array storing prefix maximum values
     * @param maxCnt output array storing counts of prefix maximum values
     * @param secondMax output array storing second distinct prefix maximum values
     * @return nothing; results are written into the provided arrays
     * Time complexity: O(n)
     * Space complexity: O(1) extra beyond output arrays
     */
    public void buildPrefixMaxInfo(int[] prices, int[] maxVal, int[] maxCnt, int[] secondMax) {
        int best = Integer.MIN_VALUE;
        int countBest = 0;
        int second = Integer.MIN_VALUE;

        for (int i = 0; i < prices.length; i++) {
            int x = prices[i];

            if (x > best) {
                second = best;
                best = x;
                countBest = 1;
            } else if (x == best) {
                countBest++;
            } else if (x > second) {
                second = x;
            }

            maxVal[i] = best;
            maxCnt[i] = countBest;
            secondMax[i] = second;
        }
    }

    /**
     * Builds suffix minimum information:
     * - minimum value from current index to end
     * - count of that minimum
     * - second distinct minimum
     *
     * @param prices input prices array
     * @param minVal output array storing suffix minimum values
     * @param minCnt output array storing counts of suffix minimum values
     * @param secondMin output array storing second distinct suffix minimum values
     * @return nothing; results are written into the provided arrays
     * Time complexity: O(n)
     * Space complexity: O(1) extra beyond output arrays
     */
    public void buildSuffixMinInfo(int[] prices, int[] minVal, int[] minCnt, int[] secondMin) {
        int best = Integer.MAX_VALUE;
        int countBest = 0;
        int second = Integer.MAX_VALUE;

        for (int i = prices.length - 1; i >= 0; i--) {
            int x = prices[i];

            if (x < best) {
                second = best;
                best = x;
                countBest = 1;
            } else if (x == best) {
                countBest++;
            } else if (x < second) {
                second = x;
            }

            minVal[i] = best;
            minCnt[i] = countBest;
            secondMin[i] = second;
        }
    }

    /**
     * Builds suffix maximum information:
     * - maximum value from current index to end
     * - count of that maximum
     * - second distinct maximum
     *
     * @param prices input prices array
     * @param maxVal output array storing suffix maximum values
     * @param maxCnt output array storing counts of suffix maximum values
     * @param secondMax