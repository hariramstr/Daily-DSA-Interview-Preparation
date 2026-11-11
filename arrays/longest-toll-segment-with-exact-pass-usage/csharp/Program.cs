/*
Title: Longest Toll Segment With Exact Pass Usage

Problem Description:
You are given an integer array `roads` where `roads[i]` is the toll cost of the `i`-th road segment on a highway trip.
You also have a special pass that can be used to make any segment with toll cost less than or equal to `passLimit` free.
However, the pass must be used on exactly `k` segments within one contiguous part of the trip.

Your task is to find the length of the longest contiguous subarray such that within that subarray,
there are exactly `k` elements whose value is less than or equal to `passLimit`.

Return the maximum possible length of such a subarray. If no valid subarray exists, return `0`.

This problem models finding the longest stretch of a route where your pass is used an exact number of times.
Note that the pass can only be applied to segments that qualify, and every qualifying segment inside the chosen subarray
counts toward the total. Segments with toll cost greater than `passLimit` do not use the pass.

Constraints:
- 1 <= roads.length <= 100000
- 0 <= roads[i] <= 1000000000
- 0 <= passLimit <= 1000000000
- 0 <= k <= roads.length

Examples:
1)
Input: roads = [8, 2, 5, 1, 9, 3, 7], passLimit = 3, k = 2
Output: 5
Explanation:
Qualifying values are those <= 3, so the qualifying positions contain 2, 1, and 3.
A longest valid subarray is [8, 2, 5, 1, 9], which contains exactly two qualifying values: 2 and 1.
Its length is 5.

2)
Input: roads = [4, 6, 8, 10], passLimit = 5, k = 1
Output: 4
Explanation:
Only 4 is <= 5, and the whole array [4, 6, 8, 10] contains exactly one qualifying value.
So the answer is 4.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(m), where m is the number of qualifying elements (at most n)

    Idea:
    Convert the problem into:
    "Find the longest subarray containing exactly k 'good' elements",
    where a 'good' element is one with value <= passLimit.

    Key observation:
    If we store the indices of all qualifying elements, then:
    - For k > 0, every valid subarray with exactly k qualifying elements must include
      some consecutive block of exactly k qualifying indices.
    - Once we choose those k qualifying indices, we can expand:
        left  up to just after the previous qualifying index
        right up to just before the next qualifying index
      without changing the count of qualifying elements.
    That gives the maximum subarray length for that chosen block of k qualifying elements.

    Special case:
    - If k == 0, then we need the longest contiguous subarray containing no qualifying elements at all.
      That means we want the longest run of values > passLimit.
    */
    public int LongestTollSegmentWithExactPassUsage(int[] roads, int passLimit, int k)
    {
        int n = roads.Length;

        // Special handling for k == 0.
        // We want the longest contiguous segment with zero qualifying values.
        // In other words, every value in that segment must be > passLimit.
        if (k == 0)
        {
            int longest = 0;
            int currentRun = 0;

            // Scan through the array and count consecutive non-qualifying values.
            for (int i = 0; i < n; i++)
            {
                // If this road does NOT qualify for the pass, it can belong to a valid k=0 segment.
                if (roads[i] > passLimit)
                {
                    currentRun++;
                    if (currentRun > longest)
                    {
                        longest = currentRun;
                    }
                }
                else
                {
                    // This road qualifies for the pass, so any k=0 segment must stop before it.
                    currentRun = 0;
                }
            }

            return longest;
        }

        // Store the indices of all qualifying elements:
        // those are exactly the positions where roads[i] <= passLimit.
        // We use a list because we only care about the positions of qualifying values,
        // not the actual values themselves.
        List<int> qualifyingIndices = new List<int>();

        for (int i = 0; i < n; i++)
        {
            // This road segment qualifies for pass usage.
            // Its position matters because valid subarrays are defined by how many such positions they include.
            if (roads[i] <= passLimit)
            {
                qualifyingIndices.Add(i);
            }
        }

        // If there are fewer than k qualifying elements in the entire array,
        // then it is impossible to form any subarray containing exactly k of them.
        if (qualifyingIndices.Count < k)
        {
            return 0;
        }

        int answer = 0;

        // We now examine every consecutive group of exactly k qualifying indices.
        //
        // Suppose the qualifying indices are:
        // q[0], q[1], q[2], ..., q[m-1]
        //
        // For each window q[i] ... q[i + k - 1]:
        // - These are the exact k qualifying elements that must be inside our chosen subarray.
        // - To avoid including an extra qualifying element on the left,
        //   the subarray must start after q[i - 1] (or at index 0 if i == 0).
        // - To avoid including an extra qualifying element on the right,
        //   the subarray must end before q[i + k] (or at index n - 1 if this window reaches the end).
        //
        // Therefore:
        // leftBoundary  = (i == 0) ? 0 : q[i - 1] + 1
        // rightBoundary = (i + k - 1 == m - 1) ? n - 1 : q[i + k] - 1
        //
        // The full range [leftBoundary, rightBoundary] contains exactly these k qualifying elements
        // and is the longest possible subarray for this chosen block.
        for (int i = 0; i + k - 1 < qualifyingIndices.Count; i++)
        {
            // Determine the farthest valid starting index.
            int leftBoundary;
            if (i == 0)
            {
                // There is no qualifying element before this block,
                // so we can start all the way from the beginning of the array.
                leftBoundary = 0;
            }
            else
            {
                // The previous qualifying element is at qualifyingIndices[i - 1].
                // If we started at or before that index, we would include one extra qualifying element.
                // So we must start strictly after it.
                leftBoundary = qualifyingIndices[i - 1] + 1;
            }

            // Determine the farthest valid ending index.
            int rightBoundary;
            if (i + k == qualifyingIndices.Count)
            {
                // There is no qualifying element after this block,
                // so we can extend all the way to the end of the array.
                rightBoundary = n - 1;
            }
            else
            {
                // The next qualifying element after this block is at qualifyingIndices[i + k].
                // If we ended at or after that index, we would include one extra qualifying element.
                // So we must end strictly before it.
                rightBoundary = qualifyingIndices[i + k] - 1;
            }

            // Compute the length of this maximum valid subarray.
            int length = rightBoundary - leftBoundary + 1;

            // Update the best answer seen so far.
            if (length > answer)
            {
                answer = length;
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] roads1 = { 8, 2, 5, 1, 9, 3, 7 };
int passLimit1 = 3;
int k1 = 2;
int result1 = solution.LongestTollSegmentWithExactPassUsage(roads1, passLimit1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] roads2 = { 4, 6, 8, 10 };
int passLimit2 = 5;
int k2 = 1;
int result2 = solution.LongestTollSegmentWithExactPassUsage(roads2, passLimit2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional demo: no valid subarray
int[] roads3 = { 7, 8, 9 };
int passLimit3 = 3;
int k3 = 1;
int result3 = solution.LongestTollSegmentWithExactPassUsage(roads3, passLimit3, k3);
Console.WriteLine(result3); // Expected: 0

// Additional demo: k = 0
int[] roads4 = { 1, 6, 7, 2, 8, 9, 10 };
int passLimit4 = 3;
int k4 = 0;
int result4 = solution.LongestTollSegmentWithExactPassUsage(roads4, passLimit4, k4);
Console.WriteLine(result4); // Expected: 3, from [8, 9, 10]