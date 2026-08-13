/*
Title: Maximum Sum of Two Non-Overlapping Value Ramps
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array nums of length n. A value ramp is a pair of indices (i, j) such that i < j and nums[i] < nums[j].
The score of that ramp is defined as nums[j] - nums[i].

You must choose exactly two value ramps, (i1, j1) and (i2, j2), such that their index intervals do not overlap.
In other words, either j1 < i2 or j2 < i1.

Your task is to return the maximum possible total score of the two ramps.
If it is impossible to choose two non-overlapping valid ramps, return -1.

Constraints:
- 2 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- Indices are 0-based
- A ramp requires strict inequality: nums[left] < nums[right]

Examples:
1) nums = [4, 1, 7, 2, 9, 3, 8]
   Output: 13
   Explanation:
   Choose ramps (1, 2) with score 7 - 1 = 6 and (3, 4) with score 9 - 2 = 7.
   These intervals [1,2] and [3,4] do not overlap, so the total is 13.

2) nums = [9, 8, 7, 6, 5, 10]
   Output: -1
   Explanation:
   Although there are valid ramps ending at index 5, they all overlap around the same region,
   so it is impossible to choose two non-overlapping ramps.

Key idea of the solution:
We compute:
- bestPrefixEnd[t] = best score of a single valid ramp fully contained in nums[0..t]
- bestSuffixStart[t] = best score of a single valid ramp fully contained in nums[t..n-1]

Then the answer is:
max over split s from 0 to n-2 of bestPrefixEnd[s] + bestSuffixStart[s+1]

This guarantees the two ramps are non-overlapping because:
- the first ramp ends at or before s
- the second ramp starts at or after s+1

How to compute a best single ramp efficiently:
For a prefix:
- scan left to right
- keep the minimum value seen so far
- for each position j, the best ramp ending at j is nums[j] - minSoFar, if nums[j] > minSoFar
- maintain the best score seen so far

For a suffix:
- scan right to left
- keep the maximum value seen so far to the right
- for each position i, the best ramp starting at i is maxSoFar - nums[i], if nums[i] < maxSoFar
- maintain the best score seen so far

This is O(n) time and O(n) space.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n)
      We perform a constant number of linear scans over the array.

    Space Complexity:
    - O(n)
      We store two helper arrays:
      1) bestPrefixEnd
      2) bestSuffixStart
    */
    public long MaxSumOfTwoNonOverlappingValueRamps(int[] nums)
    {
        int n = nums.Length;

        // If the array has fewer than 4 elements, it is impossible to form two non-overlapping ramps,
        // because each ramp needs at least two distinct indices.
        if (n < 4)
        {
            return -1;
        }

        // We use a very negative sentinel to mean:
        // "No valid ramp exists in this region."
        //
        // We choose a long sentinel because later we will add two scores together,
        // and using long keeps everything safe and simple.
        const long NEG_INF = long.MinValue / 4;

        // bestPrefixEnd[i] will store:
        // the maximum score of any single valid ramp fully contained in nums[0..i].
        //
        // Example meaning:
        // bestPrefixEnd[5] = 8 means somewhere inside indices 0..5,
        // there exists a valid ramp with score 8, and no ramp inside 0..5 has a larger score.
        long[] bestPrefixEnd = new long[n];

        // bestSuffixStart[i] will store:
        // the maximum score of any single valid ramp fully contained in nums[i..n-1].
        //
        // Example meaning:
        // bestSuffixStart[3] = 7 means somewhere inside indices 3..n-1,
        // there exists a valid ramp with score 7, and no ramp inside that suffix has a larger score.
        long[] bestSuffixStart = new long[n];

        // ------------------------------------------------------------
        // STEP 1: Build bestPrefixEnd
        // ------------------------------------------------------------
        //
        // We scan from left to right.
        //
        // For each position j, if we want the best ramp ending at j,
        // we should pair nums[j] with the smallest value that appeared before j.
        //
        // Why?
        // Because score = nums[j] - nums[i], so for a fixed nums[j],
        // the score is maximized by minimizing nums[i].
        //
        // Therefore we maintain:
        // - minSoFar = smallest value seen in nums[0..j-1] (or including current after update logic)
        // - bestSoFar = best single-ramp score found anywhere in the prefix so far
        //
        // Then:
        // - candidate at j = nums[j] - minSoFar, but only if nums[j] > minSoFar
        // - bestPrefixEnd[j] = max(bestPrefixEnd[j-1], candidate)
        long minSoFar = nums[0];
        long bestSoFar = NEG_INF;

        // At index 0, no ramp can end here because a ramp needs i < j.
        bestPrefixEnd[0] = NEG_INF;

        for (int j = 1; j < n; j++)
        {
            // If current value is greater than the smallest earlier value,
            // then we can form a valid ramp.
            if (nums[j] > minSoFar)
            {
                long candidate = (long)nums[j] - minSoFar;

                // Update the best single-ramp score found in the prefix so far.
                if (candidate > bestSoFar)
                {
                    bestSoFar = candidate;
                }
            }

            // Store the best score available anywhere in nums[0..j].
            bestPrefixEnd[j] = bestSoFar;

            // Update the minimum value seen so far for future positions.
            if (nums[j] < minSoFar)
            {
                minSoFar = nums[j];
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Build bestSuffixStart
        // ------------------------------------------------------------
        //
        // We scan from right to left.
        //
        // For each position i, if we want the best ramp starting at i,
        // we should pair nums[i] with the largest value that appears after i.
        //
        // Why?
        // Because score = nums[j] - nums[i], so for a fixed nums[i],
        // the score is maximized by maximizing nums[j].
        //
        // Therefore we maintain:
        // - maxSoFar = largest value seen in nums[i+1..n-1] (or including current after update logic)
        // - bestSoFarSuffix = best single-ramp score found anywhere in the suffix so far
        //
        // Then:
        // - candidate at i = maxSoFar - nums[i], but only if nums[i] < maxSoFar
        // - bestSuffixStart[i] = max(bestSuffixStart[i+1], candidate)
        long maxSoFar = nums[n - 1];
        long bestSoFarSuffix = NEG_INF;

        // At index n-1, no ramp can start here because a ramp needs i < j.
        bestSuffixStart[n - 1] = NEG_INF;

        for (int i = n - 2; i >= 0; i--)
        {
            // If current value is smaller than the largest later value,
            // then we can form a valid ramp.
            if (nums[i] < maxSoFar)
            {
                long candidate = maxSoFar - (long)nums[i];

                // Update the best single-ramp score found in the suffix so far.
                if (candidate > bestSoFarSuffix)
                {
                    bestSoFarSuffix = candidate;
                }
            }

            // Store the best score available anywhere in nums[i..n-1].
            bestSuffixStart[i] = bestSoFarSuffix;

            // Update the maximum value seen so far for future positions to the left.
            if (nums[i] > maxSoFar)
            {
                maxSoFar = nums[i];
            }
        }

        // ------------------------------------------------------------
        // STEP 3: Try every split between prefix and suffix
        // ------------------------------------------------------------
        //
        // If we split between s and s+1:
        // - first ramp must lie completely in nums[0..s]
        // - second ramp must lie completely in nums[s+1..n-1]
        //
        // This guarantees the two intervals do not overlap.
        //
        // We test all possible splits and take the maximum sum.
        long answer = NEG_INF;

        for (int s = 0; s < n - 1; s++)
        {
            long leftBest = bestPrefixEnd[s];
            long rightBest = bestSuffixStart[s + 1];

            // Both sides must contain a valid ramp.
            if (leftBest != NEG_INF && rightBest != NEG_INF)
            {
                long total = leftBest + rightBest;
                if (total > answer)
                {
                    answer = total;
                }
            }
        }

        // If answer was never updated, then it was impossible to choose two valid non-overlapping ramps.
        return answer == NEG_INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] nums1 = { 4, 1, 7, 2, 9, 3, 8 };
long result1 = solution.MaxSumOfTwoNonOverlappingValueRamps(nums1);
Console.WriteLine(result1); // Expected: 13

// Example 2
int[] nums2 = { 9, 8, 7, 6, 5, 10 };
long result2 = solution.MaxSumOfTwoNonOverlappingValueRamps(nums2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] nums3 = { 1, 5, 2, 6 };
long result3 = solution.MaxSumOfTwoNonOverlappingValueRamps(nums3);
Console.WriteLine(result3); // Expected: 8 => (0,1)=4 and (2,3)=4

int[] nums4 = { 5, 4, 3, 2 };
long result4 = solution.MaxSumOfTwoNonOverlappingValueRamps(nums4);
Console.WriteLine(result4); // Expected: -1