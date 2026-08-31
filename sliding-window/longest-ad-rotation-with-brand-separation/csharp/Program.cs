/*
Title: Longest Ad Rotation With Brand Separation
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array brands where brands[i] is the brand ID of the i-th advertisement shown in a video stream, in chronological order.
A stream segment is considered valid if, for every brand that appears in that segment, any two consecutive ads from the same brand
inside the segment are at least gap + 1 positions apart. In other words, if a brand appears multiple times in the chosen contiguous
segment, there must be at least gap other ads between its repeated appearances.

Your task is to return the length of the longest valid contiguous segment.

This models an ad-serving system that wants to avoid showing the same brand too frequently while still analyzing the longest
uninterrupted portion of a schedule that obeys the cooldown rule.

A segment of length 1 is always valid. If gap = 0, then every segment is valid because repeated brands may be adjacent.

Constraints:
- 1 <= brands.length <= 200000
- 1 <= brands[i] <= 1000000000
- 0 <= gap <= brands.length

Example 1:
Input: brands = [4, 1, 2, 4, 3, 1, 5], gap = 2
Output: 7

Explanation:
In the full segment, repeated brand 4 appears at indices 0 and 3, with 2 ads between them,
and repeated brand 1 appears at indices 1 and 5, with 3 ads between them.
All repeats satisfy the rule, so the whole array is valid.

Example 2:
Input: brands = [7, 2, 7, 3, 4, 7], gap = 2
Output: 4

Explanation:
The full array is invalid because the first two occurrences of brand 7 are only 1 ad apart.
One longest valid segment is [7, 3, 4, 7], whose repeated 7s have 2 ads between them, so its length is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan the array once from left to right.
    - Each index is processed in constant expected time using a dictionary.

    Space Complexity: O(n)
    - In the worst case, all brand IDs are distinct, so the dictionary stores up to n entries.

    Core idea:
    Use a sliding window [left..right] that is always valid.
    For each brand, store its most recent index.
    When we see the same brand again too soon, we move the left boundary forward enough
    to remove the earlier conflicting occurrence from the window.
    */
    public int LongestValidSegment(int[] brands, int gap)
    {
        // This dictionary maps:
        // brand ID -> most recent index where this brand appeared
        //
        // Why do we need this?
        // Because when we are at position "right", the only possible new violation
        // introduced by adding brands[right] is with the previous occurrence of the same brand.
        // We do NOT need to compare against every earlier element in the window.
        // The most recent occurrence is enough because consecutive repeated appearances
        // are the ones that matter for the rule.
        var lastSeenIndex = new Dictionary<int, int>();

        // "left" is the start of our current sliding window.
        // We will maintain the invariant:
        // the subarray brands[left..right] is always valid after each iteration.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int best = 0;

        // Expand the window one element at a time by moving "right" from left to right.
        for (int right = 0; right < brands.Length; right++)
        {
            int currentBrand = brands[right];

            // Step 1: Check whether this brand has appeared before.
            //
            // If it has, we need to see whether the previous occurrence is too close
            // to the current position according to the cooldown rule.
            if (lastSeenIndex.TryGetValue(currentBrand, out int previousIndex))
            {
                // The number of ads BETWEEN previousIndex and right is:
                // right - previousIndex - 1
                //
                // The rule says there must be at least "gap" ads between repeated appearances.
                // So a violation happens when:
                // right - previousIndex - 1 < gap
                //
                // Rearranging:
                // right - previousIndex <= gap
                //
                // If that happens AND the previous occurrence is still inside the current window,
                // then the current window would become invalid after including brands[right].
                //
                // Instead of shrinking one step at a time, we can jump "left" directly to
                // previousIndex + 1, which removes that conflicting earlier occurrence.
                //
                // We use Math.Max because "left" should never move backward.
                if (right - previousIndex <= gap)
                {
                    left = Math.Max(left, previousIndex + 1);
                }
            }

            // Step 2: Update the most recent position of the current brand.
            //
            // This must happen AFTER the conflict check above, because we still needed
            // the old previous index to decide whether there was a violation.
            lastSeenIndex[currentBrand] = right;

            // Step 3: Compute the current valid window length.
            //
            // Since we maintained the invariant that brands[left..right] is valid,
            // its length is:
            int currentLength = right - left + 1;

            // Step 4: Update the best answer seen so far.
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
int[] brands1 = { 4, 1, 2, 4, 3, 1, 5 };
int gap1 = 2;
int result1 = solution.LongestValidSegment(brands1, gap1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 7

// Example 2
int[] brands2 = { 7, 2, 7, 3, 4, 7 };
int gap2 = 2;
int result2 = solution.LongestValidSegment(brands2, gap2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick checks

// If gap = 0, every segment is valid, so answer should be full length.
int[] brands3 = { 1, 1, 1, 1 };
int gap3 = 0;
int result3 = solution.LongestValidSegment(brands3, gap3);
Console.WriteLine("Additional Check 1 Result: " + result3); // Expected: 4

// Repeats too close force the window to move.
int[] brands4 = { 5, 6, 5, 6, 5 };
int gap4 = 2;
int result4 = solution.LongestValidSegment(brands4, gap4);
Console.WriteLine("Additional Check 2 Result: " + result4); // Expected: 3