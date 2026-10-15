/*
Title: Count Compatible Ski Lift Pairs

Problem Description:
A ski resort records the heights of guests waiting in a single line. For safety, the resort wants to place some guests together on a two-person lift seat, but only if the difference between their heights is at most maxGap centimeters.

You are given an integer array heights where heights[i] is the height of the ith guest, and an integer maxGap.

Your task is to count how many distinct pairs of guests (i, j) with i < j can safely share a lift seat, meaning:
|heights[i] - heights[j]| <= maxGap

The array is not guaranteed to be sorted. An efficient solution is expected for large inputs, so a quadratic scan over all pairs will be too slow. Sorting plus a two-pointers strategy allows us to count valid pairs efficiently.

Constraints:
- 1 <= heights.length <= 2 * 10^5
- 1 <= heights[i] <= 10^9
- 0 <= maxGap <= 10^9
- The answer may be large, so use a 64-bit integer type.

Important correctness note about the examples:
The first example text in the prompt contains contradictory explanation lines.
For heights = [150, 160, 153, 168], maxGap = 5:
Sorted heights = [150, 153, 160, 168]
Valid pairs are only (150, 153), so the correct answer is 1.

For the second example:
heights = [172, 175, 171, 169, 174], maxGap = 3
Sorted heights = [169, 171, 172, 174, 175]
All valid pairs are:
(169,171), (169,172), (171,172), (171,174), (172,174), (172,175), (174,175)
That is 7 pairs, not 8.
So the mathematically correct answer for example 2 is 7.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, extra algorithmic space is O(1)
      (ignoring the internal stack usage of the sorting implementation)
    */
    public long CountCompatiblePairs(int[] heights, int maxGap)
    {
        // If the array has fewer than 2 guests, no pair can be formed.
        // This is a simple edge case check that avoids unnecessary work.
        if (heights == null || heights.Length < 2)
        {
            return 0L;
        }

        // Step 1: Sort the heights.
        //
        // Why sorting helps:
        // In an unsorted array, checking whether many pairs satisfy the gap condition
        // is difficult without comparing many combinations.
        //
        // After sorting:
        // - For any indices left < right, heights[right] >= heights[left]
        // - Therefore, the absolute difference becomes:
        //   |heights[right] - heights[left]| = heights[right] - heights[left]
        //
        // This removes the need to consider both directions and allows us to use
        // a sliding window / two-pointer approach efficiently.
        Array.Sort(heights);

        // This variable will store the total number of valid pairs.
        // We use long because the number of pairs can be very large.
        // For example, if all 200,000 heights are compatible with each other,
        // the number of pairs is n * (n - 1) / 2, which does not fit in int.
        long totalPairs = 0L;

        // The "left" pointer marks the beginning of the current valid window.
        //
        // The idea of the window:
        // For each "right" index, we want the smallest left such that:
        // heights[right] - heights[left] <= maxGap
        //
        // Then every index from left to right - 1 forms a valid pair with right,
        // because the array is sorted.
        int left = 0;

        // Step 2: Move the "right" pointer from left to right across the array.
        //
        // At each position:
        // - We adjust "left" until the window satisfies the condition.
        // - Then we count how many earlier elements in the window can pair with right.
        for (int right = 0; right < heights.Length; right++)
        {
            // Step 2a: Shrink the window from the left while it is invalid.
            //
            // Current invalid condition:
            // heights[right] - heights[left] > maxGap
            //
            // Why this works:
            // Since the array is sorted, if the smallest element in the window
            // is too far from heights[right], then that element cannot pair with right.
            // So we move left forward to try a larger value and reduce the difference.
            //
            // We keep doing this until the window becomes valid again.
            while (left < right && (long)heights[right] - heights[left] > maxGap)
            {
                left++;
            }

            // Step 2b: Count valid pairs ending at "right".
            //
            // After the while loop, every index i in [left, right - 1] satisfies:
            // heights[right] - heights[i] <= maxGap
            //
            // Because the array is sorted:
            // - heights[i] >= heights[left] for all i >= left
            // - So if heights[right] - heights[left] <= maxGap,
            //   then heights[right] - heights[i] <= maxGap as well
            //
            // Therefore, the number of valid pairs with "right" as the second element is:
            // right - left
            //
            // Example:
            // If left = 2 and right = 5, then valid earlier indices are 2, 3, 4
            // which is exactly 5 - 2 = 3 elements.
            totalPairs += right - left;
        }

        // Step 3: Return the total number of valid pairs found.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Sample 1 from the prompt.
// The prompt's stated output says 2, but its own explanation contradicts that.
// Correct math:
// heights = [150, 160, 153, 168], maxGap = 5
// Sorted = [150, 153, 160, 168]
// Valid pairs:
// (150,153) -> gap 3
// (150,160) -> gap 10 invalid
// (150,168) -> gap 18 invalid
// (153,160) -> gap 7 invalid
// (153,168) -> gap 15 invalid
// (160,168) -> gap 8 invalid
// Total = 1
int[] heights1 = { 150, 160, 153, 168 };
int maxGap1 = 5;
long result1 = solution.CountCompatiblePairs(heights1, maxGap1);
Console.WriteLine($"Example 1 result: {result1}");

// Sample 2 from the prompt.
// The prompt says 8, but the listed valid pairs total only 7, and the correct count is 7.
// heights = [172, 175, 171, 169, 174], maxGap = 3
// Sorted = [169, 171, 172, 174, 175]
// Valid pairs:
// (169,171), (169,172), (171,172), (171,174), (172,174), (172,175), (174,175)
// Total = 7
int[] heights2 = { 172, 175, 171, 169, 174 };
int maxGap2 = 3;
long result2 = solution.CountCompatiblePairs(heights2, maxGap2);
Console.WriteLine($"Example 2 result: {result2}");

// Additional demo cases for learning and verification.

// All equal heights: every pair is valid.
int[] heights3 = { 160, 160, 160, 160 };
int maxGap3 = 0;
long result3 = solution.CountCompatiblePairs(heights3, maxGap3);
Console.WriteLine($"All equal heights result: {result3}");

// No valid pairs.
int[] heights4 = { 100, 200, 300, 400 };
int maxGap4 = 10;
long result4 = solution.CountCompatiblePairs(heights4, maxGap4);
Console.WriteLine($"No valid pairs result: {result4}");

// Mixed case.
int[] heights5 = { 155, 158, 162, 163, 170 };
int maxGap5 = 4;
long result5 = solution.CountCompatiblePairs(heights5, maxGap5);
Console.WriteLine($"Mixed case result: {result5}");