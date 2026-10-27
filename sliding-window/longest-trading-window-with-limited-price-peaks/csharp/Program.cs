/*
Title: Longest Trading Window With Limited Price Peaks
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an integer array prices where prices[i] is the stock price observed at minute i, and an integer k.
A minute i is called a peak inside a contiguous window [l, r] if l < i < r and prices[i] is strictly greater than
both of its immediate neighbors within that window, meaning prices[i] > prices[i - 1] and prices[i] > prices[i + 1].

Your task is to find the length of the longest contiguous window such that the number of peak minutes inside that
window is at most k.

Be careful: whether an index is a peak depends on the current window boundaries. For example, an index cannot be a
peak if it is the leftmost or rightmost element of the window, because it would be missing one neighbor inside the
window. This boundary dependence makes the problem harder than simply precomputing all peaks once.

Return the maximum possible window length.

Constraints:
- 1 <= prices.length <= 200000
- 0 <= prices[i] <= 1000000000
- 0 <= k <= prices.length

Example 1:
Input: prices = [5, 1, 4, 2, 6, 3, 7], k = 2
Output: 6
Explanation: The window [5, 1, 4, 2, 6, 3] has internal peaks at values 4 and 6, so it contains exactly 2 peaks
and its length is 6. The full array has 3 peaks (4, 6, and 7 is not a peak because it is at the boundary), so it
is not valid.

Example 2:
Input: prices = [2, 9, 1, 8, 3, 7, 4], k = 1
Output: 4
Explanation: One valid longest window is [9, 1, 8, 3], whose only internal peak is 8. Any window of length 5 or
more contains at least 2 internal peaks.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Core idea:
    A position i is a peak for a window [l, r] exactly when:
    1) i is a "global local peak" in the original array, meaning prices[i] > prices[i - 1] and prices[i] > prices[i + 1]
    2) i is strictly inside the window, so l < i < r

    Why this works:
    The neighbors used by the window are still the original adjacent elements at i - 1 and i + 1.
    The only boundary-dependent part is whether i is allowed to be considered at all.
    So for any window [l, r], the number of peaks inside it is simply the number of globally precomputed peak indices
    that lie in the open interval (l, r).

    That transforms the problem into:
    Find the longest window [l, r] such that the count of precomputed peak indices in (l, r) is at most k.

    We can maintain this count with a sliding window:
    - When we extend r to the right, the only new index that can become an interior point is r - 1.
      So if r - 1 is a global peak, peakCount increases by 1.
    - When we move l to the right, the old left boundary index l stops excluding l + 1 from being interior.
      More directly, the only index that leaves the interior is l + 1.
      So if l + 1 is a global peak, peakCount decreases by 1.
    */
    public int LongestTradingWindowWithLimitedPeaks(int[] prices, int k)
    {
        int n = prices.Length;

        // If the array has fewer than 3 elements, no window can contain an internal peak,
        // because a peak must have both a left and a right neighbor inside the window.
        // Therefore the entire array is always valid.
        if (n < 3)
        {
            return n;
        }

        // isPeak[i] tells us whether index i is a local peak in the original array.
        // We only need to check indices 1 through n - 2, because the first and last
        // elements can never be peaks in the original array due to missing neighbors.
        bool[] isPeak = new bool[n];

        // Precompute all global local peaks.
        // This is safe because the actual comparison values for a candidate peak i
        // are always prices[i - 1] and prices[i + 1]. Those do not change with the window.
        // The only thing the window changes is whether i is interior.
        for (int i = 1; i <= n - 2; i++)
        {
            isPeak[i] = prices[i] > prices[i - 1] && prices[i] > prices[i + 1];
        }

        int left = 0;
        int peakCount = 0;
        int best = 1;

        // We expand the window by moving 'right' from left to right.
        for (int right = 0; right < n; right++)
        {
            // Step 1: incorporate the effect of adding prices[right] to the window.
            //
            // Before adding right, the window was [left, right - 1].
            // After adding right, the new window is [left, right].
            //
            // Which index newly becomes an interior index because of this expansion?
            // Exactly right - 1.
            //
            // Why?
            // - In [left, right - 1], index right - 1 was the right boundary, so it could not be a peak.
            // - In [left, right], index right - 1 is no longer the boundary; it now has both neighbors inside.
            //
            // Therefore, if right - 1 is a global peak, it now starts contributing to the window's peak count.
            if (right - 1 >= 0 && isPeak[right - 1])
            {
                peakCount++;
            }

            // Step 2: if the window has too many peaks, shrink it from the left
            // until it becomes valid again.
            //
            // The window is valid only when peakCount <= k.
            while (peakCount > k)
            {
                // We are about to move left from 'left' to 'left + 1'.
                //
                // Which interior index stops being interior because of this move?
                // Exactly left + 1.
                //
                // Why?
                // - In [left, right], index left + 1 is interior (assuming it exists inside the window).
                // - In [left + 1, right], index left + 1 becomes the new left boundary,
                //   so it can no longer be counted as a peak.
                //
                // Therefore, if left + 1 is a global peak, it must be removed from peakCount.
                if (left + 1 < n && isPeak[left + 1])
                {
                    peakCount--;
                }

                left++;
            }

            // Step 3: now the current window [left, right] is valid.
            // Update the best answer with its length.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] prices1 = { 5, 1, 4, 2, 6, 3, 7 };
int k1 = 2;
int result1 = solution.LongestTradingWindowWithLimitedPeaks(prices1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 6

// Example 2
int[] prices2 = { 2, 9, 1, 8, 3, 7, 4 };
int k2 = 1;
int result2 = solution.LongestTradingWindowWithLimitedPeaks(prices2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 4

// Additional quick checks
int[] prices3 = { 1 };
int k3 = 0;
Console.WriteLine($"Additional Check 1: {solution.LongestTradingWindowWithLimitedPeaks(prices3, k3)}"); // Expected: 1

int[] prices4 = { 1, 3, 1 };
int k4 = 0;
Console.WriteLine($"Additional Check 2: {solution.LongestTradingWindowWithLimitedPeaks(prices4, k4)}"); // Expected: 2

int[] prices5 = { 1, 3, 1 };
int k5 = 1;
Console.WriteLine($"Additional Check 3: {solution.LongestTradingWindowWithLimitedPeaks(prices5, k5)}"); // Expected: 3