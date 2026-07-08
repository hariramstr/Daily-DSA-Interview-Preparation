/*
Title: Maximum Uniform Banner Width for Ad Slots
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are given an array `slots` where `slots[i]` is the width of the `i`-th advertising space available on a website.
A design team wants to create banner creatives of one uniform integer width `w`, and each slot can be split into
multiple banners as long as every produced banner has width exactly `w`. Any leftover width in a slot is discarded
and cannot be combined with leftover width from another slot.

Given an integer `k`, return the maximum possible integer banner width `w` such that the total number of banners
produced across all slots is at least `k`. If it is impossible to produce `k` banners even with width `1`, return `0`.

Formally, for a chosen width `w`, slot `i` contributes `floor(slots[i] / w)` banners. You must find the largest `w`
for which the sum of these values over all slots is at least `k`.

This problem is intended to be solved efficiently for large inputs. A linear scan over all possible widths will be
too slow when slot widths are large, so you should take advantage of the monotonic relationship between banner width
and the number of banners that can be produced.

Constraints:
- 1 <= slots.length <= 100000
- 1 <= slots[i] <= 1000000000
- 1 <= k <= 1000000000

Example 1:
Input: slots = [9, 7, 5], k = 5
Output: 3
Explanation:
- With width 3, the slots produce:
  9 / 3 = 3
  7 / 3 = 2
  5 / 3 = 1
  Total = 6, which is enough.
- With width 4, the slots produce:
  9 / 4 = 2
  7 / 4 = 1
  5 / 4 = 1
  Total = 4, which is not enough.
So the maximum valid width is 3.

Example 2:
Input: slots = [2, 3], k = 10
Output: 0
Explanation:
- Even with width 1, the total number of banners is:
  2 / 1 + 3 / 1 = 2 + 3 = 5
- 5 < 10, so it is impossible to produce 10 banners.

Correctness check for the examples:
- Example 1 returns 3.
- Example 2 returns 0.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n * log M)
    - n is the number of slots
    - M is the maximum slot width
    We binary search over possible banner widths, and for each candidate width
    we scan the array once to count how many banners can be produced.

    Space Complexity: O(1)
    - We only use a few extra variables.
    */

    public int MaxUniformBannerWidth(int[] slots, int k)
    {
        // Step 1:
        // We first determine the largest slot width.
        // Why?
        // Because the answer can never be larger than the biggest slot.
        // If the widest slot has width 9, then a banner width of 10 is impossible.
        int maxWidth = 0;

        // We also compute the total width sum using long.
        // Why long?
        // Because slot widths can be large, and there can be many slots.
        // Using int for the sum could overflow.
        long totalWidth = 0;

        foreach (int slot in slots)
        {
            if (slot > maxWidth)
            {
                maxWidth = slot;
            }

            totalWidth += slot;
        }

        // Step 2:
        // Quick impossibility check.
        // If banner width is 1, each slot contributes exactly slot[i] banners.
        // So the maximum possible number of banners overall is the sum of all widths.
        // If even that is less than k, then there is no valid answer.
        if (totalWidth < k)
        {
            return 0;
        }

        // Step 3:
        // Set up binary search boundaries.
        //
        // We search for the largest width w such that:
        // total banners produced with width w >= k
        //
        // The search space is [1, maxWidth].
        int left = 1;
        int right = maxWidth;

        // This variable stores the best valid width found so far.
        // We start with 0, which is also the correct answer if no valid width exists.
        int answer = 0;

        // Step 4:
        // Standard binary search on the answer space.
        //
        // Why binary search works:
        // - If a width w is feasible (can produce at least k banners),
        //   then any smaller width is also feasible, because smaller banners
        //   allow us to cut at least as many pieces.
        // - If a width w is not feasible, then any larger width is also not feasible.
        //
        // This "true...true...false...false" pattern is exactly what binary search needs.
        while (left <= right)
        {
            // Compute the middle width safely.
            int mid = left + (right - left) / 2;

            // Step 5:
            // Count how many banners can be produced if every banner has width = mid.
            //
            // We use long for the count because the total number of banners can be large.
            long bannersProduced = 0;

            foreach (int slot in slots)
            {
                // Integer division automatically gives floor(slot / mid),
                // which is exactly what the problem requires.
                bannersProduced += slot / mid;

                // Small optimization:
                // If we already reached or exceeded k, we can stop early.
                // Why?
                // Because for feasibility, we only care whether the count is at least k.
                if (bannersProduced >= k)
                {
                    break;
                }
            }

            // Step 6:
            // Decide which half of the search space to keep.
            if (bannersProduced >= k)
            {
                // Current width mid is valid.
                // That means:
                // - mid is a candidate answer
                // - but maybe there is an even larger valid width
                answer = mid;

                // So we move right to search for a bigger feasible width.
                left = mid + 1;
            }
            else
            {
                // Current width mid is too large and does not produce enough banners.
                // Therefore, any width larger than mid will also fail.
                // So we discard the right half and search smaller widths.
                right = mid - 1;
            }
        }

        // Step 7:
        // After binary search finishes, answer holds the largest feasible width.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] slots1 = { 9, 7, 5 };
int k1 = 5;
int result1 = solution.MaxUniformBannerWidth(slots1, k1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] slots2 = { 2, 3 };
int k2 = 10;
int result2 = solution.MaxUniformBannerWidth(slots2, k2);
Console.WriteLine(result2); // Expected: 0

// Additional demo
int[] slots3 = { 8, 8, 8 };
int k3 = 6;
int result3 = solution.MaxUniformBannerWidth(slots3, k3);
Console.WriteLine(result3); // Expected: 4